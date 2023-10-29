package com.example.auto_ria.services;

import com.example.auto_ria.dao.AdministratorDaoSQL;
import com.example.auto_ria.dao.CustomerDaoSQL;
import com.example.auto_ria.dao.ManagerDaoSQL;
import com.example.auto_ria.dao.UserDaoSQL;
import com.example.auto_ria.enums.ETokenRole;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CommonService {

    private JwtService jwtService;
    private UserDaoSQL userDaoSQL;
    private ManagerDaoSQL managerDaoSQL;
    private CustomerDaoSQL customerDaoSQL;
    private AdministratorDaoSQL administratorDaoSQL;

    public void validate(BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new CustomException(errors.toString(), HttpStatus.BAD_REQUEST);
        }

        // String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
        //        return ResponseEntity.badRequest().body(errorMessage);
    }

    public void transferAvatar(MultipartFile picture, String originalFileName) throws java.io.IOException {
        String path = System.getProperty("user.home") + File.separator + "springboot-lib" + File.separator + originalFileName;
        File transferDestinationFile = new File(path);
        picture.transferTo(transferDestinationFile);
    }

    public List<String> transferPhotos(MultipartFile[] newPictures) {
        return Arrays.stream(newPictures).map(picture -> {
            String fileName = picture.getOriginalFilename();
            try {
                transferAvatar(picture, fileName);
            } catch (IOException e) {
                throw new CustomException("Failed: Transfer_photos. Try again later", HttpStatus.EXPECTATION_FAILED);
            }
            return fileName;
        }).collect(Collectors.toList());
    }

    public void removeAvatar(String originalFileName) {
        try {
            String path = System.getProperty("user.home") + File.separator + "springboot-lib" + File.separator + originalFileName;
            File transferDestinationFile = new File(path);
            if (transferDestinationFile.exists()) {
                transferDestinationFile.delete();
            }
        } catch (Exception e) {
            throw new CustomException("Error while handling avatars: " + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }


    public String extractEmailFromHeader(HttpServletRequest request, ETokenRole role) {
        String bearerToken = jwtService.extractTokenFromHeader(request);

        return jwtService.extractUsername(bearerToken, role);
    }

    public SellerSQL extractSellerFromHeader(HttpServletRequest request) {

        SellerSQL sellerSQL;
        try {
            sellerSQL = userDaoSQL.findSellerByEmail(extractEmailFromHeader(request, ETokenRole.SELLER));
        } catch (Exception e) {
            return null;
        }
        return sellerSQL;
    }

    public ManagerSQL extractManagerFromHeader(HttpServletRequest request) {
        ManagerSQL managerSQL;
        try {
            managerSQL = managerDaoSQL.findByEmail(extractEmailFromHeader(request, ETokenRole.MANAGER));
        } catch (Exception e) {
            return null;
        }
        return managerSQL;
    }

    public AdministratorSQL extractAdminFromHeader(HttpServletRequest request) {
        AdministratorSQL administratorSQL;
        try {
            administratorSQL = administratorDaoSQL.findByEmail(extractEmailFromHeader(request, ETokenRole.ADMIN));
        } catch (Exception e) {
            return null;
        }
        return administratorSQL;
    }

    public CustomerSQL extractCustomerFromHeader(HttpServletRequest request) {
        return customerDaoSQL.findByEmail(extractEmailFromHeader(request, ETokenRole.CUSTOMER));
    }


}
