package com.example.auto_ria.services.user;

import com.example.auto_ria.dao.user.CustomerDaoSQL;
import com.example.auto_ria.dto.updateDTO.CustomerUpdateDTO;
import com.example.auto_ria.enums.EMail;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.mail.FMService;
import com.example.auto_ria.models.responses.user.AdminResponse;
import com.example.auto_ria.models.responses.user.CustomerResponse;
import com.example.auto_ria.models.user.AdministratorSQL;
import com.example.auto_ria.models.user.CustomerSQL;
import com.example.auto_ria.models.user.ManagerSQL;
import com.example.auto_ria.models.user.SellerSQL;
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
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CustomersServiceMySQL {

    private CustomerDaoSQL customerDaoSQL;
    private CommonService commonService;
    private FMService mailer;

    public ResponseEntity<Page<CustomerResponse>> getAll(int page) {
        try {
            Pageable pageable = PageRequest.of(page, 2);
            Page<CustomerSQL> customerSQLPage = customerDaoSQL.findAll(pageable);
            Page<CustomerResponse> customerResponsePage = customerSQLPage.map(customerSQL ->
                    commonService.createCustomerResponse(customerSQL));
            return new ResponseEntity<>(customerResponsePage, HttpStatus.OK);
        } catch (CustomException e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }


    public ResponseEntity<CustomerSQL> getById(String id) {
        try {
            if (customerDaoSQL.findById(Integer.parseInt(id)).isEmpty()) {
                throw new CustomException("User doesnt exist", HttpStatus.BAD_REQUEST);
            }
            CustomerSQL user = customerDaoSQL.findById(Integer.parseInt(id)).get();
            return new ResponseEntity<>(user, HttpStatus.ACCEPTED);
        } catch (CustomException e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<CustomerResponse> getByIdAsResponse(int id) {
        try {
            Optional<CustomerSQL> customerSQL = customerDaoSQL.findById(id);
            if (customerSQL.isPresent()) {
                throw new CustomException("User doesnt exist", HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(commonService.createCustomerResponse(customerSQL.get()), HttpStatus.ACCEPTED);
        } catch (CustomException e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public CustomerSQL getByEmail(String email) {
        try {
            return customerDaoSQL.findByEmail(email);
        } catch (CustomException e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public void checkCredentials(HttpServletRequest request, int id) {
        try {
            CustomerSQL customerSQL = commonService.extractCustomerFromHeader(request);
            CustomerSQL customerById = getById(String.valueOf(id)).getBody();

            if (customerSQL != null && customerSQL.getId() != Objects.requireNonNull(customerById).getId()) {
                throw new CustomException("Check credentials", HttpStatus.FORBIDDEN);
            }
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<String> deleteById(String id, CustomerSQL customerSQL, AdministratorSQL administratorSQL, ManagerSQL manager) {
        try {
            customerDaoSQL.deleteById(Integer.valueOf(id));

            HashMap<String, Object> vars = new HashMap<>();
            vars.put("name", customerSQL.getName());
            vars.put("email", customerSQL.getEmail());

            if (administratorSQL != null || manager != null) {
                try {
                    mailer.sendEmail(customerSQL.getEmail(), EMail.YOUR_ACCOUNT_BANNED, vars);
                } catch (Exception ignore) {
                }
            }

            try {
                mailer.sendEmail(customerSQL.getEmail(), EMail.PLATFORM_LEAVE, vars);
            } catch (Exception ignore) {
            }

            return new ResponseEntity<>("Success.User_deleted", HttpStatus.GONE);
        } catch (CustomException e) {
            throw new CustomException("Failed delete: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed delete: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public boolean doesBelongToCustomer(CustomerSQL customerToUpdate, CustomerSQL customerFromHeader) {
        return customerToUpdate.getId() == customerFromHeader.getId();
    }

    public ResponseEntity<CustomerSQL> update(int id, CustomerUpdateDTO customerUpdateDTO, CustomerSQL customer) {
        try {
            CustomerSQL customerSQL = getById(String.valueOf(id)).getBody();

            assert customerSQL != null;
            if (doesBelongToCustomer(customerSQL, customer)) {

                Class<?> customerDtoClass = customerUpdateDTO.getClass();
                Field[] fields = customerDtoClass.getDeclaredFields();

                for (Field field : fields) {

                    field.setAccessible(true);

                    String fieldName = field.getName();
                    Object fieldValue = field.get(customerUpdateDTO);

                    if (fieldValue != null) {

                        Field carField = SellerSQL.class.getDeclaredField(fieldName);

                        carField.setAccessible(true);
                        carField.set(customerSQL, fieldValue);
                    }
                }
            } else {
                throw new CustomException("Error.Update_fail: The car does not belong to seller", HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(customerDaoSQL.save(customerSQL), HttpStatus.ACCEPTED);
        } catch (CustomException e) {
            throw new CustomException("Failed update: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed update: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public void updateAvatar(int id, String fileName) {
        try {
            CustomerSQL customerSQL = getById(String.valueOf(id)).getBody();
            assert customerSQL != null;
            customerSQL.setAvatar(fileName);

            customerDaoSQL.save(customerSQL);
        } catch (CustomException e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

}
