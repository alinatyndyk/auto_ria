package com.example.auto_ria.controllers;

import com.example.auto_ria.dto.updateDTO.CustomerUpdateDTO;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.AdministratorSQL;
import com.example.auto_ria.models.CustomerSQL;
import com.example.auto_ria.models.ManagerSQL;
import com.example.auto_ria.services.CommonService;
import com.example.auto_ria.services.CustomersServiceMySQL;
import com.example.auto_ria.services.UsersServiceMySQLImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@AllArgsConstructor
@RequestMapping(value = "customers")
public class CustomerController {

    private CustomersServiceMySQL customersServiceMySQL;
    private CommonService commonService;
    private UsersServiceMySQLImpl usersServiceMySQL;

    @GetMapping("/page/{page}")
    public ResponseEntity<Page<CustomerSQL>> getAll(
            @PathVariable("page") int page
    ) {
        return customersServiceMySQL.getAll(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerSQL> getById(@PathVariable("id") int id) {
        return customersServiceMySQL.getById(String.valueOf(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CustomerSQL> patchCustomer(@PathVariable int id,
                                                     @ModelAttribute CustomerUpdateDTO partialUser,
                                                     HttpServletRequest request) throws NoSuchFieldException,
            IllegalAccessException {
        CustomerSQL customerSQL = commonService.extractCustomerFromHeader(request);
        return customersServiceMySQL.update(id, partialUser, customerSQL);
    }

    @PatchMapping("/change-avatar/{id}")
    public ResponseEntity<String> patchAvatar(@PathVariable int id,
                                              @RequestParam("avatar") MultipartFile avatar,
                                              HttpServletRequest request) throws IOException {
        CustomerSQL customerSQL = commonService.extractCustomerFromHeader(request);
        AdministratorSQL administrator = commonService.extractAdminFromHeader(request);
        assert customerSQL != null;
        if (customerSQL.getId() != id || administrator == null) {
            throw new CustomException("Illegal_access_exception. No-permission: check credentials", HttpStatus.FORBIDDEN);
        }
        String fileName = avatar.getOriginalFilename();
        usersServiceMySQL.transferAvatar(avatar, fileName);
        customersServiceMySQL.updateAvatar(id, fileName);
        return ResponseEntity.ok("Success. Avatar_updated");
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable String id, HttpServletRequest request) {
        CustomerSQL customerSQL = commonService.extractCustomerFromHeader(request);
        AdministratorSQL administratorSQL = commonService.extractAdminFromHeader(request);

        ManagerSQL manager = commonService.extractManagerFromHeader(request);

        if (!Integer.valueOf(id).equals(customerSQL.getId())
                || !manager.getRoles().contains(ERole.MANAGER_GLOBAL)
                || !administratorSQL.getRoles().contains(ERole.ADMIN_GLOBAL)) {
            throw new CustomException("Illegal_access_exception. No-permission", HttpStatus.FORBIDDEN);
        }

        return customersServiceMySQL.deleteById(id);
    }

}
