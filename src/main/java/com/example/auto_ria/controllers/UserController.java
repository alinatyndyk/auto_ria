package com.example.auto_ria.controllers;

import com.example.auto_ria.dto.updateDTO.UserUpdateDTO;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.AdministratorSQL;
import com.example.auto_ria.models.ManagerSQL;
import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.services.CommonService;
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
@RequestMapping(value = "sellers")
public class UserController {

    private UsersServiceMySQLImpl usersServiceMySQL;
    private CommonService commonService;

    @GetMapping("/page/{page}")
    public ResponseEntity<Page<SellerSQL>> getAll(
            @PathVariable("page") int page
    ) {
        return usersServiceMySQL.getAll(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SellerSQL> getById(@PathVariable("id") int id) {
        return usersServiceMySQL.getById(String.valueOf(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SellerSQL> patchSeller(@PathVariable int id,
                                                 @ModelAttribute UserUpdateDTO partialUser,
                                                 HttpServletRequest request) throws NoSuchFieldException,
            IllegalAccessException {
        SellerSQL seller = commonService.extractSellerFromHeader(request);
        SellerSQL sellerById = usersServiceMySQL.getById(id);
        if (seller != null && seller.getId() != sellerById.getId()) {
            throw new CustomException("Failed. Check credentials", HttpStatus.FORBIDDEN);
        }
        return usersServiceMySQL.update(id, partialUser, seller);
    }

    @PatchMapping("/change-avatar/{id}")
    public ResponseEntity<String> patchAvatar(@PathVariable int id,
                                              @RequestParam("avatar") MultipartFile avatar,
                                              HttpServletRequest request) throws IOException {
        AdministratorSQL administrator = commonService.extractAdminFromHeader(request);
        SellerSQL seller = commonService.extractSellerFromHeader(request);
        assert seller != null;
        if (seller.getId() != id || administrator == null) {
            throw new CustomException("Illegal_access_exception. No-permission: check credentials", HttpStatus.FORBIDDEN);
        }
        String fileName = avatar.getOriginalFilename();
        usersServiceMySQL.transferAvatar(avatar, fileName);
        usersServiceMySQL.updateAvatar(id, fileName);
        return ResponseEntity.ok("Success. Avatar_updated");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable String id, HttpServletRequest request) {
        SellerSQL seller = commonService.extractSellerFromHeader(request);

        ManagerSQL manager = commonService.extractManagerFromHeader(request);
        AdministratorSQL administrator = commonService.extractAdminFromHeader(request);

        if (administrator == null && manager == null) {
            if (seller == null || !Integer.valueOf(id).equals(seller.getId())) {
                throw new CustomException("Illegal_access_exception. No-permission", HttpStatus.FORBIDDEN);
            }
        }
        return usersServiceMySQL.deleteById(id, seller, administrator, manager);
    }

}
