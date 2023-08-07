package com.example.auto_ria.controllers;

import com.example.auto_ria.dto.UserDTO;
import com.example.auto_ria.models.Seller;
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
//    @JsonView(ViewsUser.NoSL.class)
    public ResponseEntity<List<Seller>> getAll() {

        return usersServiceMySQL.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Seller> getById(@PathVariable("id") int id) {
        return usersServiceMySQL.getById(String.valueOf(id));
    }

    @SneakyThrows
    @PatchMapping("/{id}")
    public ResponseEntity<Seller> patchCar(@PathVariable int id,
                                           @ModelAttribute UserDTO partialUser,
                                           HttpServletRequest request) {
        Seller seller = usersServiceMySQL.extractSellerFromHeader(request);
        return usersServiceMySQL.update(id, partialUser, seller);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable String id) {
        return usersServiceMySQL.deleteById(id);
    }

}
