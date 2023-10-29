package com.example.auto_ria.dao.authDao;

import com.example.auto_ria.models.auth.AuthSQL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface AdminAuthDaoSQL extends JpaRepository<AuthSQL, Integer> {
    @Transactional
    void deleteAllByPersonId(int personId);
    AuthSQL findByAccessToken(String accessToken);
    AuthSQL findByRefreshToken(String refreshToken);
    void deleteAllByCreatedAtBefore(LocalDateTime before);
    @Transactional
    long countByCreatedAtBefore(LocalDateTime before);
    long count();
}