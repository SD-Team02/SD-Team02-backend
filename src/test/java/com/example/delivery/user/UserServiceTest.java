package com.example.delivery.user;


import com.example.delivery.user.dto.SignupRequestDto;
import com.example.delivery.user.entity.Role;
import com.example.delivery.user.entity.User;
import com.example.delivery.user.repository.UserRepository;
import com.example.delivery.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

//@SpringBootTest
//@Transactional
//@Rollback(false)
//@ActiveProfiles("test")
public class UserServiceTest {

    //테스트용 , db에는 등록 안됨
    @InjectMocks
    UserService userService;
    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    //db등록 테스트
//    @Autowired
//    UserRepository userRepository;
//    @Autowired
//    PasswordEncoder passwordEncoder;
//    @Autowired
//    UserService userService;


    @Test
    void 신규_유저_등록_테스트(){
        //중복 검사는 테스트 하지 않음

        SignupRequestDto signupRequestDto = new SignupRequestDto();
        Role role = Role.CUSTOMER;

        signupRequestDto.setUsername("asdf12");
        signupRequestDto.setPassword("Abcd1234!");
        signupRequestDto.setNickname("홍길동");
        signupRequestDto.setEmail("test@test.com");
        signupRequestDto.setPhone("01012345678");
        signupRequestDto.setRole("CUSTOMER");

        when(passwordEncoder.encode(any()))
                .thenReturn("encodedPassword");

        when(userRepository.findByUsernameAndDeletedFalse(any()))
                .thenReturn(Optional.empty());

        when(userRepository.findByNicknameAndDeletedFalse(any()))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmailAndDeletedFalse(any()))
                .thenReturn(Optional.empty());

        when(userRepository.findByPhoneAndDeletedFalse(any()))
                .thenReturn(Optional.empty());

        userService.signup(signupRequestDto);

        verify(userRepository, times(1))
                .save(any(User.class));

        System.out.println("username : " + signupRequestDto.getUsername());
        System.out.println("password : " + signupRequestDto.getPassword());
        System.out.println("nickname : " + signupRequestDto.getNickname());
        System.out.println("email : " + signupRequestDto.getEmail());
        System.out.println("phone : " + signupRequestDto.getPhone());
    }

    @Test
    void 중복_username_등록_테스트(){

        SignupRequestDto signupRequestDto = new SignupRequestDto();
        Role role = Role.CUSTOMER;
        User user = new User("asdf12","asd1234","asd@asd.com","asd12345!",role,"01012344321");

        signupRequestDto.setUsername("asdf12");
        signupRequestDto.setPassword("Abcd1234!");
        signupRequestDto.setNickname("aass");
        signupRequestDto.setEmail("test@test.com");
        signupRequestDto.setPhone("01012345678");
        signupRequestDto.setRole("CUSTOMER");

        when(userRepository.findByUsernameAndDeletedFalse(any()))
                .thenReturn(Optional.of(user));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.signup(signupRequestDto)
        );

        // 예외 메시지 출력
        System.out.println("예외 메시지 : " + exception.getMessage());

        // Repository가 실제로 반환하는 값도 출력
        System.out.println("Repository 결과 : "
                + userRepository.findByUsernameAndDeletedFalse("asdf12"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void 중복_닉네임_테스트(){
        SignupRequestDto signupRequestDto = new SignupRequestDto();
        Role role = Role.CUSTOMER;
        User user = new User("asdf122","asd1234","asd@asd.com","asd12345!",role,"01012344321");

        signupRequestDto.setUsername("asdf12");
        signupRequestDto.setPassword("Abcd1234!");
        signupRequestDto.setNickname("asd1234");
        signupRequestDto.setEmail("test@test.com");
        signupRequestDto.setPhone("01012345678");
        signupRequestDto.setRole("CUSTOMER");

        when(userRepository.findByUsernameAndDeletedFalse(any()))
                .thenReturn(Optional.empty());

        when(userRepository.findByNicknameAndDeletedFalse(any()))
                .thenReturn(Optional.of(user));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.signup(signupRequestDto)
        );

        // 예외 메시지 출력
        System.out.println("예외 메시지 : " + exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void 중복_이메일_테스트(){
        SignupRequestDto signupRequestDto = new SignupRequestDto();
        Role role = Role.CUSTOMER;
        User user = new User("asdf122","asd12324","test@test.com","asd12345!",role,"01012344321");

        signupRequestDto.setUsername("asdf12");
        signupRequestDto.setPassword("Abcd1234!");
        signupRequestDto.setNickname("asd1234");
        signupRequestDto.setEmail("test@test.com");
        signupRequestDto.setPhone("01012345678");
        signupRequestDto.setRole("CUSTOMER");

        when(userRepository.findByUsernameAndDeletedFalse(any()))
                .thenReturn(Optional.empty());

        when(userRepository.findByNicknameAndDeletedFalse(any()))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmailAndDeletedFalse(any()))
                .thenReturn(Optional.of(user));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.signup(signupRequestDto)
        );

        // 예외 메시지 출력
        System.out.println("예외 메시지 : " + exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void 실제_DB_회원등록_테스트() {

        Role role = Role.CUSTOMER;

        User user = new User(
                "asdf12",
                "홍길동",
                "test@test.com",
                passwordEncoder.encode("Abcd1234!"),
                role,
                "01012345678"
        );

        User savedUser = userRepository.save(user);

        System.out.println("저장 완료");
        System.out.println("userId : " + savedUser.getUserId());
        System.out.println("username : " + savedUser.getUsername());
    }

}
