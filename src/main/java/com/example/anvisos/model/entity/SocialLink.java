package com.example.anvisos.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "social_links")
public class SocialLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String platform; // facebook, instagram, linkedin, etc.

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private boolean visible; // Trạng thái hiển thị công khai trên trang SOS

    @Column(nullable = false)
    private Instant createdAt;
}
