package ru.ilyalagutin.urlshortener.exception;

public class ShortUrlNotFoundException extends RuntimeException {

    public ShortUrlNotFoundException(String shortCode) {
        super("Short URL with code '%s' was not found".formatted(shortCode));
    }
}