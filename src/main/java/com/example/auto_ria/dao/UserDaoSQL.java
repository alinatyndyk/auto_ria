package com.example.auto_ria.dao;

import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.UserSQL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface UserDaoSQL extends JpaRepository<SellerSQL, Integer> {
    SellerSQL findSellerByEmail(String email);

    @Transactional
    void deleteAllByIsActivatedFalseAndCreatedAtBefore(LocalDateTime before);

    UserSQL findByEmail(String email);
}
