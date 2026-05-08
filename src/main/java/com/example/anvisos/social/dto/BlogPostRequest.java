package com.example.anvisos.social.dto;

import lombok.Data;

@Data
public class BlogPostRequest {
    private String title;
    private String content;
    private String excerpt;
    private String thumbnailUrl;
    private String category;
}
