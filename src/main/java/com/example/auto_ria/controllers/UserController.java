package com.example.auto_ria.controllers;

import com.example.auto_ria.dto.UserDTO;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.models.Manager;
import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.responses.ErrorResponse;
import com.example.auto_ria.services.UsersServiceMySQLImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "sellers")
public class UserController {

    private UsersServiceMySQLImpl usersServiceMySQL;

    @GetMapping()
//    @JsonView(ViewsUser.NoSL.class) //todo jsonView
    public ResponseEntity<List<SellerSQL>> getAll() {

        return usersServiceMySQL.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SellerSQL> getById(@PathVariable("id") int id) {
        return usersServiceMySQL.getById(String.valueOf(id));
    }

    @SneakyThrows
    @PatchMapping("/{id}")
    public ResponseEntity<SellerSQL> patchCar(@PathVariable int id,
                                              @ModelAttribute UserDTO partialUser,
                                              HttpServletRequest request) {
        SellerSQL seller = usersServiceMySQL.extractSellerFromHeader(request);
        return usersServiceMySQL.update(id, partialUser, seller);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable String id, HttpServletRequest request) throws ErrorResponse {
        SellerSQL seller = usersServiceMySQL.extractSellerFromHeader(request);

        Manager manager = usersServiceMySQL.extractManagerFromHeader(request);

        if (!Integer.valueOf(id).equals(seller.getId()) || !manager.getRoles().contains(ERole.MANAGER_GLOBAL)) {
            throw new ErrorResponse(403, "Illegal_access_exception. No-permission");
        }

        return usersServiceMySQL.deleteById(id);
    }

}
