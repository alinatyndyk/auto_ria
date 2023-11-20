package com.example.auto_ria.controllers.user;

import com.example.auto_ria.dto.updateDTO.ManagerUpdateDTO;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.user.ManagerSQL;
import com.example.auto_ria.services.user.AdministratorServiceMySQL;
import com.example.auto_ria.services.CommonService;
import com.example.auto_ria.services.user.ManagerServiceMySQL;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@RestController
@AllArgsConstructor
@RequestMapping(value = "managers")
public class ManagerController {

    private ManagerServiceMySQL managerServiceMySQL;
    private UsersServiceMySQLImpl usersServiceMySQL;
    private CommonService commonService;
    private AdministratorServiceMySQL administratorServiceMySQL;

    @GetMapping("/page/{page}")
    public ResponseEntity<Page<ManagerSQL>> getAll(
            @PathVariable("page") int page
    ) {
        try {
            return managerServiceMySQL.getAll(page);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManagerSQL> getById(@PathVariable("id") int id) {
        try {
            return managerServiceMySQL.getById(id);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ManagerSQL> patch(
            HttpServletRequest request,
            @PathVariable int id,
            @RequestBody ManagerUpdateDTO partial) {
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
