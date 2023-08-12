package com.example.auto_ria.controllers;

import com.example.auto_ria.dto.updateDTO.ManagerUpdateDTO;
import com.example.auto_ria.models.ManagerSQL;
import com.example.auto_ria.services.ManagerServiceMySQL;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "managers")
public class ManagerController {

    private ManagerServiceMySQL managerServiceMySQL;

    @GetMapping()
//    @JsonView(ViewsCar.SL3.class)
    public ResponseEntity<List<ManagerSQL>> getAll() {
        return managerServiceMySQL.getAll();
    }

    @GetMapping("/{id}")
//    @JsonView(ViewsCar.SL1.class)
    public ResponseEntity<ManagerSQL> getById(@PathVariable("id") int id) {
        return managerServiceMySQL.getById(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ManagerSQL> patch(@PathVariable int id,
                                            @RequestBody ManagerUpdateDTO partial) throws NoSuchFieldException, IllegalAccessException {
        return managerServiceMySQL.update(id, partial);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable int id) {
        return managerServiceMySQL.deleteById(id);
    }

}
