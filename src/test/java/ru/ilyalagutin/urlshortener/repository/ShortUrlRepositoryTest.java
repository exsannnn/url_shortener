package ru.ilyalagutin.urlshortener.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.ilyalagutin.urlshortener.entity.ShortUrl;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ShortUrlRepositoryTest {

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    @Test
    void findsShortUrlByShortCode() {
        Instant now = Instant.parse("2026-10-10T12:00:00Z");
        ShortUrl shortUrl = new ShortUrl(
                "https://example.com/very/long/url",
                "aB72xQ",
                now,
                null
        );
        shortUrlRepository.save(shortUrl);

        ShortUrl foundShortUrl = shortUrlRepository.findByShortCode("aB72xQ").orElseThrow();

        assertThat(foundShortUrl.getOriginalUrl()).isEqualTo("https://example.com/very/long/url");
        assertThat(foundShortUrl.getAccessCount()).isZero();
    }

    @Test
    void returnsTrueWhenShortCodeExists() {
        Instant now = Instant.parse("2026-10-10T12:00:00Z");
        ShortUrl shortUrl = new ShortUrl(
                "https://example.com",
                "abc123",
                now,
                null
        );
        shortUrlRepository.save(shortUrl);

        boolean exists = shortUrlRepository.existsByShortCode("abc123");

        assertThat(exists).isTrue();
    }
}