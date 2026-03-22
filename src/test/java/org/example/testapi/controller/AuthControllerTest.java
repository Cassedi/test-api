package org.example.testapi.controller;

import org.example.testapi.dto.AuthResponse;
import org.example.testapi.dto.RegisterRequest;
import org.example.testapi.security.JwtAuthenticationFilter;
import org.example.testapi.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // ======================== REGISTER ========================

    @Test
    void shouldRegisterSuccessfully() throws Exception {
        AuthResponse response = new AuthResponse("access-token", "refresh-token");
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "user@test.com", "password": "secret123", "confirmPassword": "secret123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", is("access-token")))
                .andExpect(jsonPath("$.refreshToken", is("refresh-token")));
    }

    @Test
    void shouldReturn422WhenRegisterEmailIsBlank() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "", "password": "secret123", "confirmPassword": "secret123"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)));
    }

    @Test
    void shouldReturn422WhenRegisterEmailInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "not-an-email", "password": "secret123", "confirmPassword": "secret123"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.message", is("Field 'email' must be a valid email address")));
    }

    @Test
    void shouldReturn422WhenRegisterPasswordIsBlank() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "user@test.com", "password": "", "confirmPassword": "secret123"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)));
    }

    @Test
    void shouldReturn422WhenRegisterPasswordTooShort() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "user@test.com", "password": "12345", "confirmPassword": "12345"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.message", is("Password must be at least 6 characters")));
    }

    @Test
    void shouldReturn422WhenConfirmPasswordIsBlank() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "user@test.com", "password": "secret123", "confirmPassword": ""}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)));
    }

    @Test
    void shouldReturn422WhenPasswordsDoNotMatch() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Passwords do not match"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "user@test.com", "password": "secret123", "confirmPassword": "different"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.message", is("Passwords do not match")));
    }

    @Test
    void shouldReturn409WhenEmailAlreadyRegistered() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "taken@test.com", "password": "secret123", "confirmPassword": "secret123"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", is("Email is already registered")));
    }

    // ======================== LOGIN ========================

    @Test
    void shouldLoginSuccessfully() throws Exception {
        AuthResponse response = new AuthResponse("access-token", "refresh-token");
        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "user@test.com", "password": "secret123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("access-token")))
                .andExpect(jsonPath("$.refreshToken", is("refresh-token")));
    }

    @Test
    void shouldReturn422WhenLoginEmailIsBlank() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "", "password": "secret123"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)));
    }

    @Test
    void shouldReturn422WhenLoginPasswordIsBlank() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "user@test.com", "password": ""}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)));
    }

    @Test
    void shouldReturn401WhenCredentialsInvalid() throws Exception {
        when(authService.login(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "user@test.com", "password": "wrongpass"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("Invalid email or password")));
    }

    // ======================== REFRESH ========================

    @Test
    void shouldRefreshTokensSuccessfully() throws Exception {
        AuthResponse response = new AuthResponse("new-access-token", "new-refresh-token");
        when(authService.refresh(eq("valid-refresh-token"))).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "valid-refresh-token"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("new-access-token")))
                .andExpect(jsonPath("$.refreshToken", is("new-refresh-token")));
    }

    @Test
    void shouldReturn401WhenRefreshTokenInvalid() throws Exception {
        when(authService.refresh(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "invalid-token"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("Invalid refresh token")));
    }
}
