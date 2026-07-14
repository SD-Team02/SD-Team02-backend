package com.example.delivery.image.controller;

import com.example.delivery.global.config.JpaAuditingConfig;
import com.example.delivery.image.dto.request.ImageRequestDto;
import com.example.delivery.image.dto.response.ImageResponseDto;
import com.example.delivery.image.service.ImageService;
import com.example.delivery.user.security.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ImageController {

    private final ImageService imageService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Tag(name = "이미지", description = "이미지 업로드 API")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ImageResponseDto>> imageUpload(
            @RequestPart("imageRequestDto") String imageRequestDtoJson, // multipart 요청형식 때문에 String 사용 {"refType" : "MENU"}
            @RequestPart("files") List<MultipartFile> files
    ){
        // JSON 문자열을 요청 DTO로 변환
        ImageRequestDto imageRequestDto = convertRequest(imageRequestDtoJson);

        // 이미지 업로드 처리
        List<ImageResponseDto> response = imageService.imageUpLoad(imageRequestDto, files);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    //이미지 가져오기
    @GetMapping("/images")
    @Tag(name = "이미지", description = "이미지 조회 API")
    public ResponseEntity<List<ImageResponseDto>> getImages(
            ImageRequestDto imageRequestDto
    ) {
        return ResponseEntity.ok(imageService.getImages(imageRequestDto));
    }
    //이미지 삭제
    @Tag(name = "이미지", description = "이미지 삭제 API")
    @DeleteMapping("/images/{imageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteImage(
            @PathVariable UUID imageId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        imageService.deleteImage(imageId, userDetails);
        return ResponseEntity.noContent().build();
    }

    private ImageRequestDto convertRequest(String imageRequestJson) {
        try {
            // JSON 문자열을 ImageRequestDto로 변환
            return objectMapper.readValue(
                    imageRequestJson,
                    ImageRequestDto.class
            );

        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "이미지 요청 정보가 올바르지 않습니다."
            );
        }
    }
}
