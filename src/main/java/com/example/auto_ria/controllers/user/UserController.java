package com.example.auto_ria.controllers.user;

import com.example.auto_ria.dto.updateDTO.UserUpdateDTO;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.responses.user.UserResponse;
import com.example.auto_ria.models.user.AdministratorSQL;
import com.example.auto_ria.models.user.ManagerSQL;
import com.example.auto_ria.models.user.UserSQL;
import com.example.auto_ria.services.CommonService;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping(value = "users")
public class UserController {

    private UsersServiceMySQLImpl usersServiceMySQL;
    private CommonService commonService;

    @GetMapping("/page/{page}")
    public ResponseEntity<Page<UserResponse>> getAll(
            @PathVariable("page") int page
    ) {
        try {
            return usersServiceMySQL.getAll(page);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable("id") int id) {
        try {
            return usersServiceMySQL.getByIdAsResponse(id);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserSQL> patchUser(@PathVariable int id,
                                                 @ModelAttribute UserUpdateDTO partialUser,
                                                 HttpServletRequest request) {
        try {
            UserSQL user = commonService.extractUserFromHeader(request);
            UserSQL userById = usersServiceMySQL.getById(id);
            if (user != null && user.getId() != userById.getId()) {
                throw new CustomException("Failed. Check credentials", HttpStatus.FORBIDDEN);
            }
            return usersServiceMySQL.update(id, partialUser, user);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PatchMapping("/change-avatar/{id}")
    public ResponseEntity<String> patchAvatar(@PathVariable int id,
                                              @RequestParam("avatar") MultipartFile avatar,
                                              HttpServletRequest request) {
        try {
            AdministratorSQL administrator = commonService.extractAdminFromHeader(request); //* todo has role adm or user check */
            UserSQL user = commonService.extractUserFromHeader(request);
            assert user != null;
            if (user.getId() != id || administrator == null) {
                throw new CustomException("Illegal_access_exception. No-permission: check credentials", HttpStatus.FORBIDDEN);
            }

            commonService.removeAvatar(user.getAvatar());

            String fileName = avatar.getOriginalFilename();
            usersServiceMySQL.transferAvatar(avatar, fileName);
            usersServiceMySQL.updateAvatar(id, fileName);

            return ResponseEntity.ok("Success. Avatar_updated");
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable String id, HttpServletRequest request) {
        try {
            UserSQL user = commonService.extractUserFromHeader(request);
            ManagerSQL manager = commonService.extractManagerFromHeader(request);
            AdministratorSQL administrator = commonService.extractAdminFromHeader(request); //todo separate or has role

            if (administrator == null && manager == null) {
                if (user == null || !Integer.valueOf(id).equals(user.getId())) {
                    throw new CustomException("Illegal_access_exception. No-permission", HttpStatus.FORBIDDEN);
                }
            }

            commonService.removeAvatar(user.getAvatar());

            return usersServiceMySQL.deleteById(id, user, administrator, manager);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

}
