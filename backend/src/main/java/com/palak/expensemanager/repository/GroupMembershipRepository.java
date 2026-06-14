package com.palak.expensemanager.repository;

import com.palak.expensemanager.entity.ExpenseGroup;
import com.palak.expensemanager.entity.GroupMembership;
import com.palak.expensemanager.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {

    List<GroupMembership> findByUserAndExpenseGroup(User user, ExpenseGroup expenseGroup);
}
