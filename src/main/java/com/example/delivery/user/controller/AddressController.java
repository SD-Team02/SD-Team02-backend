package com.example.delivery.user.controller;

import com.example.delivery.global.common.response.ApiResponse;
import com.example.delivery.global.common.response.PageResponse;
import com.example.delivery.global.common.util.PageableFactory;
import com.example.delivery.user.dto.request.ReqCreateAddressDto;
import com.example.delivery.user.dto.request.ReqUpdateAddressDto;
import com.example.delivery.user.dto.response.ResAddressListDto;
import com.example.delivery.user.dto.response.ResCreateAddressDto;
import com.example.delivery.user.dto.response.ResUpdateAddressDto;
import com.example.delivery.user.entity.Role;
import com.example.delivery.user.security.UserDetailsImpl;
import com.example.delivery.user.service.AddressService;
import com.example.delivery.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/address")
public class AddressController {

    private final AddressService addressService;
    private final UserService userService;

    @RequestMapping("/create")
    @Operation(summary = "주소 등록")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주소 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ResCreateAddressDto>> createAddress(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ReqCreateAddressDto reqCreateAddressDto)
    {

        Long userId = userService.getCurrentUserId(userDetails);
        ResCreateAddressDto resCreateAddressDto = addressService.createAddress(userId, reqCreateAddressDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("주소 등록 성공",resCreateAddressDto));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주소 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인증 필요"),
    })
    @Operation(summary = "주소 목록 조회")
    public ResponseEntity<ApiResponse<PageResponse<ResAddressListDto>>> getAddressList(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(required = false) Integer size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "DESC") String direction
    ){
        Long userId = userService.getCurrentUserId(userDetails);
        Role role = userDetails.getUser().getRole();

        Pageable pageable = PageableFactory.of(page, size, sortBy, direction);
        PageResponse<ResAddressListDto> addressList = addressService.getAddressList(userId,role,pageable);
        return ResponseEntity
                .ok(ApiResponse.success("주소가 조회되었습니다." , addressList));
    }


    @PreAuthorize("isAuthenticated()")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주소 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인증 필요"),
    })
    @PutMapping("/{addressId}")
    @Operation(summary = "주소 수정")
    public ResponseEntity<ApiResponse<ResUpdateAddressDto>> updateAddress(
            @PathVariable UUID addressId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ReqUpdateAddressDto reqUpdateAddressDto
            ){

        Long userId = userService.getCurrentUserId(userDetails);
        Role role = userDetails.getUser().getRole();

        ResUpdateAddressDto resUpdateAddressDto = addressService.updateAddress(userId,role, addressId,reqUpdateAddressDto);
        return ResponseEntity.ok(ApiResponse.success("주소가 성공적으로 수정되었습니다.",resUpdateAddressDto));
    }

}
