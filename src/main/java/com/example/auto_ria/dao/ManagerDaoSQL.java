package com.example.auto_ria.dao;

import com.example.auto_ria.models.ManagerSQL;
import com.example.auto_ria.models.SellerSQL;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ManagerDaoSQL extends JpaRepository<ManagerSQL, Integer> {

    ManagerSQL findByEmail(String email);

    List<ManagerSQL> findByCreatedAtBeforeAndIsActivatedFalse(LocalDate date);
}
