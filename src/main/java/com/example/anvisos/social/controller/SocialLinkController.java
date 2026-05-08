package com.example.anvisos.social.controller;

import com.example.anvisos.social.dto.PublicProfileResponse;
import com.example.anvisos.social.dto.SocialLinkRequest;
import com.example.anvisos.social.dto.SocialLinkResponse;
import com.example.anvisos.social.service.SocialLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialLinkController {

    private final SocialLinkService socialLinkService;

    @GetMapping("/public/{shortCode}")
    public ResponseEntity<PublicProfileResponse> getPublicProfile(@PathVariable String shortCode) {
        return ResponseEntity.ok(socialLinkService.getPublicProfile(shortCode));
    }

    @GetMapping("/my")
    public ResponseEntity<List<SocialLinkResponse>> getMyLinks(@RequestParam Long userId) {
        return ResponseEntity.ok(socialLinkService.getMySocialLinks(userId));
    }

    @PostMapping
    public ResponseEntity<SocialLinkResponse> addLink(@RequestParam Long userId, @RequestBody SocialLinkRequest request) {
        return ResponseEntity.ok(socialLinkService.addSocialLink(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SocialLinkResponse> updateLink(@PathVariable Long id, @RequestBody SocialLinkRequest request) {
        return ResponseEntity.ok(socialLinkService.updateSocialLink(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLink(@PathVariable Long id) {
        socialLinkService.deleteSocialLink(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<SocialLinkResponse> toggleVisibility(@PathVariable Long id) {
        return ResponseEntity.ok(socialLinkService.toggleVisibility(id));
    }
}
