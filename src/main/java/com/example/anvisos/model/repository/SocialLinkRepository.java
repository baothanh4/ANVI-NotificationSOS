package com.example.anvisos.model.repository;

import com.example.anvisos.model.entity.SocialLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SocialLinkRepository extends JpaRepository<SocialLink, Long> {
    List<SocialLink> findByUserId(Long userId);
    List<SocialLink> findByUserIdAndVisibleTrue(Long userId);
}
