package com.sharks.event.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Supabase Storage S3-compatible credentials (server-side only).
 * {@code url} is the S3 endpoint, e.g. {@code https://<ref>.storage.supabase.co/storage/v1/s3}.
 */
@ConfigurationProperties(prefix = "supabase.storage")
public record SupabaseStorageProperties(String url, String bucket, String accessKey, String secretKey) {
}
