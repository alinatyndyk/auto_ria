package com.example.auto_ria.dao.user;

import com.example.auto_ria.models.user.ManagerSQL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface ManagerDaoSQL extends JpaRepository<ManagerSQL, Integer> {

    ManagerSQL findByEmail(String email);

    @Transactional
    void deleteAllByIsActivatedFalseAndCreatedAtBefore(LocalDateTime before);
}
