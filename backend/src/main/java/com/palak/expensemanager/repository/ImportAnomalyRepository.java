package com.palak.expensemanager.repository;

import com.palak.expensemanager.entity.ImportAnomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportAnomalyRepository extends JpaRepository<ImportAnomaly, Long> {
}
