package ru.ilyalagutin.urlshortener.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ilyalagutin.urlshortener.entity.ShortUrl;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    @Modifying
    @Query("update ShortUrl shortUrl "
            + "set shortUrl.accessCount = shortUrl.accessCount + 1 "
            + "where shortUrl.shortCode = :shortCode")
    int incrementAccessCountByShortCode(@Param("shortCode") String shortCode);
}