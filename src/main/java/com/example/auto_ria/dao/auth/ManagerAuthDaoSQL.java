package com.example.auto_ria.dao.auth;

import com.example.auto_ria.models.auth.AuthSQL;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ManagerAuthDaoSQL extends JpaRepository<AuthSQL, Integer> {
    void deleteAllByPersonId(int personId);
    void deleteAllByRefreshToken(String refreshToken); //todo all or not

    AuthSQL findByAccessToken(String accessToken);
    AuthSQL findByRefreshToken(String refreshToken);
    void deleteAllByCreatedAtBefore(LocalDateTime before);
}
