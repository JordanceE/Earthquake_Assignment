package com.example.earthquake_assignement.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class Earthquake {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;
    @Column(name = "usgs_event_id", nullable = false, unique = true)
    private String usgsEventId;
    private Double magnitude;
    private String magnitudeType;
    private String location;
    private Instant eventTime;
    private String title;

    public Earthquake(String usgsEventId, Double magnitude, String magnitudeType, String location, Instant eventTime, String title) {
        this.usgsEventId = usgsEventId;
        this.magnitude = magnitude;
        this.magnitudeType = magnitudeType;
        this.location = location;
        this.eventTime = eventTime;
        this.title = title;
    }
    protected Earthquake() {}

//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public Long getUsgsEventId() {
//        return usgsEventId;
//    }
//
//    public void setUsgsEventId(Long usgsEventId) {
//        this.usgsEventId = usgsEventId;
//    }
//
//    public Double getMagnitude() {
//        return magnitude;
//    }
//
//    public void setMagnitude(Double magnitude) {
//        this.magnitude = magnitude;
//    }
//
//    public String getMagnitudeType() {
//        return magnitudeType;
//    }
//
//    public void setMagnitudeType(String magnitudeType) {
//        this.magnitudeType = magnitudeType;
//    }
//
//    public String getLocation() {
//        return location;
//    }
//
//    public void setLocation(String location) {
//        this.location = location;
//    }
//
//    public Instant getEventTime() {
//        return eventTime;
//    }
//
//    public void setEventTime(Instant eventTime) {
//        this.eventTime = eventTime;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }
}
