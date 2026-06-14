package com.palak.expensemanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CsvRecord {

    private int rowNumber;
    private String description;
    private BigDecimal amount;
    private String currency;
    private String paidBy;
    private String group;
    private String participants;
    private LocalDate expenseDate;
}
