package com.example.taskandexpensemanager.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taskandexpensemanager.datatransferobjects.ExpenseRequestDto;
import com.example.taskandexpensemanager.entity.ExpenseEntity;
import com.example.taskandexpensemanager.exception.NotFoundException;
import com.example.taskandexpensemanager.repository.ExpenseRepository;

@Service
public class ExpenseService {

    // Newest-added first. `id` breaks ties so paging stays stable even
    // when several rows share the same `created_at` timestamp.
    private static final Sort EXPENSE_SORT = Sort
            .by(Sort.Direction.DESC, "createdAt")
            .and(Sort.by(Sort.Direction.DESC, "id"));

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public ExpenseEntity addExpense(
            ExpenseRequestDto request,
            String userId) {

        ExpenseEntity expense = new ExpenseEntity();

        // User ID comes from the authenticated JWT
        expense.setUserId(userId);

        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());

        expense.setExpenseDate(
                request.getExpenseDate() != null
                ? request.getExpenseDate()
                : LocalDate.now()
        );

        return expenseRepository.save(expense);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseEntity> getExpensesByPeriod(
            String userId,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                EXPENSE_SORT
        );

        return expenseRepository.findByUserIdAndExpenseDateBetween(
                userId,
                startDate,
                endDate,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public BigDecimal getExpenseTotalByPeriod(
            String userId,
            LocalDate startDate,
            LocalDate endDate) {

        BigDecimal total
                = expenseRepository.sumAmountByUserIdAndExpenseDateBetween(
                        userId,
                        startDate,
                        endDate
                );

        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public List<ExpenseEntity> searchExpenses(
            String userId,
            String category,
            LocalDate startDate,
            LocalDate endDate) {

        String normalizedCategory
                = category == null ? "" : category.trim();

        if (normalizedCategory.isEmpty()) {
            return expenseRepository
                    .findByUserIdAndExpenseDateBetweenOrderByCreatedAtDescIdDesc(
                            userId,
                            startDate,
                            endDate
                    );
        }

        return expenseRepository
                .findByUserIdAndExpenseDateBetweenAndCategoryIgnoreCaseOrderByCreatedAtDescIdDesc(
                        userId,
                        startDate,
                        endDate,
                        normalizedCategory
                );
    }

    @Transactional
    public void deleteExpense(Long expenseId, String userId) {

        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .filter(found -> userId.equals(found.getUserId()))
                .orElseThrow(() -> new NotFoundException(
                        "EXPENSE_NOT_FOUND",
                        "Expense not found for this user."
                ));

        expenseRepository.delete(expense);
    }
}
