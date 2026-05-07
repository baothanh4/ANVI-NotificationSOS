package com.example.anvisos.social.service;

import com.example.anvisos.model.entity.SocialLink;
import com.example.anvisos.model.entity.User;
import com.example.anvisos.model.repository.SocialLinkRepository;
import com.example.anvisos.model.repository.UserRepository;
import com.example.anvisos.social.dto.SocialLinkRequest;
import com.example.anvisos.social.dto.SocialLinkResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SocialLinkService {

    private final SocialLinkRepository socialLinkRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<SocialLinkResponse> getMySocialLinks(Long userId) {
        return socialLinkRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SocialLinkResponse addSocialLink(Long userId, SocialLinkRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        SocialLink link = SocialLink.builder()
                .user(user)
                .platform(request.getPlatform())
                .url(request.getUrl())
                .visible(request.isVisible())
                .createdAt(Instant.now())
                .build();

        return mapToResponse(socialLinkRepository.save(link));
    }

    @Transactional
    public SocialLinkResponse updateSocialLink(Long linkId, SocialLinkRequest request) {
        SocialLink link = socialLinkRepository.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Link not found"));

        link.setPlatform(request.getPlatform());
        link.setUrl(request.getUrl());
        link.setVisible(request.isVisible());

        return mapToResponse(socialLinkRepository.save(link));
    }

    @Transactional
    public void deleteSocialLink(Long linkId) {
        socialLinkRepository.deleteById(linkId);
    }

    @Transactional
    public SocialLinkResponse toggleVisibility(Long linkId) {
        SocialLink link = socialLinkRepository.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Link not found"));
        
        link.setVisible(!link.isVisible());
        return mapToResponse(socialLinkRepository.save(link));
    }

    private SocialLinkResponse mapToResponse(SocialLink link) {
        return SocialLinkResponse.builder()
                .id(link.getId())
                .platform(link.getPlatform())
                .url(link.getUrl())
                .visible(link.isVisible())
                .build();
    }
}
