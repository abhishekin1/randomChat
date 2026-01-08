package com.randomchat.chat_backend.security;


import com.randomchat.chat_backend.dto.LoginRequestDto;
import com.randomchat.chat_backend.dto.LoginResponseDto;
import com.randomchat.chat_backend.dto.SignupRequestDto;
import com.randomchat.chat_backend.model.User;
import com.randomchat.chat_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AuthUtil authUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword())
        );

        User user = (User) authentication.getPrincipal();

        String token = authUtil.generateAccessToken(user);

        return new LoginResponseDto(token, user.getUsername());
    }

    public LoginResponseDto signup(final SignupRequestDto signupRequestDto) {
        User user = userRepository.findByUsername(signupRequestDto.getUsername()).orElse(null);
        if(user != null){
            log.info("user already exist: {}", user);
            throw new IllegalArgumentException("User already exists");
        }

        user = userRepository.save(User.builder()
                .username(signupRequestDto.getUsername())
                .password(passwordEncoder.encode(signupRequestDto.getPassword()))
                .name(signupRequestDto.getName())
                .email(signupRequestDto.getEmail())
                .gender(signupRequestDto.getGender())
                .bio(signupRequestDto.getBio())
                .photoUrl(signupRequestDto.getPhotoUrl())
                .build()
        );
        
        // Automatically login after signup
        String token = authUtil.generateAccessToken(user);
        return new LoginResponseDto(token, user.getUsername());
    }
}
