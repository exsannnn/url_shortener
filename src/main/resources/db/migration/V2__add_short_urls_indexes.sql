CREATE INDEX IF NOT EXISTS idx_short_urls_expires_at
    ON short_urls (expires_at);

CREATE INDEX IF NOT EXISTS idx_short_urls_created_at
    ON short_urls (created_at);