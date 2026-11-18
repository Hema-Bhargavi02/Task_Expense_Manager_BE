package com.example.taskandexpensemanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.example.taskandexpensemanager.repository.ExpenseRepository;

class ExpenseServiceTotalTest {

    private static final LocalDate START = LocalDate.of(2026, 11, 1);
    private static final LocalDate END = LocalDate.of(2026, 11, 30);

    @Test
    void returnsTheSumForTheWholePeriod() {

        ExpenseRepository repository = mock(ExpenseRepository.class);
        ExpenseService service = new ExpenseService(repository);

        when(repository.sumAmountByUserIdAndExpenseDateBetween(
                "alice", START, END))
                .thenReturn(new BigDecimal("1234.50"));

        BigDecimal total
                = service.getExpenseTotalByPeriod("alice", START, END);

        assertEquals(new BigDecimal("1234.50"), total);

        verify(repository).sumAmountByUserIdAndExpenseDateBetween(
                "alice", START, END
        );
    }

    @Test
    void returnsZeroWhenThereAreNoExpenses() {

        ExpenseRepository repository = mock(ExpenseRepository.class);
        ExpenseService service = new ExpenseService(repository);

        when(repository.sumAmountByUserIdAndExpenseDateBetween(
                "alice", START, END))
                .thenReturn(null);

        assertEquals(
                BigDecimal.ZERO,
                service.getExpenseTotalByPeriod("alice", START, END)
        );
    }
}
