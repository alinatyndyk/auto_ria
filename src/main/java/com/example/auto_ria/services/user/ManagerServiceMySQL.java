package com.example.auto_ria.services.user;

import com.example.auto_ria.dao.user.ManagerDaoSQL;
import com.example.auto_ria.dto.updateDTO.ManagerUpdateDTO;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.responses.user.ManagerResponse;
import com.example.auto_ria.models.user.ManagerSQL;
import com.example.auto_ria.services.CommonService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ManagerServiceMySQL {

    private ManagerDaoSQL managerDaoSQL;
    private CommonService commonService;

    public ResponseEntity<Page<ManagerResponse>> getAll(int page) {
        Pageable pageable = PageRequest.of(page, 2);

        Page<ManagerSQL> managerSQLPage = managerDaoSQL.findAll(pageable);
        Page<ManagerResponse> managerResponsePage = managerSQLPage.map(managerSQL ->
                commonService.createManagerResponse(managerSQL));

        return new ResponseEntity<>(managerResponsePage, HttpStatus.OK);
    }

    public List<ManagerSQL> getAll() {
        return managerDaoSQL.findAll();
    }

    public ManagerSQL getByEmail(String email) {
        return managerDaoSQL.findByEmail(email);
    }

    public ResponseEntity<ManagerSQL> getById(int id) {
        if (managerDaoSQL.findById(id).isEmpty()) {
            throw new CustomException("User doesnt exist", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(managerDaoSQL.findById(id).get(), HttpStatus.ACCEPTED);
    }

    public ResponseEntity<ManagerResponse> getByIdAsResponse(int id) {
        try {
            Optional<ManagerSQL> managerSQL = managerDaoSQL.findById(id);
            if (managerSQL.isEmpty()) {
                throw new CustomException("User doesnt exist", HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(commonService.createManagerResponse(managerSQL.get()), HttpStatus.ACCEPTED);
        } catch (CustomException e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<String> deleteById(int id) {
        assert managerDaoSQL.findById(id).isPresent();
        managerDaoSQL.findById(id).get();
        return new ResponseEntity<>("Success.Manager_deleted", HttpStatus.GONE);
    }

    public void checkCredentials(HttpServletRequest request, int id) {
        ManagerSQL managerSQL = commonService.extractManagerFromHeader(request);
        ManagerSQL manager = getById(id).getBody();

        if (manager == null) {
            throw new CustomException("User doesnt exist", HttpStatus.BAD_REQUEST);
        }

        if (managerSQL != null) {
            if (managerSQL.getId() == manager.getId()) {
                throw new CustomException("Forbidden. Check credentials", HttpStatus.FORBIDDEN);
            }
        }
    }

    public ResponseEntity<ManagerSQL> update(int id, ManagerUpdateDTO managerUpdateDTO) {
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
        } catch (Exception e) {
            throw new CustomException("Fail_update: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public void updateAvatar(int id, String fileName) {

        ManagerSQL managerSQL = getById(id).getBody();
        assert managerSQL != null;
        managerSQL.setAvatar(fileName);

        managerDaoSQL.save(managerSQL);
    }

    public boolean isManagerByEmailPresent(String email) {

        return managerDaoSQL.findByEmail(email) != null;
    }

}
