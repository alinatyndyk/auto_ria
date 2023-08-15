package com.example.auto_ria.controllers;

import com.example.auto_ria.dto.updateDTO.ManagerUpdateDTO;
import com.example.auto_ria.models.ManagerSQL;
import com.example.auto_ria.services.ManagerServiceMySQL;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(value = "managers")
public class ManagerController {

    private ManagerServiceMySQL managerServiceMySQL;

    @GetMapping("/page/{page}")
    public ResponseEntity<Page<ManagerSQL>> getAll(
            @PathVariable("page") int page
    ) {
        return managerServiceMySQL.getAll(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManagerSQL> getById(@PathVariable("id") int id) {
        return managerServiceMySQL.getById(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ManagerSQL> patch(
            HttpServletRequest request,
            @PathVariable int id,
            @RequestBody ManagerUpdateDTO partial) throws NoSuchFieldException, IllegalAccessException {
        managerServiceMySQL.checkCredentials(request, id);
        return managerServiceMySQL.update(id, partial);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable int id,
                                             HttpServletRequest request) {
        managerServiceMySQL.checkCredentials(request, id);
        return managerServiceMySQL.deleteById(id);
    }

}
