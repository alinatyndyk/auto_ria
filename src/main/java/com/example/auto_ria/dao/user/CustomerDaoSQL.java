package com.example.auto_ria.dao.user;

import com.example.auto_ria.models.user.CustomerSQL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface CustomerDaoSQL extends JpaRepository<CustomerSQL, Integer> {
    CustomerSQL findByEmail(String email); //todo fix car post, card token

    @Transactional
    void deleteAllByIsActivatedFalseAndCreatedAtBefore(LocalDateTime before);
}
