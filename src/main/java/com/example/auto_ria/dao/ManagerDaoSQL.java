package com.example.auto_ria.dao;

import com.example.auto_ria.models.Manager;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagerDaoSQL extends JpaRepository<Manager, Integer> {

    Manager findByEmail(String email);
}
