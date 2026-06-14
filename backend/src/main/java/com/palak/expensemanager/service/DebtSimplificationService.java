package com.palak.expensemanager.service;

import com.palak.expensemanager.dto.SimplifiedSettlementDto;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DebtSimplificationService {

    public List<SimplifiedSettlementDto> simplifyBalances(Map<Long, BigDecimal> balances, Map<Long, String> names) {
        List<SimplifiedSettlementDto> result = new ArrayList<>();

        List<BalanceItem> creditors = balances.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0)
                .map(entry -> new BalanceItem(entry.getKey(), names.get(entry.getKey()), entry.getValue()))
                .sorted(Comparator.comparing(BalanceItem::getAmount).reversed())
                .collect(Collectors.toList());

        List<BalanceItem> debtors = balances.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) < 0)
                .map(entry -> new BalanceItem(entry.getKey(), names.get(entry.getKey()), entry.getValue().abs()))
                .sorted(Comparator.comparing(BalanceItem::getAmount).reversed())
                .collect(Collectors.toList());

        int creditorIndex = 0;
        int debtorIndex = 0;

        while (creditorIndex < creditors.size() && debtorIndex < debtors.size()) {
            BalanceItem creditor = creditors.get(creditorIndex);
            BalanceItem debtor = debtors.get(debtorIndex);
            BigDecimal settlementAmount = creditor.amount.min(debtor.amount);

            result.add(new SimplifiedSettlementDto(
                    debtor.userId,
                    debtor.userName,
                    creditor.userId,
                    creditor.userName,
                    settlementAmount
            ));

            creditor.amount = creditor.amount.subtract(settlementAmount);
            debtor.amount = debtor.amount.subtract(settlementAmount);

            if (creditor.amount.compareTo(BigDecimal.ZERO) == 0) {
                creditorIndex++;
            }
            if (debtor.amount.compareTo(BigDecimal.ZERO) == 0) {
                debtorIndex++;
            }
        }

        return result;
    }

    private static class BalanceItem {
        private final Long userId;
        private final String userName;
        private BigDecimal amount;

        public BalanceItem(Long userId, String userName, BigDecimal amount) {
            this.userId = userId;
            this.userName = userName;
            this.amount = amount;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUserName() {
            return userName;
        }

        public BigDecimal getAmount() {
            return amount;
        }
    }
}
