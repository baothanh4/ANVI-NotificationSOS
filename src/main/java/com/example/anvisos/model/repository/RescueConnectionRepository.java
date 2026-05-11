package com.example.anvisos.model.repository;

import com.example.anvisos.model.entity.RescueConnection;
import com.example.anvisos.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RescueConnectionRepository extends JpaRepository<RescueConnection, Long> {
    List<RescueConnection> findByRequesterId(Long requesterId);
    List<RescueConnection> findByTargetId(Long targetId);
    
    // Find accepted connections where the user is either requester or target
    List<RescueConnection> findByRequesterIdAndStatus(Long requesterId, String status);
    List<RescueConnection> findByTargetIdAndStatus(Long targetId, String status);

    Optional<RescueConnection> findByRequesterIdAndTargetId(Long requesterId, Long targetId);
}
