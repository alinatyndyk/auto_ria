package com.example.auto_ria.controllers.user;

import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.auto_ria.dto.updateDTO.AdministratorUpdateDTO;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.responses.user.AdminResponse;
import com.example.auto_ria.models.responses.user.UserResponse;
import com.example.auto_ria.models.user.AdministratorSQL;
import com.example.auto_ria.services.CommonService;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping(value = "administrators")
public class AdministratorController {

    private UsersServiceMySQLImpl userServiceMySQL;
    private CommonService commonService;

    @GetMapping("/page/{page}")
    public ResponseEntity<Page<UserResponse>> getAll(
            @PathVariable("page") int page) {
        try {
            return userServiceMySQL.findAllByRole(ERole.ADMIN, page);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    // @GetMapping("/{id}")
    // public ResponseEntity<AdminResponse> getById(@PathVariable("id") int id) {
    //     try {
    //         return administratorServiceMySQL.getByIdAsResponse(id);
    //     } catch (CustomException e) {
    //         throw new CustomException(e.getMessage(), e.getStatus());
    //     }
    // }

    @PatchMapping("/{id}")
    public ResponseEntity<AdministratorSQL> patchAdmin(@PathVariable int id,
            @ModelAttribute @Valid AdministratorUpdateDTO partialUser,
            HttpServletRequest request) {
        try {
            AdministratorSQL administratorHeader = commonService.extractAdminFromHeader(request);
            AdministratorSQL administratorSQL = administratorServiceMySQL.getById(String.valueOf(id)).getBody();
            if (administratorHeader.getId() != id) {
                throw new CustomException("Illegal_access_exception. No-permission", HttpStatus.FORBIDDEN);
            }

            return administratorServiceMySQL.update(partialUser, administratorSQL);

        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PatchMapping("/change-avatar/{id}")
    public ResponseEntity<String> patchAvatar(@PathVariable int id,
            @RequestParam("avatar") MultipartFile avatar,
            HttpServletRequest request) {
        try {
            AdministratorSQL administratorHeader = commonService.extractAdminFromHeader(request);
            administratorServiceMySQL.getById(String.valueOf(id));

            if (administratorHeader.getId() != id) {
                throw new CustomException("Illegal_access_exception. No-permission", HttpStatus.FORBIDDEN);
            }

            commonService.removeAvatar(administratorHeader.getAvatar());

            String fileName = avatar.getOriginalFilename();
            administratorServiceMySQL.transferAvatar(avatar, fileName);
            administratorServiceMySQL.updateAvatar(id, fileName);
            return ResponseEntity.ok("Success. Avatar_updated");
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable int id, HttpServletRequest request) throws IOException {
        try {
            AdministratorSQL administratorHeader = commonService.extractAdminFromHeader(request);

            if (administratorHeader.getId() != id) {
                throw new CustomException("Illegal_access_exception. No-permission", HttpStatus.FORBIDDEN);
            }

            commonService.removeAvatar(administratorHeader.getAvatar());

            return administratorServiceMySQL.deleteById(String.valueOf(id));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

}
