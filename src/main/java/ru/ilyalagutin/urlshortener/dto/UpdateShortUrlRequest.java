package ru.ilyalagutin.urlshortener.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record UpdateShortUrlRequest(
        @NotBlank(message = "URL must not be blank")
        @Size(max = 2048, message = "URL must not exceed 2048 characters")
        String url,

        @Future(message = "Expiration date must be in the future")
        Instant expiresAt
) {
}