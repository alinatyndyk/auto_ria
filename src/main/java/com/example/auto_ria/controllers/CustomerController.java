package com.example.auto_ria.controllers;

import com.example.auto_ria.dto.updateDTO.CustomerUpdateDTO;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.models.AdministratorSQL;
import com.example.auto_ria.models.CustomerSQL;
import com.example.auto_ria.models.ManagerSQL;
import com.example.auto_ria.models.responses.ErrorResponse;
import com.example.auto_ria.services.CustomersServiceMySQL;
import com.example.auto_ria.services.UsersServiceMySQLImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "customers")
public class CustomerController {

    private CustomersServiceMySQL customersServiceMySQL;
    private UsersServiceMySQLImpl usersServiceMySQL;

    @GetMapping()
//    @JsonView(ViewsUser.NoSL.class) //todo jsonView
    public ResponseEntity<List<CustomerSQL>> getAll() {
        return customersServiceMySQL.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerSQL> getById(@PathVariable("id") int id) {
        return customersServiceMySQL.getById(String.valueOf(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CustomerSQL> patchCustomer(@PathVariable int id,
                                                     @ModelAttribute CustomerUpdateDTO partialUser,
                                                     HttpServletRequest request) throws NoSuchFieldException,
            IllegalAccessException, ErrorResponse {
        CustomerSQL customerSQL = usersServiceMySQL.extractCustomerFromHeader(request);
        return customersServiceMySQL.update(id, partialUser, customerSQL);
    }

    @PatchMapping("/change-avatar/{id}")
    public ResponseEntity<String> patchAvatar(@PathVariable int id,
                                              @RequestParam("avatar") MultipartFile avatar,
                                              HttpServletRequest request) throws ErrorResponse, IOException {
        CustomerSQL customerSQL = usersServiceMySQL.extractCustomerFromHeader(request);
        AdministratorSQL administrator = usersServiceMySQL.extractAdminFromHeader(request);
        assert customerSQL != null;
        if (customerSQL.getId() != id || administrator == null) {
            throw new ErrorResponse(403, "Illegal_access_exception. No-permission");
        }
        String fileName = avatar.getOriginalFilename();
        usersServiceMySQL.transferAvatar(avatar, fileName);
        customersServiceMySQL.updateAvatar(id, fileName);
        return ResponseEntity.ok("Success. Avatar_updated");
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable String id, HttpServletRequest request) throws ErrorResponse {
        CustomerSQL customerSQL = usersServiceMySQL.extractCustomerFromHeader(request);
        AdministratorSQL administratorSQL = usersServiceMySQL.extractAdminFromHeader(request);  // todo create interceptor for admin check

        ManagerSQL manager = usersServiceMySQL.extractManagerFromHeader(request);

        if (!Integer.valueOf(id).equals(customerSQL.getId())
                || !manager.getRoles().contains(ERole.MANAGER_GLOBAL)
                || !administratorSQL.getRoles().contains(ERole.ADMIN_GLOBAL)) {
            throw new ErrorResponse(403, "Illegal_access_exception. No-permission");
        }

        return usersServiceMySQL.deleteById(id);
    }

}
