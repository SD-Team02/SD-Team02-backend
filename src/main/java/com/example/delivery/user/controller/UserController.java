package com.example.delivery.user.controller;

import com.example.delivery.global.common.response.ApiResponse;
import com.example.delivery.global.common.util.PageableFactory;
import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.user.dto.SignupRequestDto;
import com.example.delivery.user.dto.UserInfoDto;
import com.example.delivery.user.dto.UserUpdateDto;
import com.example.delivery.user.security.UserDetailsImpl;
import com.example.delivery.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


//swagger에대한 것은 Ai를 사용해 작성한 것이라 추후 수정이 필요할 수 있음
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "회원", description = "회원 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequestDto requestDto) {

        userService.signup(requestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.successMessage("회원가입 성공"));
    }

    @Operation(summary = "회원 정보 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/user/info/{username}")
    public ResponseEntity<ApiResponse<UserInfoDto>> userInfo(
            @PathVariable String username,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Long userId = userDetails.getUser().getUserId();
        UserInfoDto userInfo = userService.getUserInfoByUserId(userId);

        return ResponseEntity.ok(
                ApiResponse.success("회원 조회 성공", userInfo)
        );
    }

    @Operation(summary = "회원 정보 수정")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PutMapping("/user/update/{username}")
    public ResponseEntity<ApiResponse<Void>> updateUser(
            @PathVariable String username,
            @RequestBody UserUpdateDto userUpdateDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Long userId = userDetails.getUser().getUserId();

        userService.updateUserInfo(userId, userUpdateDto);

        return ResponseEntity.ok(
                ApiResponse.successMessage("회원 수정 성공")
        );
    }

    @Operation(summary = "회원 삭제")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @DeleteMapping("/user/delete/{username}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable String username,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Long userId = userDetails.getUser().getUserId();

        userService.deleteUser(userId);

        return ResponseEntity.ok(
                ApiResponse.successMessage("회원 삭제 성공")
        );
    }

    @GetMapping("/user/list")
    @Operation(summary = "전체 회원 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "MASTER 권한 필요")
    })
    public ResponseEntity<ApiResponse<Page<UserInfoDto>>> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        // 로그인 여부 확인
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // MASTER 권한 확인
        if (!userDetails.getUser().getRole().name().equals("MASTER")) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        Pageable pageable = PageableFactory.of(page, size, sortBy, direction);
        Page<UserInfoDto> users = userService.getUserList(pageable);

        return ResponseEntity.ok(
                ApiResponse.success("회원 목록 조회 성공", users)
        );
    }

}