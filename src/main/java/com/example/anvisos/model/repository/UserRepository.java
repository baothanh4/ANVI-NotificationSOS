package com.example.anvisos.model.repository;

import com.example.anvisos.model.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmail(String email);

    @org.springframework.data.jpa.repository.Query(value = 
        "SELECT * FROM users u WHERE u.is_volunteer = true " +
        "AND (6371 * acos(cos(radians(?1)) * cos(radians(u.last_lat)) * cos(radians(u.last_lng) - radians(?2)) + sin(radians(?1)) * sin(radians(u.last_lat)))) <= ?3", 
        nativeQuery = true)
    java.util.List<User> findVolunteersNearby(java.math.BigDecimal lat, java.math.BigDecimal lng, double radiusKm);
}

