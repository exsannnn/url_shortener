package ru.ilyalagutin.urlshortener.cache;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.databind.ObjectMapper;
import ru.ilyalagutin.urlshortener.dto.ShortUrlResponse;

@ExtendWith(MockitoExtension.class)
class RedisShortUrlCacheTest {

    private static final Instant CREATED_AT = Instant.parse("2026-10-10T12:00:00Z");

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisShortUrlCache cache;

    @BeforeEach
    void setUp() {
        cache = new RedisShortUrlCache(redisTemplate, new ObjectMapper());
    }

    @Test
    void savesResponseWithDefaultTtl() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        ShortUrlResponse response = new ShortUrlResponse(
                1L,
                "https://example.com",
                "abc1234",
                CREATED_AT,
                CREATED_AT,
                null
        );

        cache.put(response);

        verify(valueOperations).set(eq("short-url:abc1234"), any(String.class), eq(Duration.ofHours(1)));
    }

    @Test
    void doesNotSaveExpiredResponse() {
        ShortUrlResponse response = new ShortUrlResponse(
                1L,
                "https://example.com",
                "abc1234",
                CREATED_AT,
                CREATED_AT,
                Instant.now().minusSeconds(1)
        );

        cache.put(response);

        verify(redisTemplate).delete("short-url:abc1234");
    }
}