package com.example.auto_ria.dao;

import com.example.auto_ria.models.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarDaoSQL extends JpaRepository<Car, Integer> {
    List<Car> findByBrand(String brand);
    List<Car> findByPowerH(int power);
}
