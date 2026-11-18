package com.example.taskandexpensemanager.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taskandexpensemanager.datatransferobjects.ExpensePageDto;
import com.example.taskandexpensemanager.datatransferobjects.ExpenseRequestDto;
import com.example.taskandexpensemanager.entity.ExpenseEntity;
import com.example.taskandexpensemanager.exception.BadRequestException;
import com.example.taskandexpensemanager.service.ExpenseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/expense")
public class ExpenseController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping("/add")
    public ResponseEntity<ExpenseEntity> addExpense(
            @Valid @RequestBody ExpenseRequestDto request,
            Authentication authentication) {

        String userId = authentication.getName();

        ExpenseEntity savedExpense
                = expenseService.addExpense(request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedExpense);
    }

    @GetMapping("/period")
    public ResponseEntity<ExpensePageDto> getExpensesByPeriod(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        if (startDate.isAfter(endDate)) {
            throw new BadRequestException(
                    "INVALID_DATE_RANGE",
                    "startDate must be before or equal to endDate."
            );
        }

        String userId = authentication.getName();

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Page<ExpenseEntity> expenses
                = expenseService.getExpensesByPeriod(
                        userId,
                        startDate,
                        endDate,
                        safePage,
                        safeSize
                );

        BigDecimal totalAmount
                = expenseService.getExpenseTotalByPeriod(
                        userId,
                        startDate,
                        endDate
                );

        return ResponseEntity.ok(
                ExpensePageDto.from(expenses, totalAmount)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<ExpenseEntity>> searchExpenses(
            @RequestParam(defaultValue = "") String category,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Authentication authentication) {

        // The delete flow searches the user's whole history unless
        // the request narrows it down.
        LocalDate safeStart = startDate != null
                ? startDate
                : LocalDate.of(1970, 1, 1);

        LocalDate safeEnd = endDate != null
                ? endDate
                : LocalDate.of(9999, 12, 31);

        if (safeStart.isAfter(safeEnd)) {
            throw new BadRequestException(
                    "INVALID_DATE_RANGE",
                    "startDate must be before or equal to endDate."
            );
        }

        return ResponseEntity.ok(
                expenseService.searchExpenses(
                        authentication.getName(),
                        category,
                        safeStart,
                        safeEnd
                )
        );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteExpense(
            @PathVariable Long id,
            Authentication authentication) {

        expenseService.deleteExpense(id, authentication.getName());

        return ResponseEntity.ok(Map.of(
                "message", "Expense deleted successfully."
        ));
    }
}
