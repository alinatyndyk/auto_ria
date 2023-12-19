package com.example.auto_ria.dao.premium;

import com.example.auto_ria.models.premium.PremiumPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

public interface PremiumPlanDaoSQL extends JpaRepository<PremiumPlan, Integer> {

    PremiumPlan findByCustomerId(String customerId);

    PremiumPlan findBySellerId(int sellerId);

    @Transactional
    void deleteByEndDateBefore(LocalDate date);

}
