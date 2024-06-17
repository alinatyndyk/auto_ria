package com.example.auto_ria.controllers.user;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.auto_ria.dto.updateDTO.ManagerUpdateDTO;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.responses.user.ManagerResponse;
import com.example.auto_ria.models.responses.user.UserResponse;
import com.example.auto_ria.services.CommonService;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping(value = "managers")
public class ManagerController {

    private UsersServiceMySQLImpl usersServiceMySQL;
    private CommonService commonService;

    @PreAuthorize("hasRole('ADMIN', 'MANAGER')")
    @GetMapping("/page/{page}")
    public ResponseEntity<Page<UserResponse>> getAll(
            @PathVariable("page") int page) {
        try {
            return usersServiceMySQL.findAllByRole(ERole.MANAGER, page);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ManagerSQL> patch(
            HttpServletRequest request,
            @PathVariable int id,
            @RequestBody UserUp partial) {
        try {
            managerServiceMySQL.checkCredentials(request, id);
            return managerServiceMySQL.update(id, partial);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PatchMapping("/change-avatar/{id}")
    public ResponseEntity<String> patchAvatar(@PathVariable int id,
            @RequestParam("avatar") MultipartFile avatar,
            HttpServletRequest request) {
        try {
            managerServiceMySQL.checkCredentials(request, id);

            commonService.removeAvatar(Objects.requireNonNull(managerServiceMySQL.getById(id).getBody()).getAvatar());

            String fileName = avatar.getOriginalFilename();
            usersServiceMySQL.transferAvatar(avatar, fileName);
            managerServiceMySQL.updateAvatar(id, fileName);
            return ResponseEntity.ok("Success. Avatar_updated");
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable int id,
            HttpServletRequest request) {
        try {
            if (administratorServiceMySQL.getById(String.valueOf(id)).getBody() == null) {
                managerServiceMySQL.checkCredentials(request, id);
            }
            commonService.removeAvatar(Objects.requireNonNull(managerServiceMySQL.getById(id).getBody()).getAvatar());
            return managerServiceMySQL.deleteById(id);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

}
