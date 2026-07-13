package com.example.delivery.user.controller;

import com.example.delivery.global.common.response.ApiResponse;
import com.example.delivery.global.common.response.PageResponse;
import com.example.delivery.global.common.util.PageableFactory;
import com.example.delivery.user.dto.request.ReqCreateAddressDto;
import com.example.delivery.user.dto.request.ReqUpdateAddressDto;
import com.example.delivery.user.dto.response.ResAddressListDto;
import com.example.delivery.user.dto.response.ResCreateAddressDto;
import com.example.delivery.user.dto.response.ResDeleteAddressDto;
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

    @Operation(summary = "주소 등록")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주소 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
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


    @Operation(summary = "주소 목록 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주소 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인증 필요"),
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
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

    @Operation(summary = "주소 수정")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주소 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인증 필요"),
    })
    @PutMapping("/{addressId}/update")
    @PreAuthorize("isAuthenticated()")
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


    @Operation(summary = "주소 삭제")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @DeleteMapping("/{addressId}/delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ResDeleteAddressDto>> deleteAddress(
            @PathVariable UUID addressId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userService.getCurrentUserId(userDetails);
        Role role = userDetails.getUser().getRole();

        ResDeleteAddressDto resDeleteAddressDto = addressService.deleteAddress(addressId, userId, role);
        return ResponseEntity.ok(ApiResponse.success("주소 삭제 성공",resDeleteAddressDto));
    }


    @Operation(summary = "주소 상세")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주소 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인증 필요"),
    })
    @GetMapping("/{addressId}/info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ResAddressListDto>> getAddress(
            @PathVariable UUID addressId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        Long userId = userService.getCurrentUserId(userDetails);
        Role role = userDetails.getUser().getRole();

        ResAddressListDto resAddressListDto = addressService.addressDetail(addressId, userId, role);
        return ResponseEntity
                .ok(ApiResponse.success("주소가 조회되었습니다." , resAddressListDto));
    }


}
