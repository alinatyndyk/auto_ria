package com.example.auto_ria.dao;

import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.UserSQL;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface UserDaoSQL extends JpaRepository<SellerSQL, Integer> {
    SellerSQL findSellerByEmail(String email);

    UserSQL findByEmail(String email);

    List<SellerSQL> findByCreatedAtBeforeAndIsActivatedFalse(LocalDate date);
}
