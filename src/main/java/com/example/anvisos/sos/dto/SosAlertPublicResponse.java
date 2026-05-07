package com.example.anvisos.sos.dto;

import com.example.anvisos.model.enums.SosType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Public response trả về cho emergency contacts.
 * Không cần auth — chỉ cần token hợp lệ.
 */
@Getter
@Setter
public class SosAlertPublicResponse {

    private String victimName;
    private String victimPhone;
    private String avatarUrl;      // URL ảnh avatar hoặc null
    private String initials;       // 2 chữ cái đầu tên để render avatar fallback
    private String bloodType;
    private String allergies;
    private String conditions;
    private String emergencyNote;
    private Integer birthYear;

    private BigDecimal lat;
    private BigDecimal lng;
    private String locationText;

    private String triggeredAt;    // Formatted VN time
    private String updatedAt;
    private boolean active;
    
    private String mediaUrl;
    private SosType sosType;
    private boolean isSafe; // Alias cho !active

    // Google Maps URLs
    private String googleMapsViewUrl;
    private String googleMapsDirectionsUrl;
    private java.util.List<com.example.anvisos.social.dto.SocialLinkResponse> socialLinks;

    public SosAlertPublicResponse() {}
}
