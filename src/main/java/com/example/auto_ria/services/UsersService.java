package com.example.auto_ria.services;

import com.example.auto_ria.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface UsersService {

    public ResponseEntity<List<UserDTO>> getAll();

//    public ResponseEntity<List<UserDTO>> getAllSL1();

    public ResponseEntity<UserDTO> getById(String id);

    public UserDTO post(UserDTO userDTO);

    public File transferAvatar(MultipartFile picture, String originalFileName);

    public ResponseEntity<String> deleteById(String id);

}
