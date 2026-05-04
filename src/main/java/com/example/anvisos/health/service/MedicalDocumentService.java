package com.example.anvisos.health.service;

import com.example.anvisos.common.FileStorageService;
import com.example.anvisos.health.dto.MedicalDocumentResponse;
import com.example.anvisos.model.entity.MedicalDocument;
import com.example.anvisos.model.entity.User;
import com.example.anvisos.model.repository.MedicalDocumentRepository;
import com.example.anvisos.model.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
public class MedicalDocumentService {
    private final MedicalDocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public MedicalDocumentService(MedicalDocumentRepository documentRepository, UserRepository userRepository, FileStorageService fileStorageService) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    public MedicalDocumentResponse uploadDocument(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String fileName = fileStorageService.storeFile(file);
        
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/documents/download/")
                .path(fileName)
                .toUriString();

        MedicalDocument document = MedicalDocument.builder()
                .user(user)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileUrl(fileDownloadUri)
                .createdAt(Instant.now())
                .build();

        document = documentRepository.save(document);
        return toResponse(document);
    }

    public List<MedicalDocumentResponse> getDocuments(Long userId) {
        return documentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void deleteDocument(Long id) {
        MedicalDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        
        String fileName = document.getFileUrl().substring(document.getFileUrl().lastIndexOf("/") + 1);
        fileStorageService.deleteFile(fileName);
        
        documentRepository.delete(document);
    }

    private MedicalDocumentResponse toResponse(MedicalDocument document) {
        return new MedicalDocumentResponse(
                document.getId(),
                document.getUser().getId(),
                document.getFileName(),
                document.getFileType(),
                document.getFileUrl(),
                document.getCreatedAt()
        );
    }
}
