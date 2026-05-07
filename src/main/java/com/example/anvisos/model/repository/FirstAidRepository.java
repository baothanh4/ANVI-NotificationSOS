package com.example.anvisos.model.repository;

import com.example.anvisos.model.entity.FirstAidInstruction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FirstAidRepository extends JpaRepository<FirstAidInstruction, Long> {
    List<FirstAidInstruction> findByCategory(String category);
}
