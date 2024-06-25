package com.example.auto_ria.services.user;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.auto_ria.dao.user.UserDaoSQL;
import com.example.auto_ria.dto.updateDTO.UserUpdateDTO;
import com.example.auto_ria.enums.EMail;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.mail.FMService;
import com.example.auto_ria.models.responses.user.UserResponse;
import com.example.auto_ria.models.user.UserSQL;
import com.example.auto_ria.services.CommonService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UsersServiceMySQLImpl {

    private UserDaoSQL userDaoSQL;
    private CommonService commonService;
    private FMService mailer;

    public ResponseEntity<Page<UserResponse>> getAll(int page) {
        Pageable pageable = PageRequest.of(page, 2);

        Page<UserSQL> userSQLPage = userDaoSQL.findAll(pageable);
        Page<UserResponse> userResponsePage = userSQLPage
                .map(userSQL -> commonService.createUserResponse(userSQL));

        return new ResponseEntity<>(userResponsePage, HttpStatus.OK);
    }

    public ResponseEntity<UserSQL> getById(String id) {
        assert userDaoSQL.findById(Integer.parseInt(id)).isPresent();
        UserSQL user = userDaoSQL.findById(Integer.parseInt(id)).get();
        return new ResponseEntity<>(user, HttpStatus.ACCEPTED);
    }

    public List<UserSQL> getListByRole(ERole role) {
        return userDaoSQL.findAllByRole(role);
    }

    public ResponseEntity<UserResponse> getByIdAsResponse(int id) {
        try {
            UserSQL userSQL = userDaoSQL.findById(id).orElse(null);

            if (userSQL == null) {
                throw new CustomException("User doesnt exist", HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(commonService.createUserResponse(userSQL), HttpStatus.ACCEPTED);
        } catch (CustomException e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public UserSQL getByEmail(String email) {
        try {
            return userDaoSQL.findUserByEmail(email);
        } catch (Exception e) {
            throw new CustomException(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public UserSQL getById(int id) {
        if (userDaoSQL.findById(id).isEmpty()) {
            throw new CustomException("User doesnt exist", HttpStatus.BAD_REQUEST);
        }
        return userDaoSQL.findById(id).get();
    }

    public Integer countByRole(ERole role) {
        return userDaoSQL.countByRole(role);
    }

    public void transferAvatar(MultipartFile picture, String originalFileName) {
        try {
            String path = System.getProperty("user.home") + File.separator + "springboot-lib" + File.separator
                    + originalFileName;
            File transferDestinationFile = new File(path);
            picture.transferTo(transferDestinationFile);
        } catch (Exception e) {
            throw new CustomException(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<String> deleteById(String id, UserSQL user, EMail deleteTypeMail) {
        userDaoSQL.deleteById(Integer.valueOf(id));

        HashMap<String, Object> vars = new HashMap<>();
        vars.put("name", user.getName());
        vars.put("email", user.getEmail());

        mailer.sendEmail(user.getEmail(), deleteTypeMail, vars);

        return new ResponseEntity<>("Success.User_deleted", HttpStatus.GONE);
    }

    public boolean doesBelongToSeller(UserSQL user, UserSQL user1) {
        return user.getId() == user1.getId();
    }

    public ResponseEntity<UserResponse> update(int id, UserUpdateDTO userDTO, UserSQL user) {
        try {
            Class<?> userDTOClass = userDTO.getClass();
            Field[] fields = userDTOClass.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);

                String fieldName = field.getName();
                Object fieldValue = field.get(userDTO);

                if (fieldValue != null) {
                    Field carField = findField(UserSQL.class, fieldName);

                    carField.setAccessible(true);
                    carField.set(user, fieldValue);
                }
            }
            UserSQL userSQL = userDaoSQL.save(user);

            return new ResponseEntity<>(commonService.createUserResponse(userSQL), HttpStatus.ACCEPTED);
        } catch (NoSuchFieldException e) {
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            throw new CustomException(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            throw new CustomException(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }

    }

    private Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    public void updateAvatar(int id, String fileName) {

        UserSQL user = getById(String.valueOf(id)).getBody();
        assert user != null;
        user.setAvatar(fileName);

        userDaoSQL.save(user);
    }

    public boolean isUserByNumberPresent(String number) {

        return userDaoSQL.findUserByNumber(number) != null;
    }

    public boolean isUserByEmailPresent(String email) {

        return userDaoSQL.findUserByEmail(email) != null;
    }

    public ResponseEntity<Page<UserResponse>> findAllByRole(ERole role, int page) {

        Pageable pageable = PageRequest.of(page, 2);
        Page<UserSQL> userSQLPage = userDaoSQL.findAllByRole(role, pageable);

        Page<UserResponse> userResponsePage = userSQLPage
                .map(userSQL -> commonService.createUserResponse(userSQL));

        return new ResponseEntity<>(userResponsePage, HttpStatus.OK); // todo to common service
    }

    public List<UserSQL> findAllByRole(ERole role) {

        return userDaoSQL.findAllByRole(role);

    }

}
