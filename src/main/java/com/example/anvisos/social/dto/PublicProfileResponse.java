package com.example.anvisos.social.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicProfileResponse {
    private String fullName;
    private String bio;
    private String dateOfBirth;
    private List<SocialLinkResponse> socialLinks;
}
