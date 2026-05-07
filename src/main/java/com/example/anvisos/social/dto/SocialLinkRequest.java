package com.example.anvisos.social.dto;

import lombok.Data;

@Data
public class SocialLinkRequest {
    private String platform;
    private String url;
    private boolean visible;
}
