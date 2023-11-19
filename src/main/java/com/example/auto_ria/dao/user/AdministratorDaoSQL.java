package com.example.auto_ria.dao.user;

import com.example.auto_ria.models.user.AdministratorSQL;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface AdministratorDaoSQL extends JpaRepository<AdministratorSQL, Integer> {
    AdministratorSQL findByEmail(String email);
    @NotNull Page<AdministratorSQL> findAll(@NotNull Pageable pageable);
    long count();

    @Transactional
    void deleteAllByIsActivatedFalseAndCreatedAtBefore(LocalDateTime before);
}
