package ru.ilyalagutin.urlshortener.exception;

public class ShortCodeGenerationException extends RuntimeException {

    public ShortCodeGenerationException() {
        super("Unable to generate a unique short code");
    }
}