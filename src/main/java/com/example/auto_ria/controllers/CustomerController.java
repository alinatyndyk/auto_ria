package com.example.auto_ria.controllers;

import com.example.auto_ria.dto.updateDTO.CustomerUpdateDTO;
import com.example.auto_ria.models.CustomerSQL;
import com.example.auto_ria.services.CommonService;
import com.example.auto_ria.services.CustomersServiceMySQL;
import com.example.auto_ria.services.UsersServiceMySQLImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

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
        customersServiceMySQL.checkCredentials(request, id);

        return customersServiceMySQL.update(id, partialUser, customerSQL);
    }

    @PatchMapping("/change-avatar/{id}")
    public ResponseEntity<String> patchAvatar(@PathVariable int id,
                                              @RequestParam("avatar") MultipartFile avatar,
                                              HttpServletRequest request) throws IOException {
        customersServiceMySQL.checkCredentials(request, id);

        commonService.removeAvatar(Objects.requireNonNull(customersServiceMySQL.getById(String.valueOf(id)).getBody()).getAvatar());

        String fileName = avatar.getOriginalFilename();
        usersServiceMySQL.transferAvatar(avatar, fileName);
        customersServiceMySQL.updateAvatar(id, fileName);
        return ResponseEntity.ok("Success. Avatar_updated");
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable String id, HttpServletRequest request) throws IOException {
        customersServiceMySQL.checkCredentials(request, Integer.parseInt(id));

        commonService.removeAvatar(Objects.requireNonNull(customersServiceMySQL.getById(id).getBody()).getAvatar());

        return customersServiceMySQL.deleteById(id);
    }

}
