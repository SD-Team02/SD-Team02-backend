package com.example.delivery.global.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloud.aws")
public record S3Properties(
        String accessKey,
        String secretKey,
        String region,
        S3 s3
) {

    public record S3(
            String bucket,
            Duration presignedUrlExpiration
    ) {
    }
}