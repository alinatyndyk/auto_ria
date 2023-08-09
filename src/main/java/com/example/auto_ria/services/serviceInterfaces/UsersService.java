package com.example.auto_ria.services.serviceInterfaces;

import com.example.auto_ria.dto.UserDTO;
import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.responses.ErrorResponse;
import io.jsonwebtoken.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UsersService {

    ResponseEntity<List<SellerSQL>> getAll();

    ResponseEntity<SellerSQL> getById(String id);

    void transferAvatar(MultipartFile picture, String originalFileName);

    ResponseEntity<String> deleteById(String id);

    ResponseEntity<SellerSQL> update(int id, UserDTO userDTO, SellerSQL seller) throws IllegalAccessException, IOException, ErrorResponse, NoSuchFieldException;

    // todo     ResponseEntity<List<UserSQL>> getAllSL1();


}
