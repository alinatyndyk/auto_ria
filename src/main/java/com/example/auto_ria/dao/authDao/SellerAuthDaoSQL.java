package com.example.auto_ria.dao.authDao;

import com.example.auto_ria.models.auth.AuthSQL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface SellerAuthDaoSQL extends JpaRepository<AuthSQL, Integer> {
    void deleteAllByPersonId(int personId);
    AuthSQL findByAccessToken(String accessToken);
    AuthSQL findByRefreshToken(String refreshToken);
    @Transactional
    void deleteAllByCreatedAtBefore(LocalDateTime before);
}
