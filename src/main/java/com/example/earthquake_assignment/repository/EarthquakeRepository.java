package com.example.earthquake_assignement.repository;

import com.example.earthquake_assignement.model.Earthquake;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EarthquakeRepository extends JpaRepository<Earthquake, Long> {

}
