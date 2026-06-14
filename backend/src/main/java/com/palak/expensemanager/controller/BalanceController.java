package com.palak.expensemanager.controller;

import com.palak.expensemanager.dto.BalanceDto;
import com.palak.expensemanager.dto.SimplifiedSettlementDto;
import com.palak.expensemanager.service.BalanceService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/balances")
public class BalanceController {

    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<BalanceDto>> getGroupBalances(@PathVariable Long groupId) {
        List<BalanceDto> balances = balanceService.calculateGroupBalances(groupId);
        return ResponseEntity.ok(balances);
    }

    @GetMapping("/summary/{groupId}")
    public ResponseEntity<List<BalanceDto>> getBalanceSummary(@PathVariable Long groupId) {
        List<BalanceDto> balances = balanceService.calculateGroupBalances(groupId);
        return ResponseEntity.ok(balances);
    }

    @GetMapping("/simplified/{groupId}")
    public ResponseEntity<List<SimplifiedSettlementDto>> getSimplifiedSettlements(@PathVariable Long groupId) {
        List<SimplifiedSettlementDto> settlements = balanceService.calculateSimplifiedSettlements(groupId);
        return ResponseEntity.ok(settlements);
    }
}
