package com.example.anvisos.sos.service;

import com.example.anvisos.common.AuditService;
import com.example.anvisos.model.entity.Card;
import com.example.anvisos.model.entity.EmergencyContact;
import com.example.anvisos.model.entity.User;
import com.example.anvisos.model.enums.CardStatus;
import com.example.anvisos.model.repository.CardRepository;
import com.example.anvisos.model.repository.EmergencyContactRepository;
import com.example.anvisos.model.repository.HealthRecordRepository;
import com.example.anvisos.model.repository.UserRepository;
import com.example.anvisos.notification.NotificationService;
import com.example.anvisos.sos.dto.SosTriggerRequest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SosServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private EmergencyContactRepository contactRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditService auditService;

    @Mock
    private HealthRecordRepository healthRecordRepository;

    @InjectMocks
    private SosService sosService;

    @Test
    void triggerSendsToAllContacts() {
        Long userId = 1L;
        User user = User.builder().id(userId).fullName("Anvi").createdAt(Instant.now()).build();
        Card card = Card.builder().id(10L).user(user).status(CardStatus.ACTIVE).createdAt(Instant.now()).build();

        EmergencyContact c1 = EmergencyContact.builder().id(100L).phone("0901").build();
        EmergencyContact c2 = EmergencyContact.builder().id(101L).phone("0902").build();

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        Mockito.when(contactRepository.findByUserIdOrderByPriorityAsc(userId)).thenReturn(List.of(c1, c2));
        Mockito.when(notificationService.sendToPhone(Mockito.anyString(), Mockito.anyString())).thenReturn(2);
        Mockito.when(healthRecordRepository.findByUserId(userId)) .thenReturn(Optional.empty());

        SosTriggerRequest request = new SosTriggerRequest();
        request.setUserId(userId);
        request.setCardId(card.getId());

        SosService.TriggerResult result = sosService.trigger(request, "127.0.0.1", "JUnit");

        Assertions.assertEquals(2, result.sentCount());
    }
}
