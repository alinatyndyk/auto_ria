package com.example.auto_ria.dao.authDao;

import com.example.auto_ria.models.AuthSQL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface AdminAuthDaoSQL extends JpaRepository<AuthSQL, Integer> {
    @Transactional
    void deleteAllByPersonId(int personId);
    AuthSQL findByAccessToken(String accessToken);
    AuthSQL findByRefreshToken(String refreshToken);
}
