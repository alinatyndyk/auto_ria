package com.example.auto_ria.dao;

import com.example.auto_ria.models.auth.RegisterKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegisterKeyDaoSQL extends JpaRepository<RegisterKey, Integer> {
    RegisterKey findByRegisterKey(String key);
}
