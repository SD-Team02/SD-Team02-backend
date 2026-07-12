package com.example.delivery.user.service;

import com.example.delivery.global.common.response.PageResponse;
import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.user.dto.request.ReqCreateAddressDto;
import com.example.delivery.user.dto.request.ReqUpdateAddressDto;
import com.example.delivery.user.dto.response.ResAddressListDto;
import com.example.delivery.user.dto.response.ResCreateAddressDto;
import com.example.delivery.user.dto.response.ResDeleteAddressDto;
import com.example.delivery.user.dto.response.ResUpdateAddressDto;
import com.example.delivery.user.entity.Address;
import com.example.delivery.user.entity.Role;
import com.example.delivery.user.repository.AddressRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;



    @Transactional
    public ResCreateAddressDto createAddress(Long userId, ReqCreateAddressDto requestDto) {

        String address = requestDto.getAddress();
        String detailAddress = requestDto.getDetailAddress();

        //검증 절차


        Address addressE = new Address(userId,address,detailAddress);
        addressRepository.save(addressE);

        return new ResCreateAddressDto(addressE.getAddressId());
    }

    @Transactional
    public PageResponse<ResAddressListDto> getAddressList(Long userId, Role role, Pageable pageable) {

        Page<Address> addressPage;

        //MASTER라면 모든 주소를 볼 수 있음
        if(role== Role.MASTER) {
            addressPage = addressRepository.findByDeletedAtIsNull(pageable);
        }else {
            addressPage =  addressRepository.findByUserIdAndDeletedAtIsNull(userId,pageable);
        }

        Page<ResAddressListDto> response = addressPage.map( address -> {
             return new ResAddressListDto(
                    address.getUserId(),
                    address.getAddressId(),
                    address.getAddress(),
                    address.getDetailAddress());
        });
            return  PageResponse.from(response);
    }

    @Transactional
    public ResUpdateAddressDto updateAddress(Long userId,Role role ,UUID addressId, @Valid ReqUpdateAddressDto reqUpdateAddressDto) {

        String address = reqUpdateAddressDto.getAddress();;
        String detailAddress = reqUpdateAddressDto.getDetailAddress();

        Optional<Address> checkAddress = addressRepository.findById(addressId);
        if (!checkAddress.isPresent()) {
            throw new BusinessException(ErrorCode.ADDRESS_NOT_FOUND);
        }

        Address addressE =  checkAddress.get();

        //Master가 아닌 사람이 다른 사람의 주소를 수정하지 못하도록
        if (role != Role.MASTER &&
                !addressE.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        //주소 수정
        addressE.update(address,detailAddress);
        return new ResUpdateAddressDto(
                addressE.getAddressId(),
                addressE.getAddress(),
                addressE.getDetailAddress()
        );
    }

    @Transactional
    public ResDeleteAddressDto deleteAddress(UUID addressId, Long userId, Role role) {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        // MASTER가 아니면서 자신의 주소가 아니면 삭제 불가
        if (role != Role.MASTER &&
                !address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 소프트 삭제
        address.delete(userId);

        return new ResDeleteAddressDto(address.getAddressId());

    }

    //주소 상세 정보 [ 주소 단건 검색]
    @Transactional
    public ResAddressListDto addressDetail(UUID addressId, Long userId, Role role) {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        //master만 삭제된 데이터에 접근 가능
        if (role != Role.MASTER && address.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.ADDRESS_NOT_FOUND);
        }

        // MASTER가 아니면 자신의 주소이외에 접근 불가
        if (role != Role.MASTER &&
                !address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return new ResAddressListDto(address.getUserId(), address.getAddressId(),address.getAddress(), address.getDetailAddress());
    }
}
