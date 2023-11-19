package com.example.auto_ria.dao.user;

import com.example.auto_ria.models.user.SellerSQL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface UserDaoSQL extends JpaRepository<SellerSQL, Integer> {
    SellerSQL findSellerByEmail(String email);
    SellerSQL findByPaymentSource(String paymentSource);

    SellerSQL findSellerByNumber(String number);

    @Transactional
    void deleteAllByIsActivatedFalseAndCreatedAtBefore(LocalDateTime before);
}
