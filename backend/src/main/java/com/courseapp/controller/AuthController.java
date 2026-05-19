package com.courseapp.controller;

import com.courseapp.dto.LoginRequest;
import com.courseapp.dto.RegisterRequest;
import com.courseapp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        Map<String, Object> result = authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        Map<String, Object> result = authService.login(req);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var user = (com.courseapp.security.UserPrincipal) auth.getPrincipal();
        Map<String, Object> result = authService.me(user.userId());
        return ResponseEntity.ok(result);
    }
}