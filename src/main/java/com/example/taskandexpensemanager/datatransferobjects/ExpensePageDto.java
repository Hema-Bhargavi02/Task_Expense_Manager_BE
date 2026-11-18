package com.example.taskandexpensemanager.datatransferobjects;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;

import com.example.taskandexpensemanager.entity.ExpenseEntity;

public record ExpensePageDto(
        List<ExpenseEntity> items,
        int page,
        int size,
        long totalItems,
        int totalPages,
        boolean hasNext,
        BigDecimal totalAmount) {

    public static ExpensePageDto from(
            Page<ExpenseEntity> result,
            BigDecimal totalAmount) {

        return new ExpensePageDto(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext(),
                totalAmount != null ? totalAmount : BigDecimal.ZERO
        );
    }
}
