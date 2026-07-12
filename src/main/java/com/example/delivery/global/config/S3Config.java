package com.example.delivery.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * AWS S3 관련 객체를 Spring Bean으로 등록하는 설정 클래스
 *
 * 쉽게 말하면:
 * S3에 파일을 올리거나,
 * 이미지 임시 URL을 만들 때 필요한 AWS 객체들을
 * Spring이 관리하도록 등록하는 곳이다.
 */
@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

//    @PostConstruct
//    public void checkAwsCredentials() {
//        // AWS Access Key 환경변수 존재 여부 확인
//        System.out.println(
//                "AWS_ACCESS_KEY_ID 존재 여부: "
//                        + (System.getenv("AWS_ACCESS_KEY_ID") != null)
//        );
//
//        // AWS Secret Key 환경변수 존재 여부 확인
//        System.out.println(
//                "AWS_SECRET_ACCESS_KEY 존재 여부: "
//                        + (System.getenv("AWS_SECRET_ACCESS_KEY") != null)
//        );
//    }

    @Bean
    public S3Client s3Client(S3Properties properties) {

        // AWS 인증 정보 생성
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                properties.accessKey(),
                properties.secretKey()
        );

        return S3Client.builder()
                .region(Region.of(properties.region())) // AWS 리전 설정
                .credentialsProvider(StaticCredentialsProvider.create(credentials)) // AWS 인증 정보 설정
                .build(); // S3Client 생성
    }

    @Bean
    public S3Presigner s3Presigner(S3Properties properties) {

        // AWS 인증 정보 생성
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                properties.accessKey(),
                properties.secretKey()
        );

        return S3Presigner.builder()
                .region(Region.of(properties.region())) // AWS 리전 설정
                .credentialsProvider(StaticCredentialsProvider.create(credentials)) // AWS 인증 정보 설정
                .build(); // Presigned URL 생성 객체 생성
    }

}