package com.example.anvisos.sos.service;

import com.example.anvisos.model.entity.FirstAidInstruction;
import com.example.anvisos.model.repository.FirstAidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FirstAidService {

    private final FirstAidRepository repository;

    public List<FirstAidInstruction> getAll() {
        return repository.findAll();
    }

    public List<FirstAidInstruction> getByCategory(String category) {
        return repository.findByCategory(category);
    }

    public FirstAidInstruction getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Instruction not found"));
    }

    public FirstAidInstruction save(FirstAidInstruction instruction) {
        return repository.save(instruction);
    }
}
