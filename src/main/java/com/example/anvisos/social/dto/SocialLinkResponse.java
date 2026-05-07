package com.example.anvisos.social.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SocialLinkResponse {
    private Long id;
    private String platform;
    private String url;
    private boolean visible;
}
