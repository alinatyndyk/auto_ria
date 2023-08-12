package com.example.auto_ria.services.serviceInterfaces;

import com.example.auto_ria.dto.updateDTO.UserUpdateDTO;
import com.example.auto_ria.models.SellerSQL;
import io.jsonwebtoken.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UsersService {

    ResponseEntity<List<SellerSQL>> getAll();

    ResponseEntity<SellerSQL> getById(String id);

    void transferAvatar(MultipartFile picture, String originalFileName) throws java.io.IOException;

    ResponseEntity<String> deleteById(String id);

    ResponseEntity<SellerSQL> update(int id, UserUpdateDTO userDTO, SellerSQL seller)
            throws IllegalAccessException, IOException, NoSuchFieldException;

}
