package org.example.testapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.testapi.dto.AuthResponse;
import org.example.testapi.dto.LoginRequest;
import org.example.testapi.dto.RegisterRequest;
import org.example.testapi.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody Map<String, String> request) {
        return authService.refresh(request.get("refreshToken"));
    }
}
