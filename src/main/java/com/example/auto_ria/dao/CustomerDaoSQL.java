package com.example.auto_ria.dao;

import com.example.auto_ria.models.CustomerSQL;
import com.example.auto_ria.models.SellerSQL;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CustomerDaoSQL extends JpaRepository<CustomerSQL, Integer> {
    CustomerSQL findByEmail(String email);

    List<CustomerSQL> findByCreatedAtBeforeAndIsActivatedFalse(LocalDate date);
}
