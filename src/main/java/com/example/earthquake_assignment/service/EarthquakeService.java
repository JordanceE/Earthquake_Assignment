package com.example.earthquake_assignment.service;

import com.example.earthquake_assignment.model.Earthquake;

import java.time.Instant;
import java.util.List;


public interface EarthquakeService{
    List<Earthquake> findAll();
    Earthquake findById(Long id);
    List<Earthquake> search(Double minimumMagnitude, Instant after);

    void deleteById(Long id);
    List<Earthquake> refreshFromUsgs(Instant after, boolean includeAll);
    List<Earthquake> replaceAll(List<Earthquake> earthquakes);
}
