package com.example.earthquake_assignment.repository;

import com.example.earthquake_assignment.model.Earthquake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;


@Repository
public interface EarthquakeRepository extends JpaRepository<Earthquake, Long> {
    boolean existsByUsgsEventId(String usgsEventId);
    List<Earthquake> findByMagnitudeGreaterThan(Double magnitude);
    List<Earthquake> findByEventTimeAfter(Instant eventTime);
    List<Earthquake> findByMagnitudeGreaterThanAndEventTimeAfter(Double magnitude, Instant eventTime);
}
