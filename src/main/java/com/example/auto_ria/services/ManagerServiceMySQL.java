package com.example.auto_ria.services;

import com.example.auto_ria.dao.ManagerDaoSQL;
import com.example.auto_ria.dto.updateDTO.ManagerUpdateDTO;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.ManagerSQL;
import io.jsonwebtoken.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;

@Service
@AllArgsConstructor
public class ManagerServiceMySQL {

    private ManagerDaoSQL managerDaoSQL;

    public ResponseEntity<List<ManagerSQL>> getAll() {
        HttpHeaders httpHeaders = new HttpHeaders();
        return new ResponseEntity<>(managerDaoSQL.findAll(), httpHeaders, HttpStatus.ACCEPTED);
    }

    public ResponseEntity<ManagerSQL> getById(int id) {
        assert managerDaoSQL.findById(id).isPresent();
        return new ResponseEntity<>(managerDaoSQL.findById(id).get(), HttpStatus.ACCEPTED);
    }

    public ResponseEntity<String> deleteById(int id) {
        assert managerDaoSQL.findById(id).isPresent();
        managerDaoSQL.findById(id).get();
        return new ResponseEntity<>("Success.Manager_deleted", HttpStatus.GONE);
    }

    public ResponseEntity<ManagerSQL> update(int id, ManagerUpdateDTO managerUpdateDTO)
            throws IllegalAccessException, IOException, NoSuchFieldException {
        try {

            ManagerSQL manager = getById(id).getBody();

            assert manager != null;
            Class<?> managerUpdateDTOClass = managerUpdateDTO.getClass();
            Field[] fields = managerUpdateDTOClass.getDeclaredFields();

            for (Field field : fields) {

                field.setAccessible(true);

                String fieldName = field.getName();
                Object fieldValue = field.get(managerUpdateDTO);

                if (fieldValue != null) {

                    Field managerField = ManagerSQL.class.getDeclaredField(fieldName);

                    managerField.setAccessible(true);
                    managerField.set(manager, fieldValue);
                }
            }
            return new ResponseEntity<>(managerDaoSQL.save(manager), HttpStatus.ACCEPTED);
        } catch (Exception exception) {
            throw new CustomException("Fail_update", HttpStatus.CONFLICT);
        }
    }


}
