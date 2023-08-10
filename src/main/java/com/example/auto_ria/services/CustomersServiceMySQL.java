package com.example.auto_ria.services;

import com.example.auto_ria.dao.CustomerDaoSQL;
import com.example.auto_ria.dto.updateDTO.CustomerUpdateDTO;
import com.example.auto_ria.models.CustomerSQL;
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
public class CustomersServiceMySQL {

    private CustomerDaoSQL customerDaoSQL;

    public ResponseEntity<List<CustomerSQL>> getAll() {
        return new ResponseEntity<>(customerDaoSQL.findAll(), HttpStatus.ACCEPTED);
    }


    public ResponseEntity<CustomerSQL> getById(String id) {
        CustomerSQL user = customerDaoSQL.findById(Integer.parseInt(id)).get();
        return new ResponseEntity<>(user, HttpStatus.ACCEPTED);
    }

    public void transferAvatar(MultipartFile picture, String originalFileName) throws java.io.IOException {
        String path = System.getProperty("user.home") + File.separator + "springboot-lib" + File.separator + originalFileName;
        File transferDestinationFile = new File(path);
        picture.transferTo(transferDestinationFile);
    }

    public ResponseEntity<String> deleteById(String id) {
        customerDaoSQL.deleteById(Integer.valueOf(id));
        return new ResponseEntity<>("Success.User_deleted", HttpStatus.GONE);
    }

    public boolean doesBelongToCustomer(CustomerSQL customerToUpdate, CustomerSQL customerFromHeader) {
        return customerToUpdate.getId() == customerFromHeader.getId();
    }

    public ResponseEntity<CustomerSQL> update(int id, CustomerUpdateDTO customerUpdateDTO, CustomerSQL customer) throws IllegalAccessException, IOException, ErrorResponse, NoSuchFieldException {

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
            throw new ErrorResponse(403, "Error.Update_fail: The car does not belong to seller");  //todo normal error
        }
        return new ResponseEntity<>(customerDaoSQL.save(customerSQL), HttpStatus.ACCEPTED);
    }

    public void updateAvatar(int id, String fileName) {

        CustomerSQL customerSQL = getById(String.valueOf(id)).getBody();
        assert customerSQL != null;
        customerSQL.setAvatar(fileName);

        customerDaoSQL.save(customerSQL);
    }

}
