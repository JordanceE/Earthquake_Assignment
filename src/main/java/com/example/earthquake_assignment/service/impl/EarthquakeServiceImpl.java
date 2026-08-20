package com.example.earthquake_assignment.service.impl;

import com.example.earthquake_assignment.clients.UsgsEarthquakeClient;
import com.example.earthquake_assignment.dtos.UsgsFeature;
import com.example.earthquake_assignment.dtos.UsgsFeed;
import com.example.earthquake_assignment.dtos.UsgsGeometry;
import com.example.earthquake_assignment.dtos.UsgsProperties;
import com.example.earthquake_assignment.exceptions.EarthquakeIdNotFoundException;
import com.example.earthquake_assignment.model.Earthquake;
import com.example.earthquake_assignment.repository.EarthquakeRepository;
import com.example.earthquake_assignment.service.EarthquakeService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.time.DateTimeException;
import java.time.Instant;

import java.util.*;



@Service
public class EarthquakeServiceImpl implements EarthquakeService {
    private static final double MINIMUM_IMPORT_MAGNITUDE = 2.0;
    private final EarthquakeRepository earthquakeRepository;
    private final UsgsEarthquakeClient usgsEarthquakeClient;


    public EarthquakeServiceImpl(EarthquakeRepository earthquakeRepository,  UsgsEarthquakeClient usgsEarthquakeClient) {
        this.earthquakeRepository = earthquakeRepository;
        this.usgsEarthquakeClient = usgsEarthquakeClient;
    }


    @Override
    public List<Earthquake> findAll() {
        return earthquakeRepository.findAll(Sort.by(Sort.Direction.DESC, "eventTime"));
    }

    @Override
    public Earthquake findById(Long id) {
        return earthquakeRepository.findById(id).orElseThrow(() ->
                new EarthquakeIdNotFoundException(id)
        );
    }


    @Override
    public List<Earthquake> search(Double minimumMagnitude, Instant after) {
        validateMagnitudeFilter(minimumMagnitude);
        if (minimumMagnitude != null && after != null) {
            return earthquakeRepository.findByMagnitudeGreaterThanAndEventTimeAfter(minimumMagnitude, after);
        }
        if  (minimumMagnitude != null) {
            return earthquakeRepository.findByMagnitudeGreaterThan(minimumMagnitude);
        }
        if  (after != null) {
            return earthquakeRepository.findByEventTimeAfter(after);
        }
        return earthquakeRepository.findAll();
    }



    @Override
    public void deleteById(Long id) {
        earthquakeRepository.delete(findById(id));
    }

    @Override
    @Transactional
    public List<Earthquake> refreshFromUsgs(Instant after, boolean includeAll) {
        if  (after == null && !includeAll) {
            throw new IllegalArgumentException("A time cutoff must be provided");
        }
        UsgsFeed feed = usgsEarthquakeClient.fetchLatestEarthquakes();
        List<Earthquake> filteredEarthquakes = validateFilterAndMap(feed, after, includeAll);
        earthquakeRepository.deleteAllInBatch();
        return earthquakeRepository.saveAll(filteredEarthquakes);
    }
    @Override
    @Transactional
    public List<Earthquake> replaceAll(List<Earthquake> earthquakes) {
        if (earthquakes == null) {
            throw new IllegalArgumentException(
                    "Earthquake list must not be null"
            );
        }
        earthquakeRepository.deleteAllInBatch();
        return earthquakeRepository.saveAll(earthquakes);
    }
    private List<Earthquake> validateFilterAndMap(UsgsFeed feed, Instant after, boolean includeAll) {
        if (feed == null || feed.features() == null) {
            throw new IllegalArgumentException(
                    "USGS response does not contain a features array"
            );
        }
        Map<String, Earthquake> earthquakesByUsgsId = new LinkedHashMap<>();
        for (UsgsFeature feature : feed.features()) {
            Earthquake earthquake = mapFilteredFeature(feature, after, includeAll);
            if (earthquake != null) {
                earthquakesByUsgsId.put(earthquake.getUsgsEventId(), earthquake);
            }
        }
        return new ArrayList<>(earthquakesByUsgsId.values());
    }
    private Earthquake mapFilteredFeature(UsgsFeature feature, Instant after, boolean includeAll) {
        if  (feature == null || !hasText(feature.id()) || feature.properties() == null) {
            return null;
        }
        UsgsProperties properties = feature.properties();
        if (properties.mag() == null || !Double.isFinite(properties.mag()) || properties.time() == null) {
            return null;
        }

        if (!"earthquake".equalsIgnoreCase(properties.type())){
            return null;
        }
        Instant eventTime;
        try {
            eventTime = Instant.ofEpochMilli(properties.time());
        }
        catch (DateTimeException | ArithmeticException  e) {
            return null;
        }
        if (!includeAll) {
            if (properties.mag() <= MINIMUM_IMPORT_MAGNITUDE) {
                return null;
            }

            if (!eventTime.isAfter(after)) {
                return null;
            }
        }
        String place = hasText(properties.place()) ? properties.place(): "Uknown location";
        String title = hasText(properties.title()) ? properties.title(): "M " + properties.mag() + " - " + place;

        Earthquake earthquake = new Earthquake(
                feature.id(),
                properties.mag(),
                properties.magType(),
                place,
                eventTime,
                title
        );
        addCoordinates(earthquake, feature.geometry());
        return earthquake;
    }
    private void addCoordinates(Earthquake earthquake, UsgsGeometry geometry) {
        if (geometry == null
                || !"Point".equalsIgnoreCase(geometry.type())
                || geometry.coordinates() == null
                || geometry.coordinates().size() < 2) {
            return;
        }
        List<Double> coordinates = geometry.coordinates();
        Double longtitude = coordinates.get(0);
        Double latitude = coordinates.get(1);

        if (isValidLongitude(longtitude) && isValidLatitude(latitude)){
            earthquake.setLongitude(longtitude);
            earthquake.setLatitude(latitude);
        }
        if (coordinates.size() >= 3) {
            Double depth = coordinates.get(2);
            if (depth != null && Double.isFinite(depth)) {
                earthquake.setDepth(depth);
            }
        }

    }
    private void validateMagnitudeFilter(
            Double minimumMagnitude
    ) {
        if (minimumMagnitude != null
                && !Double.isFinite(minimumMagnitude)) {
            throw new IllegalArgumentException(
                    "Magnitude must be a finite number"
            );
        }
    }
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
    private boolean isValidLatitude(Double latitude) {
        return latitude != null
                && Double.isFinite(latitude)
                && latitude >= -90
                && latitude <= 90;
    }

    private boolean isValidLongitude(Double longtitude) {
        return longtitude != null && longtitude >= -180 && longtitude <= 180;
    }
}
