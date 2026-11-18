package com.example.taskandexpensemanager.service;

import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.taskandexpensemanager.datatransferobjects.AuthResponseDto;
import com.example.taskandexpensemanager.datatransferobjects.LoginRequestDto;
import com.example.taskandexpensemanager.datatransferobjects.SignupRequestDto;
import com.example.taskandexpensemanager.entity.UserEntity;
import com.example.taskandexpensemanager.exception.ConflictException;
import com.example.taskandexpensemanager.exception.UnauthorizedException;
import com.example.taskandexpensemanager.repository.UserRepository;
import com.example.taskandexpensemanager.security.JwtService;

@Service
public class AuthService {

    private static final String INVALID_CREDENTIALS
            = "Invalid username/email or password.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(SignupRequestDto request) {
        String userId = normalizeUserId(request.userId());

        if (userRepository.existsByUserId(userId)) {
            throw userAlreadyExists();
        }

        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            // Handles the race between existsByUserId() and save().
            throw userAlreadyExists();
        }
    }

    public AuthResponseDto login(LoginRequestDto request) {
        String userId = normalizeUserId(request.userId());

        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UnauthorizedException(
                        "INVALID_CREDENTIALS",
                        INVALID_CREDENTIALS
                ));

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            throw new UnauthorizedException(
                    "INVALID_CREDENTIALS",
                    INVALID_CREDENTIALS
            );
        }

        String token = jwtService.generateToken(user);

        return new AuthResponseDto(user.getUserId(), token);
    }

    private ConflictException userAlreadyExists() {
        return new ConflictException(
                "USER_ALREADY_EXISTS",
                "That username or email is already registered."
        );
    }

    private String normalizeUserId(String userId) {
        return userId.trim().toLowerCase(Locale.ROOT);
    }
}
