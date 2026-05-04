package com.example.anvisos.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Mỗi khi SOS được kích hoạt, một SosEvent được tạo với token ngẫu nhiên.
 * Token này được nhúng vào URL gửi cho emergency contacts để xem trang alert.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sos_events")
public class SosEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Token ngẫu nhiên, dùng làm public URL */
    @Column(nullable = false, unique = true, length = 64)
    private String publicToken;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** GPS coordinates tại thời điểm trigger */
    @Column(precision = 11, scale = 8)
    private BigDecimal lastLat;

    @Column(precision = 11, scale = 8)
    private BigDecimal lastLng;

    /** Địa chỉ text (reverse geocoded hoặc nhập tay) */
    @Column(length = 512)
    private String locationText;

    /** Thời điểm tạo SOS */
    @Column(nullable = false)
    private Instant triggeredAt;

    /** Thời điểm cập nhật vị trí cuối cùng */
    @Column(nullable = false)
    private Instant updatedAt;

    /** SOS còn active không (false = đã giải quyết) */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
