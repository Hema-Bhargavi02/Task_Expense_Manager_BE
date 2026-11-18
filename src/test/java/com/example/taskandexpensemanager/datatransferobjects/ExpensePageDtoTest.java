package com.example.taskandexpensemanager.datatransferobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.example.taskandexpensemanager.entity.ExpenseEntity;

class ExpensePageDtoTest {

    @Test
    void middlePageExposesEnvelopeAndHasNext() {

        ExpensePageDto dto = ExpensePageDto.from(
                pageOf(10, 1, 37),
                new BigDecimal("1234.50")
        );

        assertEquals(1, dto.page());
        assertEquals(10, dto.size());
        assertEquals(37, dto.totalItems());
        assertEquals(4, dto.totalPages());
        assertEquals(10, dto.items().size());
        assertTrue(dto.hasNext());
        assertEquals(
                new BigDecimal("1234.50"),
                dto.totalAmount()
        );
    }

    @Test
    void lastPageHasNoNext() {

        ExpensePageDto dto = ExpensePageDto.from(
                pageOf(7, 3, 37),
                new BigDecimal("37.00")
        );

        assertEquals(3, dto.page());
        assertEquals(7, dto.items().size());
        assertFalse(dto.hasNext());
    }

    @Test
    void emptyResultHasNoPages() {

        ExpensePageDto dto = ExpensePageDto.from(
                pageOf(0, 0, 0),
                BigDecimal.ZERO
        );

        assertEquals(0, dto.totalItems());
        assertEquals(0, dto.totalPages());
        assertTrue(dto.items().isEmpty());
        assertFalse(dto.hasNext());
    }

    private Page<ExpenseEntity> pageOf(
            int itemCount,
            int pageNumber,
            long totalItems) {

        List<ExpenseEntity> items = new ArrayList<>();

        for (int index = 0; index < itemCount; index++) {
            items.add(new ExpenseEntity());
        }

        return new PageImpl<>(
                items,
                PageRequest.of(pageNumber, 10),
                totalItems
        );
    }
}
