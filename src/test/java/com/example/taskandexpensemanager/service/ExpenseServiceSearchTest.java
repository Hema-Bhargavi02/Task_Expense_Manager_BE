package com.example.taskandexpensemanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.taskandexpensemanager.entity.ExpenseEntity;
import com.example.taskandexpensemanager.repository.ExpenseRepository;

class ExpenseServiceSearchTest {

    private static final LocalDate START = LocalDate.of(1970, 1, 1);
    private static final LocalDate END = LocalDate.of(9999, 12, 31);

    @Test
    void blankCategorySearchesEveryExpenseInTheRange() {

        ExpenseRepository repository = mock(ExpenseRepository.class);
        ExpenseService service = new ExpenseService(repository);

        ExpenseEntity expense = new ExpenseEntity();

        when(repository
                .findByUserIdAndExpenseDateBetweenOrderByCreatedAtDescIdDesc(
                        "alice", START, END))
                .thenReturn(List.of(expense));

        List<ExpenseEntity> matches
                = service.searchExpenses("alice", "  ", START, END);

        assertEquals(List.of(expense), matches);

        verify(repository)
                .findByUserIdAndExpenseDateBetweenOrderByCreatedAtDescIdDesc(
                        "alice", START, END
                );
    }

    @Test
    void mentionedCategoryUsesTheCategoryFinder() {

        ExpenseRepository repository = mock(ExpenseRepository.class);
        ExpenseService service = new ExpenseService(repository);

        ExpenseEntity expense = new ExpenseEntity();

        when(repository
                .findByUserIdAndExpenseDateBetweenAndCategoryIgnoreCaseOrderByCreatedAtDescIdDesc(
                        "alice", START, END, "Food"))
                .thenReturn(List.of(expense));

        List<ExpenseEntity> matches
                = service.searchExpenses("alice", " Food ", START, END);

        assertEquals(List.of(expense), matches);

        verify(repository)
                .findByUserIdAndExpenseDateBetweenAndCategoryIgnoreCaseOrderByCreatedAtDescIdDesc(
                        "alice", START, END, "Food"
                );

        verify(repository, never())
                .findByUserIdAndExpenseDateBetweenOrderByCreatedAtDescIdDesc(
                        "alice", START, END
                );
    }
}
