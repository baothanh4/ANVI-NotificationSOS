package com.example.anvisos.health.controller;

import com.example.anvisos.health.dto.AccessGrantRequest;
import com.example.anvisos.health.dto.AccessGrantResponse;
import com.example.anvisos.health.service.AccessGrantService;
import com.example.anvisos.model.entity.AccessGrant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/access-grants")
public class AccessGrantController {
    private final AccessGrantService accessGrantService;

    public AccessGrantController(AccessGrantService accessGrantService) {
        this.accessGrantService = accessGrantService;
    }

    @PostMapping("/request")
    public AccessGrantResponse request(@Valid @RequestBody AccessGrantRequest request, HttpServletRequest httpRequest) {
        return toResponse(accessGrantService.requestAccess(request, getClientIp(httpRequest), httpRequest.getHeader("User-Agent")));
    }

    @PostMapping("/{grantId}/approve")
    public AccessGrantResponse approve(@PathVariable Long grantId, HttpServletRequest httpRequest) {
        return toResponse(accessGrantService.approve(grantId, getClientIp(httpRequest), httpRequest.getHeader("User-Agent")));
    }

    @PostMapping("/{grantId}/deny")
    public AccessGrantResponse deny(@PathVariable Long grantId, HttpServletRequest httpRequest) {
        return toResponse(accessGrantService.deny(grantId, getClientIp(httpRequest), httpRequest.getHeader("User-Agent")));
    }

    @GetMapping("/{grantId}")
    public AccessGrantResponse get(@PathVariable Long grantId) {
        return toResponse(accessGrantService.get(grantId));
    }

    @GetMapping("/{grantId}/stream")
    public SseEmitter stream(@PathVariable Long grantId) {
        return accessGrantService.subscribe(grantId);
    }

    private AccessGrantResponse toResponse(AccessGrant grant) {
        return new AccessGrantResponse(
                grant.getId(),
                grant.getUser().getId(),
                grant.getDoctorName(),
                grant.getHospitalName(),
                grant.getStatus(),
                grant.getToken(),
                grant.getExpiresAt(),
                grant.getCreatedAt(),
                grant.getUpdatedAt()
        );
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

