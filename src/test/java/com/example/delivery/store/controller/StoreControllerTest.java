package com.example.delivery.store.controller;

import com.example.delivery.store.dto.request.ReqCreateStoreDto;
import com.example.delivery.store.service.StoreService;
import com.example.delivery.user.entity.User;
import com.example.delivery.user.security.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoreController.class)
@Import(StoreControllerTest.TestSecurityConfig.class)
public class StoreControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StoreService storeService;

    @Test
    @DisplayName("가게 등록 실패 - CUSTOMER 접근 금지")
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    void createStore_Fail_CustomerAccessDenied() throws Exception {
        ReqCreateStoreDto dto = new ReqCreateStoreDto(
                UUID.randomUUID(), UUID.randomUUID(), "가게", "주소", "010-1234-5678", LocalTime.now(), LocalTime.now(), null
        );

        mockMvc.perform(post("/api/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("가게 등록 성공 - OWNER 접근 허용")
    void createStore_Success_OwnerAccess() throws Exception {
        ReqCreateStoreDto dto = new ReqCreateStoreDto(
                UUID.randomUUID(), UUID.randomUUID(), "가게", "주소", "010-1234-5678", LocalTime.now(), LocalTime.now(), null
        );

        User user = mock(User.class);
        when(user.getUserId()).thenReturn(1L);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, List.of(new SimpleGrantedAuthority("OWNER"))
        );

        mockMvc.perform(post("/api/stores")
                        .with(securityContext(new SecurityContextImpl(authentication)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }
}
