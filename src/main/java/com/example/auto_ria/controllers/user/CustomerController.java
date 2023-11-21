package com.example.auto_ria.controllers.user;

import com.example.auto_ria.dto.updateDTO.CustomerUpdateDTO;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.responses.user.CustomerResponse;
import com.example.auto_ria.models.user.AdministratorSQL;
import com.example.auto_ria.models.user.CustomerSQL;
import com.example.auto_ria.models.user.ManagerSQL;
import com.example.auto_ria.services.*;
import com.example.auto_ria.services.user.AdministratorServiceMySQL;
import com.example.auto_ria.services.user.CustomersServiceMySQL;
import com.example.auto_ria.services.user.ManagerServiceMySQL;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@RestController
@AllArgsConstructor
@RequestMapping(value = "customers")
public class CustomerController {

    private CustomersServiceMySQL customersServiceMySQL;
    private CommonService commonService;
    private UsersServiceMySQLImpl usersServiceMySQL;
    private AdministratorServiceMySQL administratorServiceMySQL;
    private ManagerServiceMySQL managerServiceMySQL;

    @GetMapping("/page/{page}")
    public ResponseEntity<Page<CustomerResponse>> getAll(
            @PathVariable("page") int page
    ) {
        try {
            return customersServiceMySQL.getAll(page);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable("id") int id) {
        try {
            return customersServiceMySQL.getByIdAsResponse(id);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CustomerSQL> patchCustomer(@PathVariable int id,
                                                     @ModelAttribute CustomerUpdateDTO partialUser,
                                                     HttpServletRequest request) {
        try {
            CustomerSQL customerSQL = commonService.extractCustomerFromHeader(request);
            customersServiceMySQL.checkCredentials(request, id);

            return customersServiceMySQL.update(id, partialUser, customerSQL);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PatchMapping("/change-avatar/{id}")
    public ResponseEntity<String> patchAvatar(@PathVariable int id,
                                              @RequestParam("avatar") MultipartFile avatar,
                                              HttpServletRequest request) {
        try {
            if (administratorServiceMySQL.getById(String.valueOf(id)).getBody() == null
                    || managerServiceMySQL.getById(id).getBody() == null) {
                customersServiceMySQL.checkCredentials(request, id);
            }

            commonService.removeAvatar(Objects.requireNonNull(customersServiceMySQL.getById(String.valueOf(id)).getBody()).getAvatar());

            String fileName = avatar.getOriginalFilename();
            usersServiceMySQL.transferAvatar(avatar, fileName);
            customersServiceMySQL.updateAvatar(id, fileName);
            return ResponseEntity.ok("Success. Avatar_updated");
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable String id, HttpServletRequest request) {
        try {
            CustomerSQL customerSQL = commonService.extractCustomerFromHeader(request);

            ManagerSQL manager = commonService.extractManagerFromHeader(request);
            AdministratorSQL administrator = commonService.extractAdminFromHeader(request);

            if (administrator == null && manager == null) {
                if (customerSQL == null || !Integer.valueOf(id).equals(customerSQL.getId())) {
                    throw new CustomException("Illegal_access_exception. No-permission", HttpStatus.FORBIDDEN);
                }
            }

            commonService.removeAvatar(customerSQL.getAvatar());

            return customersServiceMySQL.deleteById(id, customerSQL, administrator, manager);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

}
