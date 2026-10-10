package ru.ilyalagutin.urlshortener.dto;

import java.time.Instant;

public record ShortUrlStatsResponse(
        Long id,
        String url,
        String shortCode,
        Instant createdAt,
        Instant updatedAt,
        Instant expiresAt,
        long accessCount
) {
}