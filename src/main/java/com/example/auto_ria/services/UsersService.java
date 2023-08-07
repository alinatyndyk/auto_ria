package com.example.auto_ria.services;

import com.example.auto_ria.dto.UserDTO;
import com.example.auto_ria.models.Seller;
import com.example.auto_ria.models.UserSQL;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface UsersService {

    public ResponseEntity<List<Seller>> getAll();

//    public ResponseEntity<List<UserSQL>> getAllSL1();

    public ResponseEntity<Seller> getById(String id);

    public void transferAvatar(MultipartFile picture, String originalFileName);

    public ResponseEntity<String> deleteById(String id);

}
