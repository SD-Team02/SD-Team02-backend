package com.example.delivery.user.service;

import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.user.dto.SignupRequestDto;
import com.example.delivery.user.dto.UserInfoDto;
import com.example.delivery.user.dto.UserUpdateDto;
import com.example.delivery.user.entity.Role;
import com.example.delivery.user.entity.User;
import com.example.delivery.user.repository.UserRepository;
import com.example.delivery.user.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //Role 토큰 값
    @Value("${app.auth.token.master}")
    private  String MASTER_TOKEN;

    @Value("${app.auth.token.manager}")
    private  String MANAGER_TOKEN;

    @Value("${app.auth.token.owner}")
    private  String OWNER_TOKEN;

    public void signup(SignupRequestDto requestDto) {

        String username = requestDto.getUsername();
        String password = requestDto.getPassword();
        String nickname = requestDto.getNickname();
        String email = requestDto.getEmail();
        String phone = requestDto.getPhone().trim();    //최대 11자를 맞추기 위해 앞뒤 공백 제거

        //사용중인 중복된 username 탐색
        Optional<User> checkUsername = userRepository.findByUsernameAndDeletedFalse(username);
        System.out.println(checkUsername.isPresent());
        if (checkUsername.isPresent()) {
           throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }

        //username 정규식 [알파벳 소문자와 숫자, 4자이상 10자 이하]
        if(username.length() <4 || username.length() >10)
            throw new IllegalArgumentException("4자 이상 10자 이하로 입력해 주세요");
        if(!username.matches("^[a-z0-9]+$"))
        {
            throw new IllegalArgumentException("영문 소문자와 숫자만 입력해 주세요");
        }

        //비밀번호 정규식 [8자이상 15자 이하, 영어대소문자, 숫자, 특수문자]
        password = passwordValidation(password);

        //사용중인 nickname 중복 탐색
        Optional<User> checkNickname = userRepository.findByNicknameAndDeletedFalse(nickname);
        if (checkNickname.isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        //사용중인 email 중복탐색
        Optional<User> checkEmail = userRepository.findByEmailAndDeletedFalse(email);
        if (checkEmail.isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 이메일 입니다.");
        }

        //email 정규식
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }

        phone = phoneValidation(phone);

        //사용자 Role 확인
        Role role = Role.CUSTOMER;
        //masater
        if(requestDto.isMaster())
        {
            if (!MASTER_TOKEN.equals(requestDto.getMasterToken())) {
                throw new IllegalArgumentException("Master 암호가 틀려 등록이 불가능합니다.");
            }
                role = Role.MASTER;
        }

        //manager
        if(requestDto.isManager())
        {
            if (!MANAGER_TOKEN.equals(requestDto.getManagerToken())) {
                throw new IllegalArgumentException("Manager 암호가 틀려 등록이 불가능합니다.");
            }
            role = Role.MANAGER;
        }

        //owner
        if(requestDto.isOwner())
        {
            if (!OWNER_TOKEN.equals(requestDto.getOwnerToken())) {
                throw new IllegalArgumentException("Owner 암호가 틀려 등록이 불가능합니다.");
            }
            role = Role.OWNER;
        }

        //사용자 등록
        User user = new User(username,nickname,email,password,role,phone);
        userRepository.save(user);
    }

    //유저Id를 통해 정보 찾기
    public UserInfoDto getUserInfoByUserId(Long userId) {
        Optional<User> checkUser = userRepository.findByUserId(userId);

        if (!checkUser.isPresent()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        User user = checkUser.get();

        // DTO에 담아 반환
        return new UserInfoDto(
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.getDeleted()
        );
    }

    @Transactional
    public void updateUserInfo(Long userId, UserUpdateDto userUpdateDto) {
        Optional<User> checkUser = userRepository.findByUserId(userId);

        if (!checkUser.isPresent()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        User user = checkUser.get();

        String email = userUpdateDto.getEmail();
        String nickname = userUpdateDto.getNickname();
        String phone = userUpdateDto.getPhone().trim();
        String password = userUpdateDto.getPassword();

        //회원 가입과 다르게 id가 자신과 다른 경우만 체크
        //사용중인 nickname 중복 탐색
        Optional<User> checkNickname = userRepository.findByNicknameAndDeletedFalse(nickname);
        if (checkNickname.isPresent() && !checkNickname.get().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        //사용중인 email 중복탐색
        Optional<User> checkEmail = userRepository.findByEmailAndDeletedFalse(email);
        if (checkEmail.isPresent() && !checkEmail.get().getUserId().equals(userId)) {
            throw new IllegalArgumentException("이미 사용중인 이메일 입니다.");
        }

        //email 정규식
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }

        password = passwordValidation(password);

        phone = phoneValidation(phone);
        user.update(nickname,email,phone,password, userId);
    }

    //회원 탈퇴/삭제처리
    @Transactional
    public void deleteUser(Long userId) {
        Optional<User> checkUser = userRepository.findByUserId(userId);
        if (!checkUser.isPresent()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        User user = checkUser.get();
        user.withdraw(userId);

    }

    //master권한만, 유저 전체 목록
    public Page<UserInfoDto> getUserList(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserInfoDto::new);
    }


    //비밀번호 정규식 [8자이상 15자 이하, 영어대소문자, 숫자, 특수문자]
    public String passwordValidation(String password)
    {
        if(password.length()<8 || password.length()>15)
        {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
        if(!password.matches("^[A-Za-z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~]+$"))
        {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
        password = passwordEncoder.encode(password);
        return password;
    }

    //phone 정규식
    public String phoneValidation(String phone)
    {
        if(!phone.matches("^0(2|[1-9]\\d)\\d{7,8}$"))
        {
            throw new IllegalArgumentException("올바른 전화번호 양식이 아닙니다.");
        }
        return  phone;
    }


    //로그인 여부 확인 후 사용자Id 가져오기 (user_id)값
    public Long getCurrentUserId(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return userDetails.getUser().getUserId();
    }

    //owner권한 체크
    public void validateOwner(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (userDetails.getUser().getRole() != Role.OWNER) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }
    //manager권한 체크
    public void validateManager(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (userDetails.getUser().getRole() != Role.MANAGER) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    //master 권한 체크
    public void validateMaster(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (userDetails.getUser().getRole() != Role.MASTER) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }
}
