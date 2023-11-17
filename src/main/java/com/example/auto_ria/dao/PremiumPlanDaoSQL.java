package com.example.auto_ria.dao;

import com.example.auto_ria.models.premium.PremiumPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface PremiumPlanDaoSQL extends JpaRepository<PremiumPlan, Integer> {

    PremiumPlan findByCustomerId(String customerId);

}
