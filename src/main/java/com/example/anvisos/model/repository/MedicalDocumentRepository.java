package com.example.anvisos.model.repository;

import com.example.anvisos.model.entity.MedicalDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalDocumentRepository extends JpaRepository<MedicalDocument, Long> {
    List<MedicalDocument> findByUserIdOrderByCreatedAtDesc(Long userId);
}
