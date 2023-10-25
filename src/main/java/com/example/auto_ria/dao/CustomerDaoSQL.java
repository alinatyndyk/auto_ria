package com.example.auto_ria.dao;

import com.example.auto_ria.models.CustomerSQL;
import com.example.auto_ria.models.SellerSQL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CustomerDaoSQL extends JpaRepository<CustomerSQL, Integer> {
    CustomerSQL findByEmail(String email);

    @Transactional
    void deleteAllByIsActivatedFalseAndCreatedAtBefore(LocalDateTime before);
}
