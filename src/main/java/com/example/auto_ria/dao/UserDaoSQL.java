package com.example.auto_ria.dao;

import com.example.auto_ria.models.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDaoSQL  extends JpaRepository<Seller, Integer> {

    Seller findSellerByEmail(String email);
}
