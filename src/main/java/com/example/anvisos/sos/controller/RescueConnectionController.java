package com.example.anvisos.sos.controller;

import com.example.anvisos.model.entity.RescueConnection;
import com.example.anvisos.model.entity.User;
import com.example.anvisos.model.repository.RescueConnectionRepository;
import com.example.anvisos.model.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/connections")
public class RescueConnectionController {

    private final RescueConnectionRepository connectionRepository;
    private final UserRepository userRepository;

    public RescueConnectionController(RescueConnectionRepository connectionRepository, UserRepository userRepository) {
        this.connectionRepository = connectionRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam String phone) {
        return userRepository.findByPhone(phone)
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", u.getId());
                    map.put("fullName", u.getFullName());
                    map.put("phone", u.getPhone());
                    return map;
                })
                .orElse(null);
    }

    @PostMapping("/request")
    public RescueConnection sendRequest(@RequestParam Long requesterId, @RequestParam Long targetId) {
        if (requesterId.equals(targetId)) throw new IllegalArgumentException("Cannot connect to yourself");
        
        Optional<RescueConnection> existing = connectionRepository.findByRequesterIdAndTargetId(requesterId, targetId);
        if (existing.isPresent()) return existing.get();

        User requester = userRepository.findById(requesterId).orElseThrow();
        User target = userRepository.findById(targetId).orElseThrow();

        RescueConnection conn = RescueConnection.builder()
                .requester(requester)
                .target(target)
                .status("PENDING")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        return connectionRepository.save(conn);
    }

    @GetMapping("/my")
    public List<Map<String, Object>> getMyConnections(@RequestParam Long userId) {
        List<RescueConnection> sent = connectionRepository.findByRequesterId(userId);
        List<RescueConnection> received = connectionRepository.findByTargetId(userId);

        List<Map<String, Object>> result = new ArrayList<>();

        for (RescueConnection c : sent) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("fullName", c.getTarget().getFullName());
            m.put("phone", c.getTarget().getPhone());
            m.put("status", "PENDING_OUT".equals(c.getStatus()) || "PENDING".equals(c.getStatus()) ? "PENDING_OUT" : c.getStatus());
            if ("ACCEPTED".equals(c.getStatus())) m.put("status", "CONNECTED");
            m.put("relationship", c.getRelationship());
            result.add(m);
        }

        for (RescueConnection c : received) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("fullName", c.getRequester().getFullName());
            m.put("phone", c.getRequester().getPhone());
            m.put("status", "PENDING".equals(c.getStatus()) ? "PENDING_IN" : c.getStatus());
            if ("ACCEPTED".equals(c.getStatus())) m.put("status", "CONNECTED");
            m.put("relationship", c.getRelationship());
            result.add(m);
        }

        return result;
    }

    @PatchMapping("/{id}/status")
    public void updateStatus(@PathVariable Long id, @RequestParam String status) {
        RescueConnection conn = connectionRepository.findById(id).orElseThrow();
        conn.setStatus(status);
        conn.setUpdatedAt(Instant.now());
        connectionRepository.save(conn);
    }

    @PatchMapping("/{id}/relationship")
    public void updateRelationship(@PathVariable Long id, @RequestBody Map<String, String> body) {
        RescueConnection conn = connectionRepository.findById(id).orElseThrow();
        conn.setRelationship(body.get("relationship"));
        conn.setUpdatedAt(Instant.now());
        connectionRepository.save(conn);
    }
}
