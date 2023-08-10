package com.example.auto_ria.services;

import com.example.auto_ria.dao.AdministratorDaoSQL;
import com.example.auto_ria.dao.CustomerDaoSQL;
import com.example.auto_ria.dao.ManagerDaoSQL;
import com.example.auto_ria.dao.UserDaoSQL;
import com.example.auto_ria.dto.UserDTO;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.models.AdministratorSQL;
import com.example.auto_ria.models.CustomerSQL;
import com.example.auto_ria.models.ManagerSQL;
import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.responses.ErrorResponse;
import com.example.auto_ria.services.serviceInterfaces.UsersService;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
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
public class UsersServiceMySQLImpl implements UsersService {

    private AdministratorDaoSQL administratorDaoSQL;
    private ManagerDaoSQL managerDaoSQL;
    private UserDaoSQL userDaoSQL;
    private CustomerDaoSQL customerDaoSQL;
    private JwtService jwtService;


    public String extractEmailFromHeader(HttpServletRequest request, ERole role) {
        String bearerToken = jwtService.extractTokenFromHeader(request);

        return jwtService.extractUsername(bearerToken, role);
    }

    public SellerSQL extractSellerFromHeader(HttpServletRequest request) {
        return userDaoSQL.findSellerByEmail(extractEmailFromHeader(request, ERole.SELLER));
    }

    public ManagerSQL extractManagerFromHeader(HttpServletRequest request) {
        return managerDaoSQL.findByEmail(extractEmailFromHeader(request, ERole.MANAGER));
    }

    public AdministratorSQL extractAdminFromHeader(HttpServletRequest request) {
        return administratorDaoSQL.findByEmail(extractEmailFromHeader(request, ERole.ADMIN));
    }

    public CustomerSQL extractCustomerFromHeader(HttpServletRequest request) {
        return customerDaoSQL.findByEmail(extractEmailFromHeader(request, ERole.CUSTOMER));
    }


    public ResponseEntity<List<SellerSQL>> getAll() {
        return new ResponseEntity<>(userDaoSQL.findAll(), HttpStatus.ACCEPTED);
    }


    public ResponseEntity<SellerSQL> getById(String id) {
        SellerSQL user = userDaoSQL.findById(Integer.parseInt(id)).get();
        return new ResponseEntity<>(user, HttpStatus.ACCEPTED);
    }

    public void transferAvatar(MultipartFile picture, String originalFileName) throws java.io.IOException {
        String path = System.getProperty("user.home") + File.separator + "springboot-lib" + File.separator + originalFileName;
        File transferDestinationFile = new File(path);
        picture.transferTo(transferDestinationFile);
    }

    public ResponseEntity<String> deleteById(String id) {
        userDaoSQL.deleteById(Integer.valueOf(id));
        return new ResponseEntity<>("Success.User_deleted", HttpStatus.GONE);
    }

    public boolean doesBelongToSeller(SellerSQL seller, SellerSQL seller1) {
        return seller.getId() == seller1.getId();
    }

    public ResponseEntity<SellerSQL> update(int id, UserDTO userDTO, SellerSQL seller) throws IllegalAccessException, IOException, ErrorResponse, NoSuchFieldException {

        SellerSQL seller1 = getById(String.valueOf(id)).getBody();

        assert seller1 != null;
        if (doesBelongToSeller(seller, seller1)) {

            Class<?> carDTOClass = userDTO.getClass();
            Field[] fields = carDTOClass.getDeclaredFields();

            for (Field field : fields) {

                field.setAccessible(true);

                String fieldName = field.getName();
                System.out.println(fieldName);
                Object fieldValue = field.get(userDTO);

                if (fieldValue != null) {

                    Field carField = SellerSQL.class.getDeclaredField(fieldName);

                    carField.setAccessible(true);
                    carField.set(seller1, fieldValue);
                }
            }
        } else {
            throw new ErrorResponse(403, "Error.Update_fail: The car does not belong to seller");  //todo normal error
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