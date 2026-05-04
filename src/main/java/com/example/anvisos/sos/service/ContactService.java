package com.example.anvisos.sos.service;

import com.example.anvisos.model.entity.EmergencyContact;
import com.example.anvisos.model.entity.User;
import com.example.anvisos.model.repository.EmergencyContactRepository;
import com.example.anvisos.model.repository.UserRepository;
import com.example.anvisos.sos.dto.ContactRequest;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ContactService {
    private final EmergencyContactRepository emergencyContactRepository;
    private final UserRepository userRepository;

    public ContactService(
            EmergencyContactRepository emergencyContactRepository,
            UserRepository userRepository
    ) {
        this.emergencyContactRepository = emergencyContactRepository;
        this.userRepository = userRepository;
    }

    public List<EmergencyContact> list(Long userId) {
        return emergencyContactRepository.findByUserIdOrderByPriorityAsc(userId);
    }

    public EmergencyContact create(Long userId, ContactRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (userRepository.findByPhone(request.getPhone()).isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không tồn tại trong hệ thống");
        }

        ensurePriorityAvailable(userId, request.getPriority(), null);

        EmergencyContact contact = EmergencyContact.builder()
                .user(user)
                .name(request.getName())
                .phone(request.getPhone())
                .priority(request.getPriority())
                .verified(request.isVerified())
                .createdAt(Instant.now())
                .build();

        return emergencyContactRepository.save(contact);
    }

    public EmergencyContact update(Long contactId, ContactRequest request) {
        EmergencyContact contact = emergencyContactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

        if (userRepository.findByPhone(request.getPhone()).isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không tồn tại trong hệ thống");
        }

        ensurePriorityAvailable(contact.getUser().getId(), request.getPriority(), contactId);

        contact.setName(request.getName());
        contact.setPhone(request.getPhone());
        contact.setPriority(request.getPriority());
        contact.setVerified(request.isVerified());
        return emergencyContactRepository.save(contact);
    }

    public void delete(Long contactId) {
        EmergencyContact contact = emergencyContactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));
        emergencyContactRepository.delete(contact);
    }

    public void reorder(Long userId, List<Long> orderedContactIds) {
        if (orderedContactIds.size() > 3) {
            throw new IllegalArgumentException("Max 3 emergency contacts");
        }
        List<EmergencyContact> contacts = emergencyContactRepository.findByUserIdOrderByPriorityAsc(userId);
        for (int i = 0; i < orderedContactIds.size(); i++) {
            Long id = orderedContactIds.get(i);
            EmergencyContact contact = contacts.stream()
                    .filter(existing -> existing.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Contact not found for user"));
            contact.setPriority(i + 1);
            emergencyContactRepository.save(contact);
        }
    }

    private void ensurePriorityAvailable(Long userId, int priority, Long excludeId) {
        List<EmergencyContact> contacts = emergencyContactRepository.findByUserIdOrderByPriorityAsc(userId);
        for (EmergencyContact contact : contacts) {
            if (contact.getPriority() == priority && (excludeId == null || !contact.getId().equals(excludeId))) {
                throw new IllegalArgumentException("Priority already in use");
            }
        }
    }
}

