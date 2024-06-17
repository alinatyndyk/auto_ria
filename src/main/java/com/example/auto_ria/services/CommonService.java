package com.example.auto_ria.services;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.auto_ria.dao.socket.SessionDaoSQL;
import com.example.auto_ria.dao.user.UserDaoSQL;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.enums.ETokenRole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.responses.user.UserResponse;
import com.example.auto_ria.models.socket.Session;
import com.example.auto_ria.models.user.UserSQL;
import com.example.auto_ria.services.auth.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CommonService {

    private JwtService jwtService;

    private UserDaoSQL userDaoSQL;
    private SessionDaoSQL sessionDaoSQL;

    public void transferAvatar(MultipartFile picture, String originalFileName) {
        try {
            String path = System.getProperty("user.home") + File.separator + "springboot-lib" + File.separator
                    + originalFileName;
            File transferDestinationFile = new File(path);
            picture.transferTo(transferDestinationFile);
        } catch (CustomException e) {
            throw new CustomException("Failed to transfer avatar: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed to transfer avatar: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public void transferPhotos(MultipartFile[] newPictures) {
        try {
            Arrays.stream(newPictures).map(picture -> {
                String fileName = picture.getOriginalFilename();
                transferAvatar(picture, fileName);
                return fileName;
            }).collect(Collectors.toList());
        } catch (CustomException e) {
            throw new CustomException("Failed to transfer photos: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed to transfer photos: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public void removeAvatar(String originalFileName) {
        try {
            String path = System.getProperty("user.home") + File.separator + "springboot-lib" + File.separator
                    + originalFileName;
            File transferDestinationFile = new File(path);
            if (transferDestinationFile.exists()) {
                transferDestinationFile.delete();
            }
        } catch (Exception e) {
            throw new CustomException("Error while handling avatars: " + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public String extractEmailFromHeader(HttpServletRequest request, ETokenRole role) {
        try {
            String bearerToken = jwtService.extractTokenFromHeader(request);
            return jwtService.extractUsername(bearerToken, role);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new CustomException("Please sign in", HttpStatus.UNAUTHORIZED);
        }

    }

    public UserSQL extractUserFromHeader(HttpServletRequest request) {
        UserSQL sellerSQL = null;
        try {
            String email = extractEmailFromHeader(request, ETokenRole.USER);
            sellerSQL = userDaoSQL.findByEmail(email);
        } catch (Exception ignore) {
        }
        return sellerSQL;
    }

     public UserResponse createUserResponse(UserSQL userSQL) {
        try {
            UserResponse userResponse = UserResponse.builder()
                    .id(userSQL.getId())
                    .name(userSQL.getName())
                    .lastName(userSQL.getLastName())
                    .region(userSQL.getRegion())
                    .city(userSQL.getCity())
                    .number(userSQL.getNumber())
                    .avatar(userSQL.getAvatar())
                    .createdAt(userSQL.getCreatedAt())
                    .accountType(userSQL.getAccountType())
                    .role(ERole.USER)
                    .isPaymentSourcePresent(userSQL.isPaymentSourcePresent())
                    .build();

            Session session = sessionDaoSQL.findByUserId(userSQL.getId());

            if (session != null) {
                userResponse.setLastOnline(session.getDisconnectedAt());
            }

            return userResponse;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new CustomException("Could not create response", HttpStatus.CONFLICT);
        }
    }

}
