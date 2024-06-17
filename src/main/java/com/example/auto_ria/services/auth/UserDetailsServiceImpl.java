package com.example.auto_ria.services.auth;

import org.hibernate.Hibernate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auto_ria.dao.user.UserDaoSQL;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.user.Person;
import com.example.auto_ria.models.user.UserSQL;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private UserDaoSQL userDao;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        UserSQL user = userDao.findUserByEmail(email);

        if (user == null) {
            throw new CustomException("User does not exist", HttpStatus.NOT_FOUND);
        }

        Hibernate.initialize(user.getRoles());
        return Person.builder()
                .email(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRoles())
                .build();

    }
}
