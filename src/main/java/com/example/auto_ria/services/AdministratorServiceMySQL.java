package com.example.auto_ria.services;

import com.example.auto_ria.dao.AdministratorDaoSQL;
import com.example.auto_ria.dao.RegisterKeyDaoSQL;
import com.example.auto_ria.dto.updateDTO.AdministratorUpdateDTO;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.AdministratorSQL;
import com.example.auto_ria.models.Person;
import com.example.auto_ria.models.RegisterKey;
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

@Service
@AllArgsConstructor
public class AdministratorServiceMySQL {

    private AdministratorDaoSQL administratorDaoSQL;
    private JwtService jwtService;
    private RegisterKeyDaoSQL registerKeyDaoSQL;

    public ResponseEntity<Page<AdministratorSQL>> getAll(int page) {
        Pageable pageable = PageRequest.of(page, 2);
        Page<AdministratorSQL> admins = administratorDaoSQL.findAll(pageable);
        return new ResponseEntity<>(admins, HttpStatus.ACCEPTED);
    }


    public ResponseEntity<AdministratorSQL> getById(String id) {
        if (administratorDaoSQL.findById(Integer.parseInt(id)).isEmpty()) {
            throw new CustomException("User doesnt exist", HttpStatus.BAD_REQUEST);
        }
        AdministratorSQL administratorSQL = administratorDaoSQL.findById(Integer.parseInt(id)).get();
        return new ResponseEntity<>(administratorSQL, HttpStatus.ACCEPTED);
    }

    public void transferAvatar(MultipartFile picture, String originalFileName) throws java.io.IOException {
        String path = System.getProperty("user.home") + File.separator + "springboot-lib" + File.separator + originalFileName;
        File transferDestinationFile = new File(path);
        picture.transferTo(transferDestinationFile);
    }

    public ResponseEntity<String> deleteById(String id) {
        administratorDaoSQL.deleteById(Integer.valueOf(id));
        return new ResponseEntity<>("Success.User_deleted", HttpStatus.GONE);
    }

    public ResponseEntity<AdministratorSQL> update(AdministratorUpdateDTO administratorUpdateDTO, AdministratorSQL administratorSQL) throws IllegalAccessException, NoSuchFieldException {
        try {

            Class<?> adminDtoClass = administratorUpdateDTO.getClass();
            Field[] fields = adminDtoClass.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);

                String fieldName = field.getName();
                Object fieldValue = field.get(administratorUpdateDTO);

                if (fieldName.equals("name")) {
                    Field personField = Person.class.getDeclaredField(fieldName);
                    personField.setAccessible(true);
                    personField.set(administratorSQL, fieldValue);
                } else if (fieldValue != null) {

                    Field adminField = AdministratorSQL.class.getDeclaredField(fieldName);

                    adminField.setAccessible(true);
                    adminField.set(administratorSQL, fieldValue);
                }
            }
            return new ResponseEntity<>(administratorDaoSQL.save(administratorSQL), HttpStatus.ACCEPTED);
        } catch (Exception exception) {
            throw new CustomException("Administrator_update_failed. Forbidden fields found", HttpStatus.CONFLICT);
        }
    }

    public void updateAvatar(int id, String fileName) {

        AdministratorSQL administratorSQL = getById(String.valueOf(id)).getBody();
        assert administratorSQL != null;
        administratorSQL.setAvatar(fileName);

        administratorDaoSQL.save(administratorSQL);
    }

    public ResponseEntity<RegisterKey> generateAuthKey(String email, ERole role) {
        String key = jwtService.generateRegisterKey(email, role);
        return ResponseEntity.ok(registerKeyDaoSQL.save(RegisterKey.builder().registerKey(key).build()));
    }

}
