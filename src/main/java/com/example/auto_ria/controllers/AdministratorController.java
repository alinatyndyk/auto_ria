package com.example.auto_ria.controllers;

import com.example.auto_ria.dto.updateDTO.AdministratorUpdateDTO;
import com.example.auto_ria.enums.EMail;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.mail.FMService;
import com.example.auto_ria.models.AdministratorSQL;
import com.example.auto_ria.services.AdministratorServiceMySQL;
import com.example.auto_ria.services.CommonService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

@RestController
@AllArgsConstructor
@RequestMapping(value = "administrators")
public class AdministratorController {

    private AdministratorServiceMySQL administratorServiceMySQL;
    private CommonService commonService;
    private FMService mailer;

    @GetMapping("/page/{page}")
    public ResponseEntity<Page<AdministratorSQL>> getAll(
            @PathVariable("page") int page
    ) {
        return administratorServiceMySQL.getAll(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdministratorSQL> getById(@PathVariable("id") int id) {
        return administratorServiceMySQL.getById(String.valueOf(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AdministratorSQL> patchAdmin(@PathVariable int id,
                                                       @ModelAttribute @Valid AdministratorUpdateDTO partialUser,
                                                       HttpServletRequest request)
            throws NoSuchFieldException,
            IllegalAccessException {
        AdministratorSQL administratorHeader = commonService.extractAdminFromHeader(request);
        AdministratorSQL administratorSQL = administratorServiceMySQL.getById(String.valueOf(id)).getBody();
        if (administratorHeader.getId() != id) {
            throw new CustomException("Illegal_access_exception. No-permission", HttpStatus.FORBIDDEN);
        }

        return administratorServiceMySQL.update(partialUser, administratorSQL);
    }

    @PatchMapping("/change-avatar/{id}")
    public ResponseEntity<String> patchAvatar(@PathVariable int id,
                                              @RequestParam("avatar") MultipartFile avatar,
                                              HttpServletRequest request) throws IOException {
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
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable int id, HttpServletRequest request) throws IOException {
        AdministratorSQL administratorHeader = commonService.extractAdminFromHeader(request);

        if (administratorHeader.getId() != id) {
            throw new CustomException("Illegal_access_exception. No-permission", HttpStatus.FORBIDDEN);
        }

        commonService.removeAvatar(administratorHeader.getAvatar());

        return administratorServiceMySQL.deleteById(String.valueOf(id));
    }

    @GetMapping("/register-entity")
    public ResponseEntity<String> managerAuthKey(@RequestParam("email")
                                                 @Pattern(regexp = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$",
                                                         message = "Invalid email") String email,
                                                 @RequestParam("role") ERole role) {
        HashMap<String, Object> vars = new HashMap<>();
        vars.put("role", role.name().toLowerCase());
        vars.put("link", "frontend/" + Objects.requireNonNull(administratorServiceMySQL.generateAuthKey(email, role).getBody()).getRegisterKey());

        try {
            mailer.sendEmail(email, EMail.REGISTER_KEY, vars);
        } catch (Exception e) {
            throw new CustomException("Error sending email. Attempt later...", HttpStatus.EXPECTATION_FAILED);
        }
        return ResponseEntity.ok("Email sent to " + email);
    }

}
