package com.example.anvisos.sos.controller;

import com.example.anvisos.model.entity.FirstAidInstruction;
import com.example.anvisos.sos.service.FirstAidService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/first-aid")
@RequiredArgsConstructor
public class FirstAidController {

    private final FirstAidService firstAidService;

    @GetMapping
    public List<FirstAidInstruction> getAll(@RequestParam(required = false) String category) {
        if (category != null) {
            return firstAidService.getByCategory(category);
        }
        return firstAidService.getAll();
    }

    @GetMapping("/{id}")
    public FirstAidInstruction getById(@PathVariable Long id) {
        return firstAidService.getById(id);
    }

    @PostMapping
    public FirstAidInstruction create(@RequestBody FirstAidInstruction instruction) {
        return firstAidService.save(instruction);
    }
}
