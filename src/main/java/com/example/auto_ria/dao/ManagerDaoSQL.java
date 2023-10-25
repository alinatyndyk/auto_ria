package com.example.auto_ria.dao;

import com.example.auto_ria.models.ManagerSQL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface ManagerDaoSQL extends JpaRepository<ManagerSQL, Integer> {

    ManagerSQL findByEmail(String email);

    @Transactional
    void deleteAllByIsActivatedFalseAndCreatedAtBefore(LocalDateTime before);
}
