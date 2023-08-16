package com.example.auto_ria.services;

import com.example.auto_ria.dao.AdministratorDaoSQL;
import com.example.auto_ria.dao.CustomerDaoSQL;
import com.example.auto_ria.dao.ManagerDaoSQL;
import com.example.auto_ria.dao.UserDaoSQL;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.AdministratorSQL;
import com.example.auto_ria.models.CustomerSQL;
import com.example.auto_ria.models.ManagerSQL;
import com.example.auto_ria.models.SellerSQL;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.List;

@Service
@AllArgsConstructor
public class CommonService {

    private JwtService jwtService;
    private UserDaoSQL userDaoSQL;
    private ManagerDaoSQL managerDaoSQL;
    private CustomerDaoSQL customerDaoSQL;
    private AdministratorDaoSQL administratorDaoSQL;

    public void validate (BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new CustomException(errors.toString(), HttpStatus.BAD_REQUEST);
        }
    }


    public String extractEmailFromHeader(HttpServletRequest request, ERole role) {
        String bearerToken = jwtService.extractTokenFromHeader(request);

        return jwtService.extractUsername(bearerToken, role);
    }

    public SellerSQL extractSellerFromHeader(HttpServletRequest request) {

        SellerSQL sellerSQL;
        try {
            sellerSQL = userDaoSQL.findSellerByEmail(extractEmailFromHeader(request, ERole.SELLER));
        } catch (Exception e) {
            return null;
        }
        return sellerSQL;
    }

    public ManagerSQL extractManagerFromHeader(HttpServletRequest request) {
        ManagerSQL managerSQL;
        try {
            managerSQL = managerDaoSQL.findByEmail(extractEmailFromHeader(request, ERole.MANAGER));
        } catch (Exception e) {
            return null;
        }
        return managerSQL;
    }

    public AdministratorSQL extractAdminFromHeader(HttpServletRequest request) {
        AdministratorSQL administratorSQL;
        try {
            administratorSQL = administratorDaoSQL.findByEmail(extractEmailFromHeader(request, ERole.ADMIN));
        } catch (Exception e) {
            return null;
        }
        return administratorSQL;
    }

    public CustomerSQL extractCustomerFromHeader(HttpServletRequest request) {
        return customerDaoSQL.findByEmail(extractEmailFromHeader(request, ERole.CUSTOMER));
    }


}
