package com.example.anvisos.sos.dto;

import java.math.BigDecimal;

/**
 * Public response trả về cho emergency contacts.
 * Không cần auth — chỉ cần token hợp lệ.
 */
public class SosAlertPublicResponse {

    private String victimName;
    private String victimPhone;
    private String avatarUrl;      // URL ảnh avatar hoặc null
    private String initials;       // 2 chữ cái đầu tên để render avatar fallback
    private String bloodType;
    private String allergies;
    private String conditions;
    private String emergencyNote;
    private Integer birthYear;

    private BigDecimal lat;
    private BigDecimal lng;
    private String locationText;

    private String triggeredAt;    // Formatted VN time
    private String updatedAt;
    private boolean active;

    // Google Maps URLs
    private String googleMapsViewUrl;
    private String googleMapsDirectionsUrl;

    public SosAlertPublicResponse() {}

    // Getters & Setters
    public String getVictimName()           { return victimName; }
    public void setVictimName(String v)     { this.victimName = v; }

    public String getVictimPhone()          { return victimPhone; }
    public void setVictimPhone(String v)    { this.victimPhone = v; }

    public String getAvatarUrl()            { return avatarUrl; }
    public void setAvatarUrl(String v)      { this.avatarUrl = v; }

    public String getInitials()             { return initials; }
    public void setInitials(String v)       { this.initials = v; }

    public String getBloodType()            { return bloodType; }
    public void setBloodType(String v)      { this.bloodType = v; }

    public String getAllergies()            { return allergies; }
    public void setAllergies(String v)      { this.allergies = v; }

    public String getConditions()           { return conditions; }
    public void setConditions(String v)     { this.conditions = v; }

    public String getEmergencyNote()        { return emergencyNote; }
    public void setEmergencyNote(String v)  { this.emergencyNote = v; }

    public Integer getBirthYear()           { return birthYear; }
    public void setBirthYear(Integer v)     { this.birthYear = v; }

    public BigDecimal getLat()              { return lat; }
    public void setLat(BigDecimal v)        { this.lat = v; }

    public BigDecimal getLng()              { return lng; }
    public void setLng(BigDecimal v)        { this.lng = v; }

    public String getLocationText()         { return locationText; }
    public void setLocationText(String v)   { this.locationText = v; }

    public String getTriggeredAt()          { return triggeredAt; }
    public void setTriggeredAt(String v)    { this.triggeredAt = v; }

    public String getUpdatedAt()            { return updatedAt; }
    public void setUpdatedAt(String v)      { this.updatedAt = v; }

    public boolean isActive()               { return active; }
    public void setActive(boolean v)        { this.active = v; }

    public String getGoogleMapsViewUrl()            { return googleMapsViewUrl; }
    public void setGoogleMapsViewUrl(String v)       { this.googleMapsViewUrl = v; }

    public String getGoogleMapsDirectionsUrl()       { return googleMapsDirectionsUrl; }
    public void setGoogleMapsDirectionsUrl(String v) { this.googleMapsDirectionsUrl = v; }
}
