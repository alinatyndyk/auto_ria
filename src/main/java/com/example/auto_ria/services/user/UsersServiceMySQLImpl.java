package com.example.auto_ria.services.user;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
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
            System.out.println(e.getMessage());
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

    // public ResponseEntity<UserResponse> update(int id, UserUpdateDTO userDTO, UserSQL user) {
    //     try {

    //         System.out.println(userDTO + "UserDTO");

    //         Class<?> userDTOClass = userDTO.getClass();
    //         Field[] fields = userDTOClass.getDeclaredFields();

    //         for (Field field : fields) {

    //             System.out.println(field + "field");
                
    //             field.setAccessible(true);
                
    //             String fieldName = field.getName();
    //             System.out.println(fieldName + "field nmse");
    //             Object fieldValue = field.get(userDTO);
    //             System.out.println(fieldValue + "field val");
                
    //             if (fieldValue != null) {
    //                 System.out.println(fieldValue + "not null");
                    
    //                 Field carField = UserSQL.class.getDeclaredField(fieldName);
    //                 System.out.println(carField + "in userSQL");

    //                 carField.setAccessible(true);
    //                 System.out.println("accessible");
    //                 carField.set(user, fieldValue);
    //                 System.out.println(fieldName + "car field n new");
    //                 System.out.println(fieldValue + "car field v new");
    //             }
    //         }
    //         UserSQL userSQL = userDaoSQL.save(user);
    //         return new ResponseEntity<>(commonService.createUserResponse(userSQL), HttpStatus.ACCEPTED);
    //     } catch (IllegalAccessException e) {
    //         throw new CustomException(e.getMessage(), HttpStatus.FORBIDDEN);
    //     } catch (Exception e) {
    //         throw new CustomException(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
    //     }
    // }

    public ResponseEntity<UserResponse> update(int id, UserUpdateDTO userDTO, UserSQL user) {
        try {
            Class<?> userDTOClass = userDTO.getClass();
            System.out.println(userDTOClass + " userDTO class");
            Field[] fields = userDTOClass.getDeclaredFields();

            System.out.println(Arrays.toString(fields) + " fields");

            for (Field field : fields) {
                System.out.println(field + " field");

                field.setAccessible(true);
                System.out.println(132);

                String fieldName = field.getName();
                System.out.println(fieldName + " field name");
                Object fieldValue = field.get(userDTO);
                System.out.println(fieldValue + " field value");

                if (fieldValue != null) {
                    System.out.println(140);
                    Field carField = findField(UserSQL.class, fieldName);
                    System.out.println(carField + " car field");

                    carField.setAccessible(true);
                    System.out.println(146);
                    carField.set(user, fieldValue);
                    System.out.println(carField + " after set");
                }
            }
            System.out.println("user" + user);
            UserSQL userSQL = userDaoSQL.save(user);
            System.out.println("userSQL" + userSQL);

            return new ResponseEntity<>(commonService.createUserResponse(userSQL), HttpStatus.ACCEPTED);
        } catch (NoSuchFieldException e) {
            System.out.println(e.getMessage() + " NoSuchFieldException");
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            System.out.println(e.getMessage() + " IllegalAccessException");
            throw new CustomException(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            System.out.println(e.getMessage() + " General Exception");
            throw new CustomException(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }

    }

    // Method to find field in the class hierarchy
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
