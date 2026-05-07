package com.example.anvisos.model.repository;

import com.example.anvisos.model.entity.SafeJourney;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SafeJourneyRepository extends JpaRepository<SafeJourney, Long> {
    List<SafeJourney> findByActiveTrueAndExpectedEndTimeBefore(Instant now);
    List<SafeJourney> findByUserIdAndActiveTrue(Long userId);
}
