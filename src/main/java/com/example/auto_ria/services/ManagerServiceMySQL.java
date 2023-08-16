package com.example.auto_ria.services;

import com.example.auto_ria.dao.ManagerDaoSQL;
import com.example.auto_ria.dto.updateDTO.ManagerUpdateDTO;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.AdministratorSQL;
import com.example.auto_ria.models.ManagerSQL;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private CommonService commonService;

    public ResponseEntity<Page<ManagerSQL>> getAll(int page) {
        HttpHeaders httpHeaders = new HttpHeaders();
        Pageable pageable = PageRequest.of(page, 2);
        return new ResponseEntity<>(managerDaoSQL.findAll(pageable), httpHeaders, HttpStatus.ACCEPTED);
    }

    public List<ManagerSQL> getAll() {
        return managerDaoSQL.findAll();
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

    public void checkCredentials(HttpServletRequest request, int id) {
        AdministratorSQL administratorSQL = commonService.extractAdminFromHeader(request);
        ManagerSQL managerSQL = commonService.extractManagerFromHeader(request);
        ManagerSQL manager = getById(id).getBody();

        if (administratorSQL == null && managerSQL.equals(manager)) {
            throw new CustomException("Forbidden. Check credentials", HttpStatus.FORBIDDEN);
        }
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
