package com.palak.expensemanager.repository;

import com.palak.expensemanager.entity.ExpenseGroup;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseGroupRepository extends JpaRepository<ExpenseGroup, Long> {

    Optional<ExpenseGroup> findByNameIgnoreCase(String name);
}
