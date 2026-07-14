package com.example.delivery.image.service;

import java.io.IOException;

import com.example.delivery.global.config.S3Properties;
import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.image.dto.S3UploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3StorageServiceImpl implements S3StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties properties;

    // 허용할 이미지 타입
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    // 최대 파일 크기(10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // 다건 이미지업로드
    @Override
    public List<S3UploadResult> upload(List<MultipartFile> files) {

        // 파일 목록 검증
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
        }

        return files.stream()
                .map(this::uploadOne)
                .toList();
    }

    // 이미지삭제
    @Override
    public void delete(String imageKey) {
        // 삭제할 이미지 Key 검증
        if (!StringUtils.hasText(imageKey)) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_DELETE_FILE);
        }

        // S3 삭제 요청 생성
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(properties.s3().bucket())
                .key(imageKey)
                .build();

        try {
            // S3 이미지 삭제
            s3Client.deleteObject(deleteObjectRequest);

        } catch (S3Exception e) {
            throw new BusinessException(ErrorCode.IMAGE_DELETE_FAILED);
        }
    }
    // 이미지 가져오기
    @Override
    public String generatePresignedUrl(String imageKey) {

        // 이미지 Key가 없으면 URL을 생성하지 않음
        if (!StringUtils.hasText(imageKey)) {
            return null;
        }

        // 조회할 S3 객체 정보 생성
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(properties.s3().bucket())
                .key(imageKey)
                .build();

        // Presigned URL 생성 요청 설정
        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(
                                properties.s3().presignedUrlExpiration()
                        )
                        .getObjectRequest(getObjectRequest)
                        .build();

        try {
            // 제한 시간 동안 유효한 이미지 URL 생성
            return s3Presigner
                    .presignGetObject(presignRequest)
                    .url()
                    .toString();

        } catch (S3Exception e) {
            throw new BusinessException(
                    ErrorCode.IMAGE_URL_GENERATION_FAILED
            );
        }
    }


    /**
     * 업로드 파일 검증
     */
    private void validateFile(MultipartFile file) {

        // 파일 존재 여부 확인
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
        }

        // 파일 크기 확인
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.IMAGE_SIZE_EXCEEDED);
        }

        // 이미지 타입 확인
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_TYPE);
        }
    }

    /**
     * S3 저장 Key 생성
     */
    private String createImageKey(MultipartFile file) {

        // 원본 파일명 조회
        String originalFilename = file.getOriginalFilename();

        // 파일 확장자 추출
        String extension = StringUtils.getFilenameExtension(originalFilename);

        // 확장자 존재 여부 확인
        if (!StringUtils.hasText(extension)) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_TYPE);
        }

        // UUID 기반 S3 Key 생성
        return "menu-images/"
                + UUID.randomUUID()
                + "."
                + extension.toLowerCase(Locale.ROOT);
    }

    /**
     * S3 이미지 업로드
     */
    private void uploadToS3(MultipartFile file, String imageKey) {

        // S3 업로드 요청 생성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.s3().bucket())
                .key(imageKey)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        // 파일 입력 스트림 생성
        try (InputStream inputStream = file.getInputStream()) {

            // 이미지 데이터를 S3에 업로드
            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(
                            inputStream,
                            file.getSize()
                    )
            );

        } catch (IOException | S3Exception e) {
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    private String extractSavedName(String imageKey) {
        // S3 Key에서 파일명 추출
        return imageKey.substring(imageKey.lastIndexOf('/') + 1);
    }

    // 이미지 업로드
    private S3UploadResult uploadOne(MultipartFile file) {

        // 업로드 파일 검증
        validateFile(file);

        // S3 저장 Key 생성
        String imageKey = createImageKey(file);

        // S3 이미지 업로드
        uploadToS3(file, imageKey);

        // 저장 파일명 추출
        String savedName = extractSavedName(imageKey);

        // 업로드 결과 반환
        return new S3UploadResult(
                imageKey,
                file.getOriginalFilename(),
                savedName,
                file.getContentType(),
                file.getSize()
        );
    }
}