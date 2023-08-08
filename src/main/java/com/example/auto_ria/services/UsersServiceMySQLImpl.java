package com.example.auto_ria.services;

import com.example.auto_ria.dao.UserDaoSQL;
import com.example.auto_ria.dto.UserDTO;
import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.responses.ErrorResponse;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
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

    private UserDaoSQL userDaoSQL;
    private JwtService jwtService;

    @SneakyThrows
    public SellerSQL extractSellerFromHeader(HttpServletRequest request) {
        String bearerToken = jwtService.extractTokenFromHeader(request);

        String email = jwtService.extractUsername(bearerToken);

        SellerSQL seller = userDaoSQL.findSellerByEmail(email);
        System.out.println(seller);
        return seller;
    }


    public ResponseEntity<List<SellerSQL>> getAll() {
        return new ResponseEntity<>(userDaoSQL.findAll(), HttpStatus.ACCEPTED);
    }


    public ResponseEntity<SellerSQL> getById(String id) {
        SellerSQL user = userDaoSQL.findById(Integer.parseInt(id)).get();
        return new ResponseEntity<>(user, HttpStatus.ACCEPTED);
    }

    @SneakyThrows
    public void transferAvatar(MultipartFile picture, String originalFileName) {
        String path = System.getProperty("user.home") + File.separator + "springboot-lib" + File.separator + originalFileName;
        File transferDestinationFile = new File(path);
        picture.transferTo(transferDestinationFile);
    }

    public ResponseEntity<String> deleteById(String id, SellerSQL seller) {
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

//    public ResponseEntity<UserSQL> getByEmail(String email) {
//        return new ResponseEntity<>(userDAO.findSellerByEmail(email), HttpStatus.GONE);
//    }

}
