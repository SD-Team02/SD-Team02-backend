package com.example.delivery.global.common.util;

import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.user.entity.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie; // 추가됨
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URLDecoder; // 추가됨
import java.nio.charset.StandardCharsets; // 추가됨
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {
    // Header KEY 값
    public static final String AUTHORIZATION_HEADER = "Authorization";
    // 사용자 권한 값의 KEY
    public static final String AUTHORIZATION_KEY = "auth";
    // Token 식별자
    public static final String BEARER_PREFIX = "Bearer ";

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.access-token-expire-ms}")
    private long TOKEN_TIME;

    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init(){
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    // 토큰 생성
    public String createToken(String username, Role role) {
        Date date = new Date();

        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(username) // 사용자 식별자값(ID)
                        .claim(AUTHORIZATION_KEY, role) // 사용자 권한
                        .setExpiration(new Date(date.getTime() + TOKEN_TIME)) // 만료 시간
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm) // 암호화 알고리즘
                        .compact();
    }

    // Header 또는 Cookie에서 JWT 가져오기 (수정된 메서드)
    public String getJwtToken(HttpServletRequest request) {
        // 1. 우선 순위로 HTTP Header에서 토큰 추출 시도 (Postman, Ajax 요청 대응)
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        // 2. Header에 없다면 Cookie에서 토큰 추출 시도 (window.location.href 이동 대응)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // 프론트엔드에서 저장한 쿠키 이름이 "Authorization"인지 확인
                if (AUTHORIZATION_HEADER.equals(cookie.getName())) {
                    String token = cookie.getValue();
                    if (StringUtils.hasText(token)) {
                        try {
                            // 쿠키 변환 시 발생할 수 있는 공백(%20 등) 인코딩 문제를 해결하기 위해 디코딩 진행
                            String decodedToken = URLDecoder.decode(token, StandardCharsets.UTF_8);

                            // 디코딩된 값에 "Bearer "가 붙어 있다면 제거 후 반환
                            if (decodedToken.startsWith(BEARER_PREFIX)) {
                                return decodedToken.substring(BEARER_PREFIX.length());
                            }
                            return decodedToken;
                        } catch (Exception e) {
                            log.error("쿠키 디코딩 중 에러 발생: {}", e.getMessage());
                            return null;
                        }
                    }
                }
            }
        }

        // 둘 다 없으면 null 반환
        return null;
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException
                 | MalformedJwtException
                 | ExpiredJwtException
                 | UnsupportedJwtException
                 | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    // 토큰에서 사용자 정보 가져오기
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parser().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}