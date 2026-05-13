package io.github.mirvmir.identity.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mirvmir.identity.application.CustomUserDetails;
import io.github.mirvmir.identity.application.service.dto.LoginDto;
import io.github.mirvmir.identity.application.service.dto.RefreshDto;
import io.github.mirvmir.identity.application.service.dto.RegisterDto;
import io.github.mirvmir.identity.application.service.interfaces.AuthService;
import io.github.mirvmir.identity.application.service.interfaces.RefreshService;
import io.github.mirvmir.identity.domain.User;
import io.github.mirvmir.identity.dto.TokenDto;
import io.github.mirvmir.identity.web.request.LoginRq;
import io.github.mirvmir.identity.web.request.RegisterRq;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private RefreshService refreshService;
    private AuthService authService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        refreshService = mock(RefreshService.class);
        authService = mock(AuthService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(refreshService, authService))
                .build();

        objectMapper = new ObjectMapper();

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ping_shouldReturnAuthOk() throws Exception {
        mockMvc.perform(get("/auth/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("auth ok"));
    }

    @Test
    void login_shouldReturnAccessTokenAndSetRefreshCookie() throws Exception {
        LoginRq request = new LoginRq("user@mail.ru", "password");

        User user = User.createNewCustomer("hash", "user@mail.ru");
        user.assignId(1L);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        when(authService.login(any(LoginRq.class)))
                .thenReturn(new LoginDto(authentication));

        when(authService.generateToken(
                eq(1L),
                eq(authentication.getAuthorities()),
                eq(false)
        )).thenReturn(new TokenDto("access-token", "refresh-token"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=refresh-token")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("HttpOnly")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Secure")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("SameSite=Strict")));

        verify(authService).login(argThat(actual ->
                actual.email().equals("user@mail.ru")
                && actual.password().equals("password")
        ));

        verify(authService).generateToken(
                eq(1L),
                eq(authentication.getAuthorities()),
                eq(false)
        );

        verify(authService, never()).logoutCurrentSession(any());
    }

    @Test
    void login_shouldLogoutCurrentSession_whenAlreadyAuthenticated() throws Exception {
        LoginRq request = new LoginRq("user@mail.ru", "password");

        User oldUser = User.createNewCustomer("hash", "old@mail.ru");
        oldUser.assignId(99L);

        CustomUserDetails oldUserDetails = new CustomUserDetails(oldUser);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        oldUserDetails,
                        null,
                        oldUserDetails.getAuthorities()
                )
        );

        User user = User.createNewCustomer("hash", "user@mail.ru");
        user.assignId(1L);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        when(authService.login(any(LoginRq.class)))
                .thenReturn(new LoginDto(authentication));

        when(authService.generateToken(anyLong(), anyCollection(), anyBoolean()))
                .thenReturn(new TokenDto("access-token", "new-refresh-token"));

        mockMvc.perform(post("/auth/login")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "old-refresh-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));

        verify(authService).logoutCurrentSession("old-refresh-token");
    }

    @Test
    void register_shouldReturnAccessTokenAndSetRefreshCookie() throws Exception {
        RegisterRq request = new RegisterRq("new@mail.ru", "password");

        User user = User.createNewCustomer("hash", "new@mail.ru");
        user.assignId(1L);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        when(authService.register(any(RegisterRq.class)))
                .thenReturn(new RegisterDto(authentication));

        when(authService.generateToken(
                eq(1L),
                eq(authentication.getAuthorities()),
                eq(false)
        )).thenReturn(new TokenDto("access-token", "refresh-token"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=refresh-token")));

        verify(authService).register(argThat(actual ->
                actual.email().equals("new@mail.ru")
                && actual.password().equals("password")
        ));
    }

    @Test
    void refresh_shouldReturnNewAccessTokenAndSetNewRefreshCookie() throws Exception {
        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_USER"));

        when(refreshService.execute("old-refresh-token"))
                .thenReturn(new RefreshDto(
                        1L,
                        authorities,
                        true
                ));

        when(authService.generateToken(1L, authorities, true))
                .thenReturn(new TokenDto("new-access-token", "new-refresh-token"));

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "old-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=new-refresh-token")));

        verify(refreshService).execute("old-refresh-token");
        verify(authService).generateToken(1L, authorities, true);
    }
}