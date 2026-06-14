package com.palak.expensemanager.repository;

import com.palak.expensemanager.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByNameIgnoreCase(String name);
}
