package com.example.auto_ria.dao;

import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.UserSQL;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDaoSQL extends JpaRepository<SellerSQL, Integer> {
    SellerSQL findSellerByEmail(String email);
    UserSQL findByEmail(String email);
    UserSQL findByLastName(String lastName);

}
