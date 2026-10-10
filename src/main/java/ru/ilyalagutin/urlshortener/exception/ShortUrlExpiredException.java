package ru.ilyalagutin.urlshortener.exception;

public class ShortUrlExpiredException extends RuntimeException {

    public ShortUrlExpiredException(String shortCode) {
        super("Short URL with code '%s' has expired".formatted(shortCode));
    }
}