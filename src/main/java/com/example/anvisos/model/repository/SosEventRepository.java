package com.example.anvisos.model.repository;

import com.example.anvisos.model.entity.SosEvent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SosEventRepository extends JpaRepository<SosEvent, Long> {

    /** Fetch kèm User để tránh LazyInitializationException */
    @EntityGraph(attributePaths = "user")
    Optional<SosEvent> findByPublicTokenAndActiveTrue(String publicToken);

    Optional<SosEvent> findTopByUserIdAndActiveTrueOrderByTriggeredAtDesc(Long userId);
}
