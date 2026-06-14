package com.palak.expensemanager.service;

import com.palak.expensemanager.dto.BalanceDto;
import com.palak.expensemanager.dto.SimplifiedSettlementDto;
import com.palak.expensemanager.entity.Expense;
import com.palak.expensemanager.entity.GroupMembership;
import com.palak.expensemanager.entity.Settlement;
import com.palak.expensemanager.entity.User;
import com.palak.expensemanager.repository.ExpenseRepository;
import com.palak.expensemanager.repository.GroupMembershipRepository;
import com.palak.expensemanager.repository.SettlementRepository;
import com.palak.expensemanager.repository.UserRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class BalanceService {

    private final ExpenseRepository expenseRepository;
    private final SettlementRepository settlementRepository;
    private final GroupMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final DebtSimplificationService debtSimplificationService;

    private static final BigDecimal USD_TO_INR = new BigDecimal("82.50");

    public BalanceService(
            ExpenseRepository expenseRepository,
            SettlementRepository settlementRepository,
            GroupMembershipRepository membershipRepository,
            UserRepository userRepository,
            DebtSimplificationService debtSimplificationService) {
        this.expenseRepository = expenseRepository;
        this.settlementRepository = settlementRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.debtSimplificationService = debtSimplificationService;
    }

    public List<BalanceDto> calculateGroupBalances(Long groupId) {
        Map<Long, BigDecimal> paidMap = new HashMap<>();
        Map<Long, BigDecimal> owedMap = new HashMap<>();

        List<Expense> expenses = expenseRepository.findAll().stream()
                .filter(expense -> expense.getGroup() != null && expense.getGroup().getId().equals(groupId))
                .collect(Collectors.toList());

        for (Expense expense : expenses) {
            BigDecimal amountInr = convertToInr(expense.getAmount(), expense.getCurrency());
            Long payerId = expense.getPaidBy().getId();
            paidMap.put(payerId, paidMap.getOrDefault(payerId, BigDecimal.ZERO).add(amountInr));

            List<GroupMembership> memberships = membershipRepository.findByUserAndExpenseGroup(expense.getPaidBy(), expense.getGroup());
            int participantsCount = 1;
            if (!memberships.isEmpty()) {
                participantsCount = memberships.size();
            }
            BigDecimal share = amountInr.divide(BigDecimal.valueOf(participantsCount), 2, BigDecimal.ROUND_HALF_UP);
            for (GroupMembership membership : memberships) {
                if (isMemberActiveOnDate(membership, expense.getExpenseDate())) {
                    Long participantId = membership.getUser().getId();
                    owedMap.put(participantId, owedMap.getOrDefault(participantId, BigDecimal.ZERO).add(share));
                }
            }
        }

        List<Settlement> settlements = settlementRepository.findAll();
        for (Settlement settlement : settlements) {
            Long payerId = settlement.getPayer().getId();
            Long receiverId = settlement.getReceiver().getId();
            BigDecimal amount = convertToInr(settlement.getAmount(), "INR");
            paidMap.put(receiverId, paidMap.getOrDefault(receiverId, BigDecimal.ZERO).add(amount));
            owedMap.put(payerId, owedMap.getOrDefault(payerId, BigDecimal.ZERO).add(amount));
        }

        Map<Long, BigDecimal> netMap = new HashMap<>();
        for (Long userId : union(paidMap.keySet(), owedMap.keySet())) {
            BigDecimal paid = paidMap.getOrDefault(userId, BigDecimal.ZERO);
            BigDecimal owed = owedMap.getOrDefault(userId, BigDecimal.ZERO);
            netMap.put(userId, paid.subtract(owed));
        }

        return netMap.entrySet().stream()
                .map(entry -> {
                    User user = userRepository.findById(entry.getKey()).orElseThrow();
                    BigDecimal totalPaid = paidMap.getOrDefault(entry.getKey(), BigDecimal.ZERO);
                    BigDecimal totalOwed = owedMap.getOrDefault(entry.getKey(), BigDecimal.ZERO);
                    return new BalanceDto(entry.getKey(), user.getName(), totalPaid, totalOwed, entry.getValue());
                })
                .collect(Collectors.toList());
    }

    public Map<Long, BigDecimal> calculateNetBalances(Long groupId) {
        return calculateGroupBalances(groupId).stream()
                .collect(Collectors.toMap(BalanceDto::getUserId, BalanceDto::getNetBalanceInr));
    }

    public List<SimplifiedSettlementDto> calculateSimplifiedSettlements(Long groupId) {
        Map<Long, BigDecimal> balances = calculateNetBalances(groupId);
        Map<Long, String> names = balances.keySet().stream()
                .collect(Collectors.toMap(id -> id, id -> userRepository.findById(id).orElseThrow().getName()));
        return debtSimplificationService.simplifyBalances(balances, names);
    }

    private BigDecimal convertToInr(BigDecimal amount, String currency) {
        if (currency == null || currency.isBlank() || currency.equalsIgnoreCase("INR")) {
            return amount;
        }
        if (currency.equalsIgnoreCase("USD")) {
            return amount.multiply(USD_TO_INR);
        }
        return amount;
    }

    private boolean isMemberActiveOnDate(GroupMembership membership, java.time.LocalDate expenseDate) {
        if (membership.getJoinedAt() != null && expenseDate.isBefore(membership.getJoinedAt())) {
            return false;
        }
        if (membership.getLeftAt() != null && expenseDate.isAfter(membership.getLeftAt())) {
            return false;
        }
        return true;
    }

    private static <T> List<T> union(java.util.Set<T> a, java.util.Set<T> b) {
        List<T> union = new java.util.ArrayList<>(a);
        for (T item : b) {
            if (!union.contains(item)) {
                union.add(item);
            }
        }
        return union;
    }
}
