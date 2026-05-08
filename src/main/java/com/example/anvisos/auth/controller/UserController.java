package com.example.anvisos.auth.controller;

import com.example.anvisos.model.entity.User;
import com.example.anvisos.model.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final com.example.anvisos.auth.service.JwtService jwtService;

    @GetMapping("/public-info/{userId}")
    public ResponseEntity<Map<String, Object>> getPublicInfo(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        return ResponseEntity.ok(Map.of(
            "fullName", user.getFullName(),
            "dateOfBirth", user.getDateOfBirth() != null ? user.getDateOfBirth() : ""
        ));
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getMyProfile(@RequestHeader("Authorization") String token) {
        Long userId = jwtService.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/admin/all")
    @com.example.anvisos.auth.annotation.RequiredRole(com.example.anvisos.model.enums.UserRole.ADMIN)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/admin/create-internal")
    @com.example.anvisos.auth.annotation.RequiredRole(com.example.anvisos.model.enums.UserRole.ADMIN)
    public User createInternalUser(@RequestBody Map<String, String> data) {
        if (userRepository.findByPhone(data.get("phone")).isPresent()) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại");
        }
        
        User user = User.builder()
                .fullName(data.get("fullName"))
                .phone(data.get("phone"))
                .email(data.get("email"))
                .passwordHash(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(data.get("password")))
                .role(com.example.anvisos.model.enums.UserRole.valueOf(data.get("role")))
                .phoneVerified(true)
                .emailVerified(true)
                .createdAt(java.time.Instant.now())
                .build();
        
        return userRepository.save(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> data) {
        
        Long userId = jwtService.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (data.containsKey("fullName")) user.setFullName((String) data.get("fullName"));
        if (data.containsKey("email")) user.setEmail((String) data.get("email"));
        if (data.containsKey("bio")) user.setBio((String) data.get("bio"));
        if (data.containsKey("dateOfBirth")) user.setDateOfBirth((String) data.get("dateOfBirth"));
        
        return ResponseEntity.ok(userRepository.save(user));
    }
}
