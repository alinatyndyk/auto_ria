package com.example.auto_ria.dao;

import com.example.auto_ria.models.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministratorDaoSQL extends JpaRepository<Administrator, Integer> {
    Administrator findByEmail(String email);
}
