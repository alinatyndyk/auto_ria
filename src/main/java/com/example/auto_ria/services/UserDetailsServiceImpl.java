package com.example.auto_ria.services;

import com.example.auto_ria.dao.user.AdministratorDaoSQL;
import com.example.auto_ria.dao.user.CustomerDaoSQL;
import com.example.auto_ria.dao.user.ManagerDaoSQL;
import com.example.auto_ria.dao.user.UserDaoSQL;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.user.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private AdministratorDaoSQL administratorDao;
    private CustomerDaoSQL customerDaoSQL;
    private ManagerDaoSQL managerDao;
    private UserDaoSQL sellerDao;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        ManagerSQL manager = managerDao.findByEmail(email);
        if (manager != null) {
            System.out.println(manager.getRoles());

            return Person.builder()
                    .email(manager.getEmail())
                    .password(manager.getPassword())
                    .roles(manager.getRoles())
                    .build();

        }

        SellerSQL seller = sellerDao.findSellerByEmail(email);
        if (seller != null) {
            System.out.println(seller.getRoles());

            return Person.builder()
                    .email(seller.getEmail())
                    .password(seller.getPassword())
                    .roles(seller.getRoles())
                    .build();
        }

        CustomerSQL customer = customerDaoSQL.findByEmail(email);

        if (customer != null) {
            System.out.println(customer.getRoles());
            System.out.println(customer);
            return Person.builder()
                    .email(customer.getEmail())
                    .password(customer.getPassword())
                    .roles(customer.getRoles())
                    .build();
        }

        AdministratorSQL administrator = administratorDao.findByEmail(email);
        System.out.println(administrator);

        if (administrator == null) {
            throw new CustomException("User not found with email: " + email, HttpStatus.BAD_REQUEST);
        }

        System.out.println(administrator.getRoles());

        return Person.builder()
                .email(administrator.getEmail())
                .password(administrator.getPassword())
                .roles(administrator.getRoles())
                .build();


    }

}

