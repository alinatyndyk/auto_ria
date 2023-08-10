package com.example.auto_ria.dao;

import com.example.auto_ria.models.ManagerSQL;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagerDaoSQL extends JpaRepository<ManagerSQL, Integer> {

    ManagerSQL findByEmail(String email);
}
