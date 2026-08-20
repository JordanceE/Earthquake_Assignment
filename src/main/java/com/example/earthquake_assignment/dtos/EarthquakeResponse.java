package com.example.earthquake_assignment.dtos;

import com.example.earthquake_assignment.model.Earthquake;

import java.time.Instant;

public record EarthquakeResponse(
        Long id,
        String usgsEventId,
        Double magnitude,
        String magnitudeType,
        String place,
        Instant eventTime,
        String title,
         Double longitude,
        Double latitude,
        Double depth
) {
    public static EarthquakeResponse toResponse(Earthquake earthquake) {
        return new EarthquakeResponse(
                earthquake.getId(),
                earthquake.getUsgsEventId(),
                earthquake.getMagnitude(),
                earthquake.getMagnitudeType(),
                earthquake.getLocation(),
                earthquake.getEventTime(),
                earthquake.getTitle(),
                earthquake.getLongitude(),
                earthquake.getLatitude(),
                earthquake.getDepth()
        );
    }
}