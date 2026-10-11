package ru.ilyalagutin.urlshortener.cache;

import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import ru.ilyalagutin.urlshortener.dto.ShortUrlResponse;

@Component
public class RedisShortUrlCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisShortUrlCache.class);
    private static final Duration DEFAULT_TTL = Duration.ofHours(1);
    private static final String KEY_PREFIX = "short-url:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisShortUrlCache(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public ShortUrlResponse get(String shortCode) {
        try {
            String value = redisTemplate.opsForValue().get(buildKey(shortCode));

            if (value == null) {
                return null;
            }

            return objectMapper.readValue(value, ShortUrlResponse.class);
        } catch (DataAccessException | JacksonException exception) {
            LOGGER.warn("Unable to read short URL {} from Redis cache", shortCode, exception);
            return null;
        }
    }

    public void put(ShortUrlResponse shortUrlResponse) {
        try {
            String value = objectMapper.writeValueAsString(shortUrlResponse);
            Duration ttl = getTtl(shortUrlResponse.expiresAt());

            if (ttl.isZero() || ttl.isNegative()) {
                delete(shortUrlResponse.shortCode());
                return;
            }

            redisTemplate.opsForValue().set(buildKey(shortUrlResponse.shortCode()), value, ttl);
        } catch (DataAccessException | JacksonException exception) {
            LOGGER.warn("Unable to cache short URL {} in Redis", shortUrlResponse.shortCode(), exception);
        }
    }

    public void delete(String shortCode) {
        try {
            redisTemplate.delete(buildKey(shortCode));
        } catch (DataAccessException exception) {
            LOGGER.warn("Unable to evict short URL {} from Redis cache", shortCode, exception);
        }
    }

    private Duration getTtl(Instant expiresAt) {
        if (expiresAt == null) {
            return DEFAULT_TTL;
        }

        return Duration.between(Instant.now(), expiresAt);
    }

    private String buildKey(String shortCode) {
        return KEY_PREFIX + shortCode;
    }
}