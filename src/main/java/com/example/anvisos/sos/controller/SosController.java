package com.example.anvisos.sos.controller;

import com.example.anvisos.sos.dto.SosAlertPublicResponse;
import com.example.anvisos.sos.dto.SosTriggerRequest;
import com.example.anvisos.sos.dto.SosTriggerResponse;
import com.example.anvisos.sos.service.SosService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/sos")
public class SosController {

    private final SosService sosService;

    public SosController(SosService sosService) {
        this.sosService = sosService;
    }

    /** Kích hoạt SOS — yêu cầu auth */
    @PostMapping("/trigger")
    public SosTriggerResponse trigger(@RequestBody SosTriggerRequest request,
                                      HttpServletRequest httpRequest) {
        System.out.println("[SOS] Trigger: userId=" + request.getUserId()
                + ", type=" + request.getSosType() + ", silent=" + request.isSilent());
        SosService.TriggerResult result = sosService.trigger(
                request, getClientIp(httpRequest), httpRequest.getHeader("User-Agent"));
        return new SosTriggerResponse(result.sent(), result.publicToken());
    }

    /**
     * Public endpoint — không cần auth.
     * Emergency contacts truy cập link này để xem thông tin nạn nhân.
     * Được poll mỗi 5s từ frontend.
     */
    @GetMapping("/public/{token}")
    public SosAlertPublicResponse getPublicAlert(@PathVariable String token) {
        return sosService.getPublicAlert(token);
    }

    /**
     * Cập nhật vị trí GPS mới từ thiết bị nạn nhân.
     * Gọi định kỳ khi nạn nhân di chuyển để cập nhật live.
     */
    @PatchMapping("/public/{token}/location")
    public Map<String, String> updateLocation(
            @PathVariable String token,
            @RequestBody Map<String, Object> body) {
        BigDecimal lat = body.get("lat") != null
                ? new BigDecimal(body.get("lat").toString()) : null;
        BigDecimal lng = body.get("lng") != null
                ? new BigDecimal(body.get("lng").toString()) : null;
        String locationText = (String) body.get("locationText");
        sosService.updateLocation(token, lat, lng, locationText);
        return Map.of("status", "updated");
    }

    /** Đánh dấu đã an toàn */
    @PostMapping("/public/{token}/safe")
    public Map<String, String> markAsSafe(@PathVariable String token, @RequestBody(required = false) Map<String, String> body) {
        String note = body != null ? body.get("note") : "Người dùng xác nhận an toàn";
        sosService.markAsSafe(token, note);
        return Map.of("status", "safe");
    }

    /** Cập nhật URL media (audio/video) */
    @PostMapping("/public/{token}/media")
    public Map<String, String> updateMedia(@PathVariable String token, @RequestBody Map<String, String> body) {
        String mediaUrl = body.get("mediaUrl");
        sosService.updateMediaUrl(token, mediaUrl);
        return Map.of("status", "media_updated");
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
