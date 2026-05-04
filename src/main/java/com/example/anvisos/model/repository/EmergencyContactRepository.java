package com.example.anvisos.model.repository;

import com.example.anvisos.model.entity.EmergencyContact;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {
    List<EmergencyContact> findByUserIdOrderByPriorityAsc(Long userId);
}

