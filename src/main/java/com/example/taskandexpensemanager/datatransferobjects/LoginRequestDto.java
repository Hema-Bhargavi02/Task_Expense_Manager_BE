package com.example.taskandexpensemanager.datatransferobjects;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank
        String userId,
        @NotBlank
        String password) {}
