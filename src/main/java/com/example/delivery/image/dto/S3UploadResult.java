package com.example.delivery.image.dto;

public record S3UploadResult(
        String imageKey,
        String originalName,
        String savedName,
        String contentType,
        long fileSize
) {
}