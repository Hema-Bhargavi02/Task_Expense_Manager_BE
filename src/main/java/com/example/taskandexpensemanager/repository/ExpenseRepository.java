package com.example.taskandexpensemanager.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.taskandexpensemanager.entity.ExpenseEntity;

public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

    Page<ExpenseEntity> findByUserIdAndExpenseDateBetween(
            String userId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    List<ExpenseEntity>
            findByUserIdAndExpenseDateBetweenOrderByCreatedAtDescIdDesc(
                    String userId,
                    LocalDate startDate,
                    LocalDate endDate
            );

    List<ExpenseEntity>
            findByUserIdAndExpenseDateBetweenAndCategoryIgnoreCaseOrderByCreatedAtDescIdDesc(
                    String userId,
                    LocalDate startDate,
                    LocalDate endDate,
                    String category
            );

    @Query("""
            select coalesce(sum(e.amount), 0)
            from ExpenseEntity e
            where e.userId = :userId
              and e.expenseDate between :startDate and :endDate
            """)
    BigDecimal sumAmountByUserIdAndExpenseDateBetween(
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
