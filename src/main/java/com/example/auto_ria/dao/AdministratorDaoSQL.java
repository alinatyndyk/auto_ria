package com.example.auto_ria.dao;

import com.example.auto_ria.models.AdministratorSQL;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministratorDaoSQL extends JpaRepository<AdministratorSQL, Integer> {
    AdministratorSQL findByEmail(String email);
}
