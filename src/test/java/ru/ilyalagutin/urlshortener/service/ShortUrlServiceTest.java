package ru.ilyalagutin.urlshortener.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ilyalagutin.urlshortener.dto.CreateShortUrlRequest;
import ru.ilyalagutin.urlshortener.dto.ShortUrlResponse;
import ru.ilyalagutin.urlshortener.dto.UpdateShortUrlRequest;
import ru.ilyalagutin.urlshortener.entity.ShortUrl;
import ru.ilyalagutin.urlshortener.exception.InvalidUrlException;
import ru.ilyalagutin.urlshortener.exception.ShortUrlExpiredException;
import ru.ilyalagutin.urlshortener.cache.RedisShortUrlCache;
import ru.ilyalagutin.urlshortener.repository.ShortUrlRepository;

@ExtendWith(MockitoExtension.class)
class ShortUrlServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-10T12:00:00Z");

    @Mock
    private ShortUrlRepository shortUrlRepository;

    @Mock
    private RandomShortCodeGenerator shortCodeGenerator;

    @Mock
    private RedisShortUrlCache shortUrlCache;

    @Captor
    private ArgumentCaptor<ShortUrl> shortUrlCaptor;

    private ShortUrlService shortUrlService;

    @BeforeEach
    void setUp() {
        shortUrlService = new ShortUrlService(
                shortUrlRepository,
                shortCodeGenerator,
                shortUrlCache
        );
    }

    @Test
    void createsShortUrlWithGeneratedUniqueCode() {
        CreateShortUrlRequest request = new CreateShortUrlRequest(
                "https://example.com/very/long/url",
                null
        );
        when(shortCodeGenerator.generate()).thenReturn("abc1234");
        when(shortUrlRepository.existsByShortCode("abc1234")).thenReturn(false);
        when(shortUrlRepository.save(any(ShortUrl.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShortUrlResponse response = shortUrlService.createShortUrl(request);

        verify(shortUrlRepository).save(shortUrlCaptor.capture());
        ShortUrl savedShortUrl = shortUrlCaptor.getValue();
        assertThat(savedShortUrl.getOriginalUrl()).isEqualTo("https://example.com/very/long/url");
        assertThat(savedShortUrl.getShortCode()).isEqualTo("abc1234");
        assertThat(savedShortUrl.getCreatedAt()).isNotNull();
        assertThat(response.shortCode()).isEqualTo("abc1234");
        verify(shortUrlCache).put(response);
    }

    @Test
    void rejectsExistingCode() {
        CreateShortUrlRequest request = new CreateShortUrlRequest("https://example.com", null);
        when(shortCodeGenerator.generate()).thenReturn("abc1234");
        when(shortUrlRepository.existsByShortCode("abc1234")).thenReturn(true);

        assertThatThrownBy(() -> shortUrlService.createShortUrl(request))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void rejectsUrlWithUnsupportedScheme() {
        CreateShortUrlRequest request = new CreateShortUrlRequest("ftp://example.com/file", null);

        assertThatThrownBy(() -> shortUrlService.createShortUrl(request))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void rejectsExpiredShortUrl() {
        ShortUrl shortUrl = new ShortUrl(
                "https://example.com",
                "abc1234",
                NOW.minusSeconds(3600),
                Instant.now().minusSeconds(1)
        );
        when(shortUrlRepository.findByShortCode("abc1234")).thenReturn(Optional.of(shortUrl));

        assertThatThrownBy(() -> shortUrlService.getShortUrl("abc1234"))
                .isInstanceOf(ShortUrlExpiredException.class);
    }

    @Test
    void resolvesCachedShortUrlAndIncrementsCounterInDatabase() {
        ShortUrlResponse cachedResponse = new ShortUrlResponse(
                1L,
                "https://example.com",
                "abc1234",
                NOW,
                NOW,
                null
        );
        when(shortUrlCache.get("abc1234")).thenReturn(cachedResponse);
        when(shortUrlRepository.incrementAccessCountByShortCode("abc1234")).thenReturn(1);

        String originalUrl = shortUrlService.resolveOriginalUrl("abc1234");

        assertThat(originalUrl).isEqualTo("https://example.com");
        verify(shortUrlRepository).incrementAccessCountByShortCode("abc1234");
        verify(shortUrlRepository, times(0)).findByShortCode("abc1234");
    }

    @Test
    void updatesExistingShortUrl() {
        ShortUrl shortUrl = new ShortUrl("https://example.com", "abc1234", NOW, null);
        UpdateShortUrlRequest request = new UpdateShortUrlRequest(
                "https://example.org/new-url",
                NOW.plusSeconds(3600)
        );
        when(shortUrlRepository.findByShortCode("abc1234")).thenReturn(Optional.of(shortUrl));

        ShortUrlResponse response = shortUrlService.updateShortUrl("abc1234", request);

        assertThat(response.url()).isEqualTo("https://example.org/new-url");
        assertThat(response.expiresAt()).isEqualTo(NOW.plusSeconds(3600));
        verify(shortUrlCache).put(response);
    }

    @Test
    void deletesShortUrlAndEvictsCache() {
        ShortUrl shortUrl = new ShortUrl("https://example.com", "abc1234", NOW, null);
        when(shortUrlRepository.findByShortCode("abc1234")).thenReturn(Optional.of(shortUrl));

        shortUrlService.deleteShortUrl("abc1234");

        verify(shortUrlRepository).delete(shortUrl);
        verify(shortUrlCache).delete("abc1234");
    }
}
