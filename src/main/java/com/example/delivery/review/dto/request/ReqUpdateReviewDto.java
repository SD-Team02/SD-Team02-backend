package com.example.delivery.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ReqUpdateReviewDto {

	@NotNull
	@Min(1)
	@Max(5)
	private Integer rating;

	@NotBlank
	@Size(max = 500)
	private String content;
}
