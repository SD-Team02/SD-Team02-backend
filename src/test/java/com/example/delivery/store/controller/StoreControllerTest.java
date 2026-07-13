package com.example.delivery.store.controller;

import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.store.dto.request.ReqCreateStoreDto;
import com.example.delivery.store.dto.request.ReqUpdateStoreDto;
import com.example.delivery.store.dto.response.ResDeleteStoreDto;
import com.example.delivery.store.dto.response.ResGetStoreDto;
import com.example.delivery.store.dto.response.ResSearchStoreDto;
import com.example.delivery.store.entity.StoreStatus;
import com.example.delivery.store.service.StoreService;
import com.example.delivery.user.entity.Role;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    private SecurityContextImpl ownerSecurityContext() {
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(1L);
        when(user.getRole()).thenReturn(Role.OWNER);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, List.of(new SimpleGrantedAuthority("OWNER"))
        );
        return new SecurityContextImpl(authentication);
    }

    @Test
    @DisplayName("전체 가게 조회 성공")
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    void getAllStores_Success() throws Exception {
        ResGetStoreDto dto = ResGetStoreDto.builder()
                .storeId(UUID.randomUUID())
                .name("가게")
                .status(StoreStatus.OPEN)
                .build();
        Page<ResGetStoreDto> page = new PageImpl<>(List.of(dto));
        when(storeService.getAllStores(any(StoreStatus.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/stores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("가게"));
    }

    @Test
    @DisplayName("가게 상세 조회 성공")
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    void getStore_Success() throws Exception {
        UUID storeId = UUID.randomUUID();
        ResGetStoreDto dto = ResGetStoreDto.builder()
                .storeId(storeId)
                .name("가게")
                .status(StoreStatus.OPEN)
                .build();
        when(storeService.getStore(eq(storeId))).thenReturn(dto);

        mockMvc.perform(get("/api/stores/{storeId}", storeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("가게"));
    }

    @Test
    @DisplayName("가게 상세 조회 실패 - 존재하지 않는 가게")
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    void getStore_Fail_NotFound() throws Exception {
        UUID storeId = UUID.randomUUID();
        when(storeService.getStore(eq(storeId))).thenThrow(new BusinessException(ErrorCode.STORE_NOT_FOUND));

        mockMvc.perform(get("/api/stores/{storeId}", storeId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("가게 수정 실패 - CUSTOMER 접근 금지")
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    void updateStore_Fail_CustomerAccessDenied() throws Exception {
        ReqUpdateStoreDto dto = new ReqUpdateStoreDto(
                UUID.randomUUID(), UUID.randomUUID(), "가게", "주소", "010-1234-5678", LocalTime.now(), LocalTime.now(), StoreStatus.OPEN
        );

        mockMvc.perform(put("/api/stores/{storeId}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("가게 수정 성공 - OWNER 접근 허용")
    void updateStore_Success_OwnerAccess() throws Exception {
        UUID storeId = UUID.randomUUID();
        ReqUpdateStoreDto dto = new ReqUpdateStoreDto(
                UUID.randomUUID(), UUID.randomUUID(), "수정된 가게", "주소", "010-1234-5678", LocalTime.now(), LocalTime.now(), StoreStatus.OPEN
        );
        ResGetStoreDto resDto = ResGetStoreDto.builder()
                .storeId(storeId)
                .name("수정된 가게")
                .status(StoreStatus.OPEN)
                .build();
        when(storeService.updateStore(any(ReqUpdateStoreDto.class), eq(storeId), any(Long.class), any(Role.class))).thenReturn(resDto);

        mockMvc.perform(put("/api/stores/{storeId}", storeId)
                        .with(securityContext(ownerSecurityContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("수정된 가게"));
    }

    @Test
    @DisplayName("가게 수정 실패 - 존재하지 않는 가게")
    void updateStore_Fail_NotFound() throws Exception {
        UUID storeId = UUID.randomUUID();
        ReqUpdateStoreDto dto = new ReqUpdateStoreDto(
                UUID.randomUUID(), UUID.randomUUID(), "가게", "주소", "010-1234-5678", LocalTime.now(), LocalTime.now(), StoreStatus.OPEN
        );
        when(storeService.updateStore(any(ReqUpdateStoreDto.class), eq(storeId), any(Long.class), any(Role.class)))
                .thenThrow(new BusinessException(ErrorCode.STORE_NOT_FOUND));

        mockMvc.perform(put("/api/stores/{storeId}", storeId)
                        .with(securityContext(ownerSecurityContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("가게 수정 실패 - 본인 소유의 가게가 아님")
    void updateStore_Fail_NotOwner() throws Exception {
        UUID storeId = UUID.randomUUID();
        ReqUpdateStoreDto dto = new ReqUpdateStoreDto(
                UUID.randomUUID(), UUID.randomUUID(), "가게", "주소", "010-1234-5678", LocalTime.now(), LocalTime.now(), StoreStatus.OPEN
        );
        when(storeService.updateStore(any(ReqUpdateStoreDto.class), eq(storeId), any(Long.class), any(Role.class)))
                .thenThrow(new BusinessException(ErrorCode.STORE_ACCESS_DENIED));

        mockMvc.perform(put("/api/stores/{storeId}", storeId)
                        .with(securityContext(ownerSecurityContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("가게 삭제 실패 - CUSTOMER 접근 금지")
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    void deleteStore_Fail_CustomerAccessDenied() throws Exception {
        mockMvc.perform(delete("/api/stores/{storeId}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("가게 삭제 성공 - OWNER 접근 허용")
    void deleteStore_Success_OwnerAccess() throws Exception {
        UUID storeId = UUID.randomUUID();
        ResDeleteStoreDto resDto = ResDeleteStoreDto.from(storeId, "가게");
        when(storeService.deleteStore(eq(storeId), any(Long.class), any(Role.class))).thenReturn(resDto);

        mockMvc.perform(delete("/api/stores/{storeId}", storeId)
                        .with(securityContext(ownerSecurityContext())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("가게"));
    }

    @Test
    @DisplayName("가게 삭제 실패 - 본인 소유의 가게가 아님")
    void deleteStore_Fail_NotOwner() throws Exception {
        UUID storeId = UUID.randomUUID();
        when(storeService.deleteStore(eq(storeId), any(Long.class), any(Role.class)))
                .thenThrow(new BusinessException(ErrorCode.STORE_ACCESS_DENIED));

        mockMvc.perform(delete("/api/stores/{storeId}", storeId)
                        .with(securityContext(ownerSecurityContext())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("가게 검색 성공 - keyword로 검색")
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    void searchStore_Success() throws Exception {
        ResSearchStoreDto dto = ResSearchStoreDto.builder()
                .storeId(UUID.randomUUID())
                .name("떡볶이집")
                .status(StoreStatus.OPEN)
                .build();
        Page<ResSearchStoreDto> page = new PageImpl<>(List.of(dto));
        when(storeService.searchStores(eq("떡볶이"), isNull(), any(StoreStatus.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/stores/search").param("keyword", "떡볶이"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("떡볶이집"));
    }

    @Test
    @DisplayName("가게 검색 실패 - keyword, categoryId 둘 다 없음")
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    void searchStore_Fail_NoSearchCondition() throws Exception {
        when(storeService.searchStores(isNull(), isNull(), any(StoreStatus.class), any(Pageable.class)))
                .thenThrow(new BusinessException(ErrorCode.STORE_SEARCH_CONDITION_REQUIRED));

        mockMvc.perform(get("/api/stores/search"))
                .andExpect(status().isBadRequest());
    }
}
