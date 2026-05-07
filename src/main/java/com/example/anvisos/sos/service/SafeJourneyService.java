package com.example.anvisos.sos.service;

import com.example.anvisos.model.entity.SafeJourney;
import com.example.anvisos.model.entity.User;
import com.example.anvisos.model.enums.SosType;
import com.example.anvisos.model.repository.SafeJourneyRepository;
import com.example.anvisos.model.repository.UserRepository;
import com.example.anvisos.sos.dto.SosTriggerRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SafeJourneyService {

    private final SafeJourneyRepository safeJourneyRepository;
    private final UserRepository userRepository;
    private final SosService sosService;

    @Transactional
    public SafeJourney startJourney(Long userId, int durationMinutes, String destName, BigDecimal destLat, BigDecimal destLng) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Deactivate previous journeys
        List<SafeJourney> activeJourneys = safeJourneyRepository.findByUserIdAndActiveTrue(userId);
        for (SafeJourney j : activeJourneys) {
            j.setActive(false);
        }
        safeJourneyRepository.saveAll(activeJourneys);

        SafeJourney journey = SafeJourney.builder()
                .user(user)
                .expectedEndTime(Instant.now().plusSeconds(durationMinutes * 60L))
                .destinationName(destName)
                .destinationLat(destLat)
                .destinationLng(destLng)
                .active(true)
                .createdAt(Instant.now())
                .build();

        return safeJourneyRepository.save(journey);
    }

    @Transactional
    public void endJourney(Long userId) {
        List<SafeJourney> activeJourneys = safeJourneyRepository.findByUserIdAndActiveTrue(userId);
        for (SafeJourney j : activeJourneys) {
            j.setActive(false);
        }
        safeJourneyRepository.saveAll(activeJourneys);
    }

    /**
     * Mỗi 1 phút quét các hành trình hết hạn mà chưa check-in
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkExpiredJourneys() {
        log.info("Checking for expired safe journeys...");
        List<SafeJourney> expired = safeJourneyRepository.findByActiveTrueAndExpectedEndTimeBefore(Instant.now());
        
        for (SafeJourney journey : expired) {
            if (!journey.isSosTriggered()) {
                log.warn("Safe Journey expired for user {}. Triggering SOS automatically!", journey.getUser().getFullName());
                
                SosTriggerRequest req = new SosTriggerRequest();
                req.setUserId(journey.getUser().getId());
                req.setGpsLat(journey.getUser().getLastLat());
                req.setGpsLng(journey.getUser().getLastLng());
                req.setLocationText("Hành trình an toàn quá hạn: " + journey.getDestinationName());
                req.setSosType(SosType.SAFE_JOURNEY);
                
                sosService.trigger(req, "SYSTEM", "SERVER");
                
                journey.setSosTriggered(true);
                journey.setActive(false);
                safeJourneyRepository.save(journey);
            }
        }
    }
}
