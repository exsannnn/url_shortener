package ru.ilyalagutin.urlshortener.service;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ilyalagutin.urlshortener.cache.RedisShortUrlCache;
import ru.ilyalagutin.urlshortener.dto.CreateShortUrlRequest;
import ru.ilyalagutin.urlshortener.dto.ShortUrlResponse;
import ru.ilyalagutin.urlshortener.dto.ShortUrlStatsResponse;
import ru.ilyalagutin.urlshortener.dto.UpdateShortUrlRequest;
import ru.ilyalagutin.urlshortener.entity.ShortUrl;
import ru.ilyalagutin.urlshortener.exception.ShortCodeGenerationException;
import ru.ilyalagutin.urlshortener.exception.ShortUrlExpiredException;
import ru.ilyalagutin.urlshortener.exception.InvalidUrlException;
import ru.ilyalagutin.urlshortener.exception.ShortUrlNotFoundException;
import ru.ilyalagutin.urlshortener.repository.ShortUrlRepository;

@Service
@Transactional(readOnly = true)
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final RandomShortCodeGenerator shortCodeGenerator;
    private final RedisShortUrlCache shortUrlCache;

    public ShortUrlService(
            ShortUrlRepository shortUrlRepository,
            RandomShortCodeGenerator shortCodeGenerator,
            RedisShortUrlCache shortUrlCache
    ) {
        this.shortUrlRepository = shortUrlRepository;
        this.shortCodeGenerator = shortCodeGenerator;
        this.shortUrlCache = shortUrlCache;
    }

    @Transactional
    public ShortUrlResponse createShortUrl(CreateShortUrlRequest request) {
        validateUrl(request.url());

        Instant now = Instant.now();
        String shortCode = generateShortCode();
        ShortUrl shortUrl = new ShortUrl(request.url(), shortCode, now, request.expiresAt());

        ShortUrlResponse response = toResponse(shortUrlRepository.save(shortUrl));
        shortUrlCache.put(response);

        return response;
    }

    public ShortUrlResponse getShortUrl(String shortCode) {
        ShortUrlResponse cached = shortUrlCache.get(shortCode);
        if (cached != null && !isExpired(cached.expiresAt())) {
            return cached;
        }

        ShortUrlResponse response = toResponse(findActiveShortUrl(shortCode));
        shortUrlCache.put(response);
        return response;
    }

    public ShortUrlStatsResponse getStats(String shortCode) {
        return toStatsResponse(findActiveShortUrl(shortCode));
    }

    @Transactional
    public ShortUrlResponse updateShortUrl(String shortCode, UpdateShortUrlRequest request) {
        validateUrl(request.url());

        ShortUrl shortUrl = findActiveShortUrl(shortCode);
        shortUrl.update(request.url(), request.expiresAt(), Instant.now());

        ShortUrlResponse response = toResponse(shortUrl);
        shortUrlCache.put(response);

        return response;
    }

    @Transactional
    public void deleteShortUrl(String shortCode) {
        ShortUrl shortUrl = findShortUrl(shortCode);
        shortUrlRepository.delete(shortUrl);
        shortUrlCache.delete(shortCode);
    }

    @Transactional
    public String resolveOriginalUrl(String shortCode) {
        ShortUrlResponse cachedShortUrl = shortUrlCache.get(shortCode);

        if (cachedShortUrl != null && !isExpired(cachedShortUrl.expiresAt())) {
            incrementAccessCount(shortCode);
            return cachedShortUrl.url();
        }

        ShortUrl shortUrl = findActiveShortUrl(shortCode);
        incrementAccessCount(shortCode);
        shortUrlCache.put(toResponse(shortUrl));

        return shortUrl.getOriginalUrl();
    }

    private String generateShortCode() {
        String shortCode = shortCodeGenerator.generate();

        if (shortUrlRepository.existsByShortCode(shortCode)) {
            throw new ShortCodeGenerationException();
        }

        return shortCode;
    }

    private ShortUrl findActiveShortUrl(String shortCode) {
        ShortUrl shortUrl = findShortUrl(shortCode);

        if (shortUrl.isExpired(Instant.now())) {
            throw new ShortUrlExpiredException(shortCode);
        }

        return shortUrl;
    }

    private ShortUrl findShortUrl(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException(shortCode));
    }

    private void incrementAccessCount(String shortCode) {
        int updatedRows = shortUrlRepository.incrementAccessCountByShortCode(shortCode);

        if (updatedRows == 0) {
            shortUrlCache.delete(shortCode);
            throw new ShortUrlNotFoundException(shortCode);
        }
    }

    private boolean isExpired(Instant expiresAt) {
        return expiresAt != null && !expiresAt.isAfter(Instant.now());
    }

    private void validateUrl(String url) {
        try {
            java.net.URI uri = new java.net.URI(url);
            boolean http = "http".equalsIgnoreCase(uri.getScheme());
            boolean https = "https".equalsIgnoreCase(uri.getScheme());

            if ((!http && !https) || uri.getHost() == null) {
                throw new InvalidUrlException();
            }
        } catch (java.net.URISyntaxException exception) {
            throw new InvalidUrlException();
        }
    }

    private ShortUrlResponse toResponse(ShortUrl shortUrl) {
        return new ShortUrlResponse(
                shortUrl.getId(),
                shortUrl.getOriginalUrl(),
                shortUrl.getShortCode(),
                shortUrl.getCreatedAt(),
                shortUrl.getUpdatedAt(),
                shortUrl.getExpiresAt()
        );
    }

    private ShortUrlStatsResponse toStatsResponse(ShortUrl shortUrl) {
        return new ShortUrlStatsResponse(
                shortUrl.getId(),
                shortUrl.getOriginalUrl(),
                shortUrl.getShortCode(),
                shortUrl.getCreatedAt(),
                shortUrl.getUpdatedAt(),
                shortUrl.getExpiresAt(),
                shortUrl.getAccessCount()
        );
    }
}
