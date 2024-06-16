package com.example.auto_ria.services.auth;

import org.hibernate.Hibernate;
import org.hibernate.mapping.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auto_ria.dao.user.AdministratorDaoSQL;
import com.example.auto_ria.dao.user.ManagerDaoSQL;
import com.example.auto_ria.dao.user.UserDaoSQL;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.user.AdministratorSQL;
import com.example.auto_ria.models.user.ManagerSQL;
import com.example.auto_ria.models.user.Person;
import com.example.auto_ria.models.user.UserSQL;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private AdministratorDaoSQL administratorDao;
    private ManagerDaoSQL managerDao;
    private UserDaoSQL userDao;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        ManagerSQL manager = managerDao.findByEmail(email);
        
        if (manager != null) {
            Hibernate.initialize(manager.getRoles());
            // System.out.println(manager.getRoles());

            return Person.builder()
                    .email(manager.getEmail())
                    .password(manager.getPassword())
                    .roles(manager.getRoles())
                    .build();

        }

        UserSQL user = userDao.findUserByEmail(email);
        System.out.println("user" + user);
        // System.out.println(user.getRoles());

        
        if (user != null) {
            Hibernate.initialize(user.getRoles());
            return Person.builder()
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRoles())
                    .build();
        }

        AdministratorSQL administrator = administratorDao.findByEmail(email);
        System.out.println("admin" + administrator);
        // System.out.println(administrator);

        if (administrator == null) {
            throw new CustomException("User not found with email: " + email, HttpStatus.BAD_REQUEST);
        }

        Hibernate.initialize(administrator.getRoles());


        // System.out.println(administrator.getRoles());

        return Person.builder()
                .email(administrator.getEmail())
                .password(administrator.getPassword())
                .roles(administrator.getRoles())
                .build();

    }

}
