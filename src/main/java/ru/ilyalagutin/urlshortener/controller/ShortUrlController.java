package ru.ilyalagutin.urlshortener.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.ilyalagutin.urlshortener.dto.CreateShortUrlRequest;
import ru.ilyalagutin.urlshortener.dto.ShortUrlResponse;
import ru.ilyalagutin.urlshortener.dto.ShortUrlStatsResponse;
import ru.ilyalagutin.urlshortener.dto.UpdateShortUrlRequest;
import ru.ilyalagutin.urlshortener.service.ShortUrlService;

@RestController
@Validated
@RequestMapping("/api/v1/shorten")
public class ShortUrlController {

    private static final String SHORT_CODE_PATTERN = "[A-Za-z0-9]{7}";

    private final ShortUrlService shortUrlService;

    public ShortUrlController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    @PostMapping
    public ResponseEntity<ShortUrlResponse> createShortUrl(
            @Valid @RequestBody CreateShortUrlRequest request
    ) {
        ShortUrlResponse response = shortUrlService.createShortUrl(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{shortCode}")
                .buildAndExpand(response.shortCode())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{shortCode}")
    public ShortUrlResponse getShortUrl(
            @PathVariable @Pattern(regexp = SHORT_CODE_PATTERN) String shortCode
    ) {
        return shortUrlService.getShortUrl(shortCode);
    }

    @PutMapping("/{shortCode}")
    public ShortUrlResponse updateShortUrl(
            @PathVariable @Pattern(regexp = SHORT_CODE_PATTERN) String shortCode,
            @Valid @RequestBody UpdateShortUrlRequest request
    ) {
        return shortUrlService.updateShortUrl(shortCode, request);
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> deleteShortUrl(
            @PathVariable @Pattern(regexp = SHORT_CODE_PATTERN) String shortCode
    ) {
        shortUrlService.deleteShortUrl(shortCode);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{shortCode}/stats")
    public ShortUrlStatsResponse getStats(
            @PathVariable @Pattern(regexp = SHORT_CODE_PATTERN) String shortCode
    ) {
        return shortUrlService.getStats(shortCode);
    }
}