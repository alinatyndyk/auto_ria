package com.example.auto_ria.dao;

import com.example.auto_ria.models.CarSQL;
import com.example.auto_ria.models.SellerSQL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarDaoSQL extends JpaRepository<CarSQL, Integer> {
    List<CarSQL> findByBrand(String brand);
    List<CarSQL> findByPowerH(int power);
    List<CarSQL> findBySeller(SellerSQL sellerSQL);
}
