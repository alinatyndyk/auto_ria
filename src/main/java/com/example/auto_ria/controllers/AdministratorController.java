package com.example.auto_ria.controllers;

import com.example.auto_ria.dto.updateDTO.AdministratorUpdateDTO;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.AdministratorSQL;
import com.example.auto_ria.services.AdministratorServiceMySQL;
import com.example.auto_ria.services.UsersServiceMySQLImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "administrators")
public class AdministratorController {

    private AdministratorServiceMySQL administratorServiceMySQL;
    private UsersServiceMySQLImpl usersServiceMySQL;

    @GetMapping()
    public ResponseEntity<List<AdministratorSQL>> getAll() {
        return administratorServiceMySQL.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdministratorSQL> getById(@PathVariable("id") int id) {
        return administratorServiceMySQL.getById(String.valueOf(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AdministratorSQL> patchAdmin(@PathVariable int id,
                                                       @ModelAttribute AdministratorUpdateDTO partialUser)
            throws NoSuchFieldException,
            IllegalAccessException {
        return administratorServiceMySQL.update(id, partialUser);
    }

    @PatchMapping("/change-avatar/{id}")
    public ResponseEntity<String> patchAvatar(@PathVariable int id,
                                              @RequestParam("avatar") MultipartFile avatar,
                                              HttpServletRequest request) throws IOException {
        AdministratorSQL administratorHeader = usersServiceMySQL.extractAdminFromHeader(request);
        assert administratorHeader != null;
        if (administratorHeader.getId() != id) {
            throw new CustomException("Illegal_access_exception. No-permission", HttpStatus.FORBIDDEN);
        }
        String fileName = avatar.getOriginalFilename();
        administratorServiceMySQL.transferAvatar(avatar, fileName);
        administratorServiceMySQL.updateAvatar(id, fileName);
        return ResponseEntity.ok("Success. Avatar_updated");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable String id) {
        return usersServiceMySQL.deleteById(id);
    }

}
