package com.example.anvisos.health.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MedicalDocumentResponse {
    private Long id;
    private Long userId;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Instant createdAt;
}
