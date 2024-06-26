package com.example.auto_ria.controllers.user;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.auto_ria.dto.updateDTO.UserUpdateDTO;
import com.example.auto_ria.enums.EMail;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.responses.user.UserResponse;
import com.example.auto_ria.models.user.UserSQL;
import com.example.auto_ria.services.CommonService;
import com.example.auto_ria.services.auth.AuthenticationService;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping(value = "users")
public class UserController {

    private UsersServiceMySQLImpl usersServiceMySQL;
    private CommonService commonService;
    private AuthenticationService authenticationService;

    @PreAuthorize("hasRole('ADMIN', 'MANAGER')")
    @GetMapping("/page/{page}")
    public ResponseEntity<Page<UserResponse>> getAll(
            @PathVariable("page") int page) {
        try {
            return usersServiceMySQL.getAll(page);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PreAuthorize("hasRole('ADMIN', 'MANAGER', 'USER')")
    @GetMapping("/by-token/{token}")
    public ResponseEntity<UserResponse> getByAccessToken(@PathVariable("token") String token) {
        try {
            return new ResponseEntity<>(authenticationService.getByToken(token), HttpStatus.ACCEPTED);

        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PreAuthorize("hasRole('ADMIN', 'MANAGER', 'USER')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable("id") int id) {
        try {
            return usersServiceMySQL.getByIdAsResponse(id);

        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PreAuthorize("hasRole('ADMIN', 'MANAGER', 'USER')")
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> patchUser(@PathVariable int id,
            @RequestBody UserUpdateDTO partialUser,
            HttpServletRequest request) {
        try {

            UserSQL user;
            UserSQL userById = usersServiceMySQL.getById(id);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                user = usersServiceMySQL.getByEmail(userDetails.getUsername());
                if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"))) {
                    return usersServiceMySQL.update(id, partialUser, userById);

                } else if (userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("MANAGER"))
                        && userDetails.getUsername().equals(userById.getEmail())) {

                    return usersServiceMySQL.update(id, partialUser, user);

                } else if (userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("USER"))
                        && userDetails.getUsername().equals(userById.getEmail())) {

                    return usersServiceMySQL.update(id, partialUser, user);

                } else {
                    throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
                }
            } else {
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
            }

        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PreAuthorize("hasRole('ADMIN', 'MANAGER', 'USER')")
    @PatchMapping("/change-avatar/{id}")
    public ResponseEntity<String> patchAvatar(@PathVariable int id,
            @RequestParam("avatar") MultipartFile avatar,
            HttpServletRequest request) {
        try {
            UserSQL userById = usersServiceMySQL.getById(id);

            assert userById != null;

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"))) {
                    commonService.removeAvatar(userById.getAvatar());

                    String fileName = avatar.getOriginalFilename();
                    usersServiceMySQL.transferAvatar(avatar, fileName);
                    usersServiceMySQL.updateAvatar(id, fileName);

                    return ResponseEntity.ok("Success. Avatar_updated");

                } else if (userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("MANAGER"))
                        && userDetails.getUsername().equals(userById.getEmail())) {

                    commonService.removeAvatar(userById.getAvatar());

                    String fileName = avatar.getOriginalFilename();
                    usersServiceMySQL.transferAvatar(avatar, fileName);
                    usersServiceMySQL.updateAvatar(id, fileName);

                    return ResponseEntity.ok("Success. Avatar_updated");

                } else if (userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("USER"))
                        && userDetails.getUsername().equals(userById.getEmail())) {

                    commonService.removeAvatar(userById.getAvatar());

                    String fileName = avatar.getOriginalFilename();
                    usersServiceMySQL.transferAvatar(avatar, fileName);
                    usersServiceMySQL.updateAvatar(id, fileName);

                    return ResponseEntity.ok("Success. Avatar_updated");

                } else {
                    throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
                }
            } else {
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
            }

        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PreAuthorize("hasRole('ADMIN', 'MANAGER', 'USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable String id, HttpServletRequest request) {
        try {
            UserSQL user = usersServiceMySQL.getById(Integer.valueOf(id));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                if (userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ADMIN"))) {
                    if (userDetails.getUsername().equals(user.getEmail())
                            && usersServiceMySQL.countByRole(ERole.ADMIN) == 1) {
                        throw new CustomException("At least one Administator has to remain in the DB",
                                HttpStatus.UNAUTHORIZED);
                    }
                    commonService.removeAvatar(user.getAvatar());

                    return usersServiceMySQL.deleteById(id, user, EMail.YOUR_ACCOUNT_BANNED);

                } else if (userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("MANAGER"))) {
                    if (userDetails.getUsername().equals(user.getEmail())) {
                        commonService.removeAvatar(user.getAvatar());

                        return usersServiceMySQL.deleteById(id, user, EMail.PLATFORM_LEAVE);
                    } else {
                        throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
                    }

                } else if (userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("USER"))
                        && userDetails.getUsername().equals(user.getEmail())) {

                    commonService.removeAvatar(user.getAvatar());

                    return usersServiceMySQL.deleteById(id, user, EMail.PLATFORM_LEAVE);

                } else {
                    throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
                }
            } else {
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
            }

        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

}
