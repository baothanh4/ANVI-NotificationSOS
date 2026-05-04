package com.example.anvisos.sos.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SosTriggerRequest {
    @NotNull
    private Long userId;
    private Long cardId;
    private BigDecimal gpsLat;
    private BigDecimal gpsLng;
    private String locationText;
    private String ipLocation;
    private String manualAddress;

    public SosTriggerRequest() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Long getCardId() { return cardId; }
    public void setCardId(Long cardId) { this.cardId = cardId; }
    
    public BigDecimal getGpsLat() { return gpsLat; }
    public void setGpsLat(BigDecimal gpsLat) { this.gpsLat = gpsLat; }
    
    public BigDecimal getGpsLng() { return gpsLng; }
    public void setGpsLng(BigDecimal gpsLng) { this.gpsLng = gpsLng; }
    
    public String getLocationText() { return locationText; }
    public void setLocationText(String locationText) { this.locationText = locationText; }
    
    public String getIpLocation() { return ipLocation; }
    public void setIpLocation(String ipLocation) { this.ipLocation = ipLocation; }
    
    public String getManualAddress() { return manualAddress; }
    public void setManualAddress(String manualAddress) { this.manualAddress = manualAddress; }
}
