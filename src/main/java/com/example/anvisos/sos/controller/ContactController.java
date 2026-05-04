package com.example.anvisos.sos.controller;

import com.example.anvisos.model.entity.EmergencyContact;
import com.example.anvisos.sos.dto.ContactRequest;
import com.example.anvisos.sos.dto.ContactResponse;
import com.example.anvisos.sos.dto.ReorderContactsRequest;
import com.example.anvisos.sos.service.ContactService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping("/users/{userId}")
    public List<ContactResponse> list(@PathVariable Long userId) {
        return contactService.list(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @PostMapping("/users/{userId}")
    public ContactResponse create(@PathVariable Long userId, @Valid @RequestBody ContactRequest request) {
        return toResponse(contactService.create(userId, request));
    }

    @PutMapping("/{contactId}")
    public ContactResponse update(@PathVariable Long contactId, @Valid @RequestBody ContactRequest request) {
        return toResponse(contactService.update(contactId, request));
    }

    @DeleteMapping("/{contactId}")
    public void delete(@PathVariable Long contactId) {
        contactService.delete(contactId);
    }

    @PostMapping("/users/{userId}/reorder")
    public void reorder(@PathVariable Long userId, @Valid @RequestBody ReorderContactsRequest request) {
        contactService.reorder(userId, request.getOrderedContactIds());
    }

    private ContactResponse toResponse(EmergencyContact contact) {
        return new ContactResponse(
                contact.getId(),
                contact.getName(),
                contact.getPhone(),
                contact.getPriority(),
                contact.isVerified()
        );
    }
}

