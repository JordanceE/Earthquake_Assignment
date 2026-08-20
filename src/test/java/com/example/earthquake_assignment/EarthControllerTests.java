package com.example.earthquake_assignment;

import com.example.earthquake_assignment.model.Earthquake;
import com.example.earthquake_assignment.service.EarthquakeService;
import com.example.earthquake_assignment.web.EarthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EarthControllerTests {

    @Mock
    private EarthquakeService earthquakeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new EarthController(earthquakeService))
                .build();
    }

    @Test
    void getEarthquakesPassesFiltersToService() throws Exception {
        Instant after = Instant.parse("2026-08-20T09:00:00Z");
        Earthquake earthquake = earthquake(7L);

        when(earthquakeService.search(2.0, after))
                .thenReturn(List.of(earthquake));

        mockMvc.perform(get("/api/earthquake/")
                        .param("minMagnitude", "2.0")
                        .param("after", "2026-08-20T09:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].usgsEventId")
                        .value("test-event"))
                .andExpect(jsonPath("$[0].magnitude").value(3.4))
                .andExpect(jsonPath("$[0].place")
                        .value("Test location"))
                .andExpect(jsonPath("$[0].longitude").value(21.4))
                .andExpect(jsonPath("$[0].latitude").value(41.9))
                .andExpect(jsonPath("$[0].depth").value(10.0));

        verify(earthquakeService).search(2.0, after);
    }

    @Test
    void getEarthquakeByIdReturnsResponse() throws Exception {
        Earthquake earthquake = earthquake(7L);

        when(earthquakeService.findById(7L))
                .thenReturn(earthquake);

        mockMvc.perform(get("/api/earthquake/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.usgsEventId")
                        .value("test-event"))
                .andExpect(jsonPath("$.eventTime")
                        .value("2026-08-20T10:00:00Z"));

        verify(earthquakeService).findById(7L);
    }

    @Test
    void deleteEarthquakeReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/earthquake/{id}", 7L))
                .andExpect(status().isNoContent());

        verify(earthquakeService).deleteById(7L);
    }

    @Test
    void refreshPassesTimeCutoffToService() throws Exception {
        Instant after = Instant.parse("2026-08-20T09:00:00Z");
        Earthquake earthquake = earthquake(7L);

        when(earthquakeService.refreshFromUsgs(after, false))
                .thenReturn(List.of(earthquake));

        mockMvc.perform(post("/api/earthquake/refresh")
                        .param("after", "2026-08-20T09:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usgsEventId")
                        .value("test-event"));

        verify(earthquakeService).refreshFromUsgs(after, false);
    }

    private Earthquake earthquake(Long id) {
        Earthquake earthquake = new Earthquake(
                "test-event",
                3.4,
                "ml",
                "Test location",
                Instant.parse("2026-08-20T10:00:00Z"),
                "M 3.4 - Test location"
        );

        earthquake.setId(id);
        earthquake.setLongitude(21.4);
        earthquake.setLatitude(41.9);
        earthquake.setDepth(10.0);

        return earthquake;
    }
}