package com.example.auto_ria.services.user;

import com.example.auto_ria.dao.user.UserDaoSQL;
import com.example.auto_ria.dto.updateDTO.UserUpdateDTO;
import com.example.auto_ria.enums.EMail;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.mail.FMService;
import com.example.auto_ria.models.responses.user.AdminResponse;
import com.example.auto_ria.models.responses.user.SellerResponse;
import com.example.auto_ria.models.user.AdministratorSQL;
import com.example.auto_ria.models.user.ManagerSQL;
import com.example.auto_ria.models.user.SellerSQL;
import com.example.auto_ria.services.CommonService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UsersServiceMySQLImpl {

    private UserDaoSQL userDaoSQL;
    private CommonService commonService;
    private FMService mailer;

    public ResponseEntity<Page<SellerResponse>> getAll(int page) {
        Pageable pageable = PageRequest.of(page, 2);

        Page<SellerSQL> sellerSQLPage = userDaoSQL.findAll(pageable);
        Page<SellerResponse> sellerResponsePage = sellerSQLPage.map(sellerSQL ->
                commonService.createSellerResponse(sellerSQL));

        return new ResponseEntity<>(sellerResponsePage, HttpStatus.OK);
    }


    public ResponseEntity<SellerSQL> getById(String id) {
        assert userDaoSQL.findById(Integer.parseInt(id)).isPresent();
        SellerSQL user = userDaoSQL.findById(Integer.parseInt(id)).get();
        return new ResponseEntity<>(user, HttpStatus.ACCEPTED);
    }

    public ResponseEntity<SellerResponse> getByIdAsResponse(int id) {
        try {
            SellerSQL sellerSQL = userDaoSQL.findById(id).orElse(null);

            if (sellerSQL == null) {
                throw new CustomException("User doesnt exist", HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(commonService.createSellerResponse(sellerSQL), HttpStatus.ACCEPTED);
        } catch (CustomException e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public SellerSQL getByEmail(String email) {
        return userDaoSQL.findSellerByEmail(email);
    }

    public SellerSQL getById(int id) {
        if (userDaoSQL.findById(id).isEmpty()) {
            throw new CustomException("User doesnt exist", HttpStatus.BAD_REQUEST);
        }
        return userDaoSQL.findById(id).get();
    }

    public void transferAvatar(MultipartFile picture, String originalFileName) {
        try {
            String path = System.getProperty("user.home") + File.separator + "springboot-lib" + File.separator + originalFileName;
            File transferDestinationFile = new File(path);
            picture.transferTo(transferDestinationFile);
        } catch (Exception e) {
            throw new CustomException(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<String> deleteById(String id, SellerSQL seller, AdministratorSQL administratorSQL, ManagerSQL manager) {
        userDaoSQL.deleteById(Integer.valueOf(id));

        HashMap<String, Object> vars = new HashMap<>();
        vars.put("name", seller.getName());
        vars.put("email", seller.getEmail());

        if (administratorSQL != null || manager != null) {
            try {
                mailer.sendEmail(seller.getEmail(), EMail.YOUR_ACCOUNT_BANNED, vars);
            } catch (Exception ignore) {
            }
        }

        try {
            mailer.sendEmail(seller.getEmail(), EMail.PLATFORM_LEAVE, vars);
        } catch (Exception ignore) {
        }

        return new ResponseEntity<>("Success.User_deleted", HttpStatus.GONE);
    }

    public boolean doesBelongToSeller(SellerSQL seller, SellerSQL seller1) {
        return seller.getId() == seller1.getId();
    }

    public ResponseEntity<SellerSQL> update(int id, UserUpdateDTO userDTO, SellerSQL seller) {
        try {
            SellerSQL seller1 = getById(String.valueOf(id)).getBody();

            assert seller1 != null;
            if (doesBelongToSeller(seller, seller1)) {

                Class<?> carDTOClass = userDTO.getClass();
                Field[] fields = carDTOClass.getDeclaredFields();

                for (Field field : fields) {

                    field.setAccessible(true);

                    String fieldName = field.getName();
                    Object fieldValue = field.get(userDTO);

                    if (fieldValue != null) {

                        Field carField = SellerSQL.class.getDeclaredField(fieldName);

                        carField.setAccessible(true);
                        carField.set(seller1, fieldValue);
                    }
                }
            } else {
                throw new IllegalAccessException("Error.Update_fail: The car does not belong to seller");
            }
            return new ResponseEntity<>(userDaoSQL.save(seller1), HttpStatus.ACCEPTED);
        } catch (IllegalAccessException e) {
            throw new CustomException(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            throw new CustomException(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public void updateAvatar(int id, String fileName) {

        SellerSQL seller = getById(String.valueOf(id)).getBody();
        assert seller != null;
        seller.setAvatar(fileName);

        userDaoSQL.save(seller);
    }

    public boolean isSellerByNumberPresent(String number) {

        if (userDaoSQL.findSellerByNumber(number) != null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isSellerByEmailPresent(String email) {

        if (userDaoSQL.findSellerByEmail(email) != null) {
            return true;
        } else {
            return false;
        }
    }

}
