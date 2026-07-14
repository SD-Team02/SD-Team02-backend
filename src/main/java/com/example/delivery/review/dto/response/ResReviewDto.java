package com.example.delivery.review.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResReviewDto {

	private UUID reviewId;

	private UUID storeId;

	private String username;

	private Integer rating;

	private String content;

	private LocalDateTime createdAt;
}
