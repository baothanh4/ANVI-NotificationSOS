package com.example.anvisos.model.repository;

import com.example.anvisos.model.entity.AccessGrant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessGrantRepository extends JpaRepository<AccessGrant, Long> {
    List<AccessGrant> findByUserIdOrderByCreatedAtDesc(Long userId);
}

