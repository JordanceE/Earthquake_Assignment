package com.example.earthquake_assignment.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
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
    private Double longitude;
    private Double latitude;
    private Double depth;

    public Earthquake(String usgsEventId, Double magnitude, String magnitudeType, String location, Instant eventTime, String title) {
        this.usgsEventId = usgsEventId;
        this.magnitude = magnitude;
        this.magnitudeType = magnitudeType;
        this.location = location;
        this.eventTime = eventTime;
        this.title = title;

    }

    public Earthquake(String usgsEventId, String magnitudeType, Double magnitude, String location, Instant eventTime, String title, Double longitude, Double latitude, Double depth) {
        this.magnitudeType = magnitudeType;
        this.usgsEventId = usgsEventId;
        this.magnitude = magnitude;
        this.location = location;
        this.eventTime = eventTime;
        this.title = title;
        this.longitude = longitude;
        this.latitude = latitude;
        this.depth = depth;
    }

}
