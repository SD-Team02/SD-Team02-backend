package com.example.delivery.review.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResReviewListDto {

	private UUID reviewId;

	private String username;

	private Integer rating;

	private String content;

	private LocalDateTime createdAt;
}
