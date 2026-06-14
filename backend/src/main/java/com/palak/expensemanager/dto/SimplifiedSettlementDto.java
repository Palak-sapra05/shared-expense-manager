package com.palak.expensemanager.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimplifiedSettlementDto {

    private Long payerId;
    private String payerName;
    private Long receiverId;
    private String receiverName;
    private BigDecimal amountInr;
}
