package com.example.anvisos.sos.controller;

import com.example.anvisos.model.entity.SafeJourney;
import com.example.anvisos.sos.service.SafeJourneyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/journey")
@RequiredArgsConstructor
public class SafeJourneyController {

    private final SafeJourneyService safeJourneyService;

    @PostMapping("/start")
    public SafeJourney start(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        int duration = Integer.parseInt(body.get("duration").toString());
        String destName = (String) body.get("destinationName");
        BigDecimal lat = body.get("lat") != null ? new BigDecimal(body.get("lat").toString()) : null;
        BigDecimal lng = body.get("lng") != null ? new BigDecimal(body.get("lng").toString()) : null;
        
        return safeJourneyService.startJourney(userId, duration, destName, lat, lng);
    }

    @PostMapping("/end")
    public Map<String, String> end(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        safeJourneyService.endJourney(userId);
        return Map.of("status", "journey_ended");
    }
}
