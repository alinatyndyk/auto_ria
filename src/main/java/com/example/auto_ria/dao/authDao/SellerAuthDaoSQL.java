package com.example.auto_ria.dao.authDao;

import com.example.auto_ria.models.AuthSQL;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerAuthDaoSQL extends JpaRepository<AuthSQL, Integer> {
    void deleteAllByPersonId(int personId);
    AuthSQL findByAccessToken(String accessToken);
    AuthSQL findByRefreshToken(String refreshToken);
}
