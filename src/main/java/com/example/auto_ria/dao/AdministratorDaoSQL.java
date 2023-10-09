package com.example.auto_ria.dao;

import com.example.auto_ria.models.AdministratorSQL;
import com.example.auto_ria.models.SellerSQL;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AdministratorDaoSQL extends JpaRepository<AdministratorSQL, Integer> {
    AdministratorSQL findByEmail(String email);
    @NotNull Page<AdministratorSQL> findAll(@NotNull Pageable pageable);
    long count();

    List<AdministratorSQL> findByCreatedAtBeforeAndIsActivatedFalse(LocalDate date);
}
