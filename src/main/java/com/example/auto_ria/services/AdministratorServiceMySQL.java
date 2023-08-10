package com.example.auto_ria.services;

import com.example.auto_ria.dao.AdministratorDaoSQL;
import com.example.auto_ria.dto.updateDTO.AdministratorUpdateDTO;
import com.example.auto_ria.models.AdministratorSQL;
import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.responses.ErrorResponse;
import io.jsonwebtoken.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

@Service
@AllArgsConstructor
public class AdministratorServiceMySQL {

    private AdministratorDaoSQL administratorDaoSQL;

    public ResponseEntity<List<AdministratorSQL>> getAll() {
        return new ResponseEntity<>(administratorDaoSQL.findAll(), HttpStatus.ACCEPTED);
    }


    public ResponseEntity<AdministratorSQL> getById(String id) {
        AdministratorSQL user = administratorDaoSQL.findById(Integer.parseInt(id)).get();
        return new ResponseEntity<>(user, HttpStatus.ACCEPTED);
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

    public ResponseEntity<AdministratorSQL> update(int id, AdministratorUpdateDTO administratorUpdateDTO) throws IllegalAccessException, IOException, ErrorResponse, NoSuchFieldException {

        AdministratorSQL administratorSQL = getById(String.valueOf(id)).getBody();

        assert administratorSQL != null;
        Class<?> adminDtoClass = administratorUpdateDTO.getClass();
        Field[] fields = adminDtoClass.getDeclaredFields();

        for (Field field : fields) {

            field.setAccessible(true);

            String fieldName = field.getName();
            Object fieldValue = field.get(administratorUpdateDTO);

            if (fieldValue != null) {

                Field carField = SellerSQL.class.getDeclaredField(fieldName);

                carField.setAccessible(true);
                carField.set(administratorSQL, fieldValue);
            }
        }
        return new ResponseEntity<>(administratorDaoSQL.save(administratorSQL), HttpStatus.ACCEPTED);
    }

    public void updateAvatar(int id, String fileName) {

        AdministratorSQL administratorSQL = getById(String.valueOf(id)).getBody();
        assert administratorSQL != null;
        administratorSQL.setAvatar(fileName);

        administratorDaoSQL.save(administratorSQL);
    }

}