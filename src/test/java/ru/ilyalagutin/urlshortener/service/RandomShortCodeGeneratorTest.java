package ru.ilyalagutin.urlshortener.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RandomShortCodeGeneratorTest {

    private final RandomShortCodeGenerator generator = new RandomShortCodeGenerator();

    @Test
    void generatesShortCodeWithExpectedFormat() {
        String shortCode = generator.generate();

        assertThat(shortCode)
                .hasSize(7)
                .matches("[A-Za-z0-9]{7}");
    }

    @Test
    void generatesDifferentCodes() {
        String firstCode = generator.generate();
        String secondCode = generator.generate();

        assertThat(firstCode).isNotEqualTo(secondCode);
    }
}