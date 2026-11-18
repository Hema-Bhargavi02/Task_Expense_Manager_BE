package com.example.taskandexpensemanager.datatransferobjects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequestDto(
        @NotBlank
        @Size(max = 100)
        String userId,
        @NotBlank
        @Size(min = 8, max = 72)
        String password) {

}
