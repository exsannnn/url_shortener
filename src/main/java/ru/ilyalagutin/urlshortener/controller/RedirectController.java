package ru.ilyalagutin.urlshortener.controller;

import jakarta.validation.constraints.Pattern;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.ilyalagutin.urlshortener.service.ShortUrlService;

@RestController
@Validated
public class RedirectController {

    private static final String SHORT_CODE_PATTERN = "[A-Za-z0-9]{7}";

    private final ShortUrlService shortUrlService;

    public RedirectController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable @Pattern(regexp = SHORT_CODE_PATTERN) String shortCode
    ) {
        String originalUrl = shortUrlService.resolveOriginalUrl(shortCode);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}