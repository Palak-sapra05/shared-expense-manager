package com.palak.expensemanager.controller;

import com.palak.expensemanager.entity.ExpenseGroup;
import com.palak.expensemanager.repository.ExpenseGroupRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/groups")
public class ExpenseGroupController {

    private final ExpenseGroupRepository expenseGroupRepository;

    public ExpenseGroupController(ExpenseGroupRepository expenseGroupRepository) {
        this.expenseGroupRepository = expenseGroupRepository;
    }

    @PostMapping
    public ResponseEntity<ExpenseGroup> createGroup(@RequestBody ExpenseGroup expenseGroup) {
        ExpenseGroup savedGroup = expenseGroupRepository.save(expenseGroup);
        return ResponseEntity.ok(savedGroup);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseGroup>> getAllGroups() {
        List<ExpenseGroup> groups = expenseGroupRepository.findAll();
        return ResponseEntity.ok(groups);
    }
}
