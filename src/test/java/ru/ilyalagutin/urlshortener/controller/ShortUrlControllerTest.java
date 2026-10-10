package ru.ilyalagutin.urlshortener.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.ilyalagutin.urlshortener.dto.CreateShortUrlRequest;
import ru.ilyalagutin.urlshortener.dto.ShortUrlResponse;
import ru.ilyalagutin.urlshortener.exception.GlobalExceptionHandler;
import ru.ilyalagutin.urlshortener.service.ShortUrlService;

@WebMvcTest({ShortUrlController.class, RedirectController.class, GlobalExceptionHandler.class})
class ShortUrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShortUrlService shortUrlService;

    @Test
    void createsShortUrl() throws Exception {
        ShortUrlResponse response = new ShortUrlResponse(
                1L,
                "https://example.com/long-url",
                "abc1234",
                Instant.parse("2026-10-10T12:00:00Z"),
                Instant.parse("2026-10-10T12:00:00Z"),
                null
        );
        when(shortUrlService.createShortUrl(any(CreateShortUrlRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "url": "https://example.com/long-url"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/shorten/abc1234"))
                .andExpect(jsonPath("$.shortCode").value("abc1234"));
    }

    @Test
    void rejectsInvalidRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("url: URL must not be blank"));
    }

    @Test
    void redirectsToOriginalUrl() throws Exception {
        when(shortUrlService.resolveOriginalUrl(eq("abc1234"))).thenReturn("https://example.com");

        mockMvc.perform(get("/abc1234"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com"));
    }
}
