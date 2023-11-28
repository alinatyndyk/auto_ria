package com.example.auto_ria.dao.user;

import com.example.auto_ria.models.CarSQL;
import com.example.auto_ria.models.user.SellerSQL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface UserDaoSQL extends JpaRepository<SellerSQL, Integer> {
//    SellerSQL findSellerByEmail(String email);
    SellerSQL findByEmail(String email);

    @Query("SELECT p FROM SellerSQL p WHERE p.email = :email")
    SellerSQL findSellerByEmail(@Param("email") String email);

    SellerSQL findByPaymentSource(String paymentSource);

    SellerSQL findSellerByNumber(String number);

    @Transactional
    void deleteAllByIsActivatedFalseAndCreatedAtBefore(LocalDateTime before);
}
