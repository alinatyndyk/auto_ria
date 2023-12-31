package com.example.auto_ria.services;

import com.example.auto_ria.dao.socket.SessionDaoSQL;
import com.example.auto_ria.dao.user.AdministratorDaoSQL;
import com.example.auto_ria.dao.user.CustomerDaoSQL;
import com.example.auto_ria.dao.user.ManagerDaoSQL;
import com.example.auto_ria.dao.user.UserDaoSQL;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.enums.ETokenRole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.responses.user.AdminResponse;
import com.example.auto_ria.models.responses.user.CustomerResponse;
import com.example.auto_ria.models.responses.user.ManagerResponse;
import com.example.auto_ria.models.responses.user.SellerResponse;
import com.example.auto_ria.models.socket.Session;
import com.example.auto_ria.models.user.AdministratorSQL;
import com.example.auto_ria.models.user.CustomerSQL;
import com.example.auto_ria.models.user.ManagerSQL;
import com.example.auto_ria.models.user.SellerSQL;
import com.example.auto_ria.services.auth.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CommonService {

    private JwtService jwtService;

    private UserDaoSQL userDaoSQL;
    private SessionDaoSQL sessionDaoSQL;
    private ManagerDaoSQL managerDaoSQL;
    private CustomerDaoSQL customerDaoSQL;
    private AdministratorDaoSQL administratorDaoSQL;

    public void transferAvatar(MultipartFile picture, String originalFileName) {
        try {
            String path = System.getProperty("user.home") + File.separator + "springboot-lib" + File.separator + originalFileName;
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
            String path = System.getProperty("user.home") + File.separator + "springboot-lib" + File.separator + originalFileName;
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
            System.out.println(bearerToken + "bearer token");
            return jwtService.extractUsername(bearerToken, role);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("111111111111111 e.getMessage()");
            throw new CustomException("Please sign in", HttpStatus.UNAUTHORIZED);
        }

    }

    public SellerSQL extractSellerFromHeader(HttpServletRequest request) {
        SellerSQL sellerSQL = null;
        try {
            String email = extractEmailFromHeader(request, ETokenRole.SELLER);
            sellerSQL = userDaoSQL.findSellerByEmail(email);
        } catch (Exception ignore) {
        }
        return sellerSQL;
    }


    public ManagerSQL extractManagerFromHeader(HttpServletRequest request) {
        ManagerSQL managerSQL;
        try {
            managerSQL = managerDaoSQL.findByEmail(extractEmailFromHeader(request, ETokenRole.MANAGER));
        } catch (Exception e) {
            return null;
        }
        return managerSQL;
    }

    public AdministratorSQL extractAdminFromHeader(HttpServletRequest request) {
        AdministratorSQL administratorSQL;
        try {
            administratorSQL = administratorDaoSQL.findByEmail(extractEmailFromHeader(request, ETokenRole.ADMIN));
        } catch (Exception e) {
            return null;
        }
        return administratorSQL;
    }

    public CustomerSQL extractCustomerFromHeader(HttpServletRequest request) {
        return customerDaoSQL.findByEmail(extractEmailFromHeader(request, ETokenRole.CUSTOMER));
    }

    public ERole findRoleByEmail(String email) {
        try {
            if (administratorDaoSQL.findByEmail(email) != null) {
                return ERole.ADMIN;
            } else if (managerDaoSQL.findByEmail(email) != null) {
                return ERole.MANAGER;
            } else if (userDaoSQL.findSellerByEmail(email) != null) {
                return ERole.SELLER;
            } else if (customerDaoSQL.findByEmail(email) != null) {
                return ERole.CUSTOMER;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new CustomException("Failed to get role: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public CustomerResponse createCustomerResponse(CustomerSQL customer) {
        try {

            Session session = sessionDaoSQL.findByUserId(customer.getId());

            return CustomerResponse.builder()
                    .id(customer.getId())
                    .name(customer.getName())
                    .lastName(customer.getLastName())
                    .region(customer.getRegion())
                    .city(customer.getCity())
                    .avatar(customer.getAvatar())
                    .role(ERole.CUSTOMER)
                    .isOnline(session.getIsOnline())
                    .lastOnline(session.getDisconnectedAt())
                    .createdAt(customer.getCreatedAt())
                    .build();

        } catch (Exception e) {
            throw new CustomException("Could not create response", HttpStatus.CONFLICT);
        }
    }

    public ManagerResponse createManagerResponse(ManagerSQL manager) {
        try {
            return ManagerResponse.builder()
                    .id(manager.getId())
                    .name(manager.getName())
                    .lastName(manager.getLastName())
                    .avatar(manager.getAvatar())
                    .role(ERole.MANAGER)
                    .createdAt(manager.getCreatedAt())
                    .build();

        } catch (Exception e) {
            throw new CustomException("Could not create response", HttpStatus.CONFLICT);
        }
    }

    public AdminResponse createAdminResponse(AdministratorSQL administratorSQL) {
        try {
            return AdminResponse.builder()
                    .id(administratorSQL.getId())
                    .name(administratorSQL.getName())
                    .lastName(administratorSQL.getLastName())
                    .avatar(administratorSQL.getAvatar())
                    .role(ERole.ADMIN)
                    .createdAt(administratorSQL.getCreatedAt())
                    .email(administratorSQL.getEmail())
                    .build();

        } catch (Exception e) {
            throw new CustomException("Could not create response", HttpStatus.CONFLICT);
        }
    }

    public SellerResponse createSellerResponse(SellerSQL sellerSQL) {
        try {
            SellerResponse sellerResponse = SellerResponse.builder()
                    .id(sellerSQL.getId())
                    .name(sellerSQL.getName())
                    .lastName(sellerSQL.getLastName())
                    .region(sellerSQL.getRegion())
                    .city(sellerSQL.getCity())
                    .number(sellerSQL.getNumber())
                    .avatar(sellerSQL.getAvatar())
                    .createdAt(sellerSQL.getCreatedAt())
                    .accountType(sellerSQL.getAccountType())
                    .role(ERole.SELLER)
                    .isPaymentSourcePresent(sellerSQL.isPaymentSourcePresent())
                    .build();

            Session session = sessionDaoSQL.findByUserId(sellerSQL.getId());

            if (session != null) {
                sellerResponse.setLastOnline(session.getDisconnectedAt());
            }

            return sellerResponse;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new CustomException("Could not create response", HttpStatus.CONFLICT);
        }
    }

}
