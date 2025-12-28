package com.randomchat.chat_backend.controller;


import com.randomchat.chat_backend.dto.LoginRequestDto;
import com.randomchat.chat_backend.dto.LoginResponseDto;
import com.randomchat.chat_backend.security.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(authService.login(loginRequestDto));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody LoginRequestDto signupRequestDto) {

            LoginResponseDto response = authService.signup(signupRequestDto);
            return ResponseEntity.ok(response);
    }
}
