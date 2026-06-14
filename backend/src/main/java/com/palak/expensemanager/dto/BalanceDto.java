package com.palak.expensemanager.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceDto {

    private Long userId;
    private String userName;
    private BigDecimal totalPaidInr;
    private BigDecimal totalOwedInr;
    private BigDecimal netBalanceInr;
}
