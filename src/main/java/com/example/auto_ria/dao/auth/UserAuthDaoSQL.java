package com.example.auto_ria.dao.auth;

import com.example.auto_ria.models.auth.AuthSQL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface UserAuthDaoSQL extends JpaRepository<AuthSQL, Integer> {
    @Transactional
    void deleteAllByPersonId(int personId);

    @Transactional
    void deleteAllByRefreshToken(String refreshToken);

    AuthSQL findByAccessToken(String accessToken);

    AuthSQL findByRefreshToken(String refreshToken);

    @Transactional
    void deleteAllByCreatedAtBefore(LocalDateTime before);
}