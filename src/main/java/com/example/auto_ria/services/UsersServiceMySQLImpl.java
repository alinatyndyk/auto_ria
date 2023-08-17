package com.example.auto_ria.services;

import com.example.auto_ria.dao.UserDaoSQL;
import com.example.auto_ria.dto.updateDTO.UserUpdateDTO;
import com.example.auto_ria.enums.EMail;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.mail.FMService;
import com.example.auto_ria.models.AdministratorSQL;
import com.example.auto_ria.models.ManagerSQL;
import com.example.auto_ria.models.SellerSQL;
import io.jsonwebtoken.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;

@Service
@AllArgsConstructor
public class UsersServiceMySQLImpl {

    private UserDaoSQL userDaoSQL;
    private FMService mailer;

    public ResponseEntity<Page<SellerSQL>> getAll(int page) {
        Pageable pageable = PageRequest.of(page, 2);
        return new ResponseEntity<>(userDaoSQL.findAll(pageable), HttpStatus.ACCEPTED);
    }


    public ResponseEntity<SellerSQL> getById(String id) {
        assert userDaoSQL.findById(Integer.parseInt(id)).isPresent();
        SellerSQL user = userDaoSQL.findById(Integer.parseInt(id)).get();
        return new ResponseEntity<>(user, HttpStatus.ACCEPTED);
    }

    public SellerSQL getById(int id) {
        if (userDaoSQL.findById(id).isEmpty()) {
            throw new CustomException("User doesnt exist", HttpStatus.BAD_REQUEST);
        }
        return userDaoSQL.findById(id).get();
    }

    public void transferAvatar(MultipartFile picture, String originalFileName) throws java.io.IOException {
        String path = System.getProperty("user.home") + File.separator + "springboot-lib" + File.separator + originalFileName;
        File transferDestinationFile = new File(path);
        picture.transferTo(transferDestinationFile);
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

    public ResponseEntity<SellerSQL> update(int id, UserUpdateDTO userDTO, SellerSQL seller)
            throws IllegalAccessException, IOException, NoSuchFieldException {

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
            throw new CustomException("Error.Update_fail: The car does not belong to seller", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(userDaoSQL.save(seller1), HttpStatus.ACCEPTED);
    }

    public void updateAvatar(int id, String fileName) {

        SellerSQL seller = getById(String.valueOf(id)).getBody();
        assert seller != null;
        seller.setAvatar(fileName);

        userDaoSQL.save(seller);
    }

}
