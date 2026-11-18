package com.example.taskandexpensemanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.taskandexpensemanager.entity.ExpenseEntity;
import com.example.taskandexpensemanager.repository.ExpenseRepository;

class ExpenseServicePaginationTest {

    private static final LocalDate START = LocalDate.of(2026, 11, 1);
    private static final LocalDate END = LocalDate.of(2026, 11, 30);

    @Test
    void requestsTheGivenPageOrderedByCreatedAtDescending() {

        ExpenseRepository repository = mock(ExpenseRepository.class);
        ExpenseService service = new ExpenseService(repository);

        Page<ExpenseEntity> emptyPage = new PageImpl<>(List.of());

        when(repository.findByUserIdAndExpenseDateBetween(
                anyString(),
                any(LocalDate.class),
                any(LocalDate.class),
                any(Pageable.class)))
                .thenReturn(emptyPage);

        service.getExpensesByPeriod("alice", START, END, 2, 10);

        ArgumentCaptor<Pageable> captor
                = ArgumentCaptor.forClass(Pageable.class);

        verify(repository).findByUserIdAndExpenseDateBetween(
                eq("alice"),
                eq(START),
                eq(END),
                captor.capture()
        );

        Pageable pageable = captor.getValue();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());

        Sort.Order createdAt
                = pageable.getSort().getOrderFor("createdAt");
        Sort.Order id = pageable.getSort().getOrderFor("id");

        assertNotNull(createdAt, "createdAt must be part of the sort");
        assertNotNull(id, "id must break ties so paging is stable");

        assertEquals(
                Sort.Direction.DESC,
                createdAt.getDirection(),
                "expenses must be returned newest-added first"
        );
        assertEquals(Sort.Direction.DESC, id.getDirection());

        assertEquals(
                List.of("createdAt", "id"),
                pageable.getSort().stream()
                        .map(Sort.Order::getProperty)
                        .toList(),
                "createdAt must be the primary sort key"
        );
    }
}
