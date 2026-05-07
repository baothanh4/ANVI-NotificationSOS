package com.example.anvisos.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "safe_journeys")
public class SafeJourney {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expectedEndTime;

    @Column(length = 255)
    private String destinationName;

    @Column(precision = 11, scale = 8)
    private BigDecimal destinationLat;

    @Column(precision = 11, scale = 8)
    private BigDecimal destinationLng;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean sosTriggered = false;

    @Column(nullable = false)
    private Instant createdAt;
}
