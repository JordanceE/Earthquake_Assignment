package com.example.earthquake_assignment.web;

import com.example.earthquake_assignment.dtos.EarthquakeResponse;
import com.example.earthquake_assignment.service.EarthquakeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;


@RestController
@RequestMapping("api/earthquake")
public class EarthController{
    public final EarthquakeService earthquakeService;
    public EarthController(EarthquakeService earthquakeService){
        this.earthquakeService = earthquakeService;
    }

    @GetMapping("/")
    public List<EarthquakeResponse> findAll(@RequestParam(required = false) Double minMagnitude,
                                            @RequestParam(required = false)
                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after) {
        return earthquakeService.search(minMagnitude, after).stream().map(EarthquakeResponse::toResponse).toList();
    }
    @GetMapping("/{id}")
    public EarthquakeResponse findById(@PathVariable Long id) {
        return EarthquakeResponse.toResponse(earthquakeService.findById(id));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        earthquakeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/refresh")
    public List<EarthquakeResponse> refresh(@RequestParam(required = false)
                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                        Instant after,
                                            @RequestParam(required = false)
                                            boolean includeAll) {
        return earthquakeService.refreshFromUsgs(after, includeAll).stream().map(EarthquakeResponse::toResponse).toList();
    }
}
