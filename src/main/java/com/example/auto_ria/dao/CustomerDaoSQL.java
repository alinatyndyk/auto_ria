package com.example.auto_ria.dao;

import com.example.auto_ria.models.CustomerSQL;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerDaoSQL extends JpaRepository<CustomerSQL, Integer> {
    CustomerSQL findByEmail(String email);
}
