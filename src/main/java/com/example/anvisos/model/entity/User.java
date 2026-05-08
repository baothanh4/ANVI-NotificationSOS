package com.example.anvisos.model.entity;

import com.example.anvisos.model.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(unique = true)
    private String email;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private boolean phoneVerified;

    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(precision = 11, scale = 8)
    private java.math.BigDecimal lastLat;

    @Column(precision = 11, scale = 8)
    private java.math.BigDecimal lastLng;

    @Column(nullable = false)
    @Builder.Default
    private boolean isVolunteer = false;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column
    private String dateOfBirth;

    @Column(nullable = false)
    private Instant createdAt;
}
