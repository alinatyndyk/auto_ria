package com.example.auto_ria.dao.premium;

import com.example.auto_ria.models.premium.PremiumPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PremiumPlanDaoSQL extends JpaRepository<PremiumPlan, Integer> {

    PremiumPlan findByCustomerId(String customerId);

    PremiumPlan findBySellerId(int sellerId);

}
