package com.example.auto_ria.dao;

import com.example.auto_ria.models.AdministratorSQL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministratorDaoSQL extends JpaRepository<AdministratorSQL, Integer> {
    AdministratorSQL findByEmail(String email);
    Page<AdministratorSQL> findAllByIsActivatedIsTrue(Pageable pageable);
    long count();
}
