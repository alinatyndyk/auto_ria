package com.example.auto_ria.dao.user;

import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.models.user.UserSQL;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface UserDaoSQL extends JpaRepository<UserSQL, Integer> {
    UserSQL findByEmail(String email);

    @Query("SELECT p FROM UserSQL p WHERE p.email = :email")
    UserSQL findUserByEmail(@Param("email") String email);

    UserSQL findByPaymentSource(String paymentSource);

    UserSQL findUserByNumber(String number);

    @Query("SELECT u FROM UserSQL u JOIN u.roles r WHERE r = :role")
    Page<UserSQL> findAllByRole(@Param("role") ERole role, Pageable pageable);

    @Transactional
    void deleteAllByIsActivatedFalseAndCreatedAtBefore(LocalDateTime before);
}
