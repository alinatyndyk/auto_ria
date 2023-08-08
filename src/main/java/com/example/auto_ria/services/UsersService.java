package com.example.auto_ria.services;

import com.example.auto_ria.models.SellerSQL;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UsersService {

    public ResponseEntity<List<SellerSQL>> getAll();

//    public ResponseEntity<List<UserSQL>> getAllSL1();

    public ResponseEntity<SellerSQL> getById(String id);

    public void transferAvatar(MultipartFile picture, String originalFileName);

    public ResponseEntity<String> deleteById(String id, SellerSQL seller);

}
