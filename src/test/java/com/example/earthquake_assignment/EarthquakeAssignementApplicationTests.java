package com.example.earthquake_assignment;

import com.example.earthquake_assignment.clients.UsgsEarthquakeClient;
import com.example.earthquake_assignment.dtos.UsgsFeature;
import com.example.earthquake_assignment.dtos.UsgsFeed;
import com.example.earthquake_assignment.dtos.UsgsGeometry;
import com.example.earthquake_assignment.dtos.UsgsProperties;
import com.example.earthquake_assignment.exceptions.EarthquakeIdNotFoundException;
import com.example.earthquake_assignment.exceptions.UsgsApiException;
import com.example.earthquake_assignment.model.Earthquake;
import com.example.earthquake_assignment.repository.EarthquakeRepository;
import com.example.earthquake_assignment.service.EarthquakeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest
class EarthquakeAssignementApplicationTests{

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private EarthquakeService earthquakeService;

    @Autowired
    private EarthquakeRepository earthquakeRepository;

    @MockitoBean
    private UsgsEarthquakeClient usgsEarthquakeClient;

    @BeforeEach
    void clearDatabase() {
        earthquakeRepository.deleteAllInBatch();
    }

    @Test
    void searchFiltersMagnitudeStrictlyGreaterThanGivenValue() {
        saveEarthquake("below", 1.9, "2026-08-20T10:00:00Z");
        saveEarthquake("equal", 2.0, "2026-08-20T10:00:00Z");
        saveEarthquake("above", 2.1, "2026-08-20T10:00:00Z");

        List<Earthquake> result =
                earthquakeService.search(2.0, null);

        assertThat(result)
                .extracting(Earthquake::getUsgsEventId)
                .containsExactly("above");
    }

    @Test
    void searchFiltersEventTimeStrictlyAfterGivenTime() {
        Instant cutoff = Instant.parse("2026-08-20T10:00:00Z");

        saveEarthquake("before", 3.0, "2026-08-20T09:59:59Z");
        saveEarthquake("equal", 3.0, "2026-08-20T10:00:00Z");
        saveEarthquake("after", 3.0, "2026-08-20T10:00:01Z");

        List<Earthquake> result =
                earthquakeService.search(null, cutoff);

        assertThat(result)
                .extracting(Earthquake::getUsgsEventId)
                .containsExactly("after");
    }

    @Test
    void searchCombinesMagnitudeAndTimeFilters() {
        Instant cutoff = Instant.parse("2026-08-20T10:00:00Z");

        saveEarthquake("matches-both", 3.5, "2026-08-20T11:00:00Z");
        saveEarthquake("magnitude-only", 3.5, "2026-08-20T09:00:00Z");
        saveEarthquake("time-only", 1.5, "2026-08-20T11:00:00Z");
        saveEarthquake("matches-neither", 1.5, "2026-08-20T09:00:00Z");

        List<Earthquake> result =
                earthquakeService.search(2.0, cutoff);

        assertThat(result)
                .extracting(Earthquake::getUsgsEventId)
                .containsExactly("matches-both");
    }

    @Test
    void findByIdThrowsExceptionWhenEarthquakeDoesNotExist() {
        assertThatThrownBy(() -> earthquakeService.findById(999_999L))
                .isInstanceOf(EarthquakeIdNotFoundException.class)
                .hasMessage("Earthquake with id 999999 not found");
    }

    @Test
    void refreshFiltersMapsAndReplacesExistingRecords() {
        Instant cutoff = Instant.parse("2026-08-20T09:00:00Z");

        saveEarthquake("old-database-record", 5.0,
                "2026-08-20T08:00:00Z");

        UsgsFeature valid = feature(
                "valid-event",
                3.4,
                "2026-08-20T10:00:00Z",
                "earthquake",
                List.of(21.4, 41.9, 10.0)
        );

        // Exactly 2.0 must be excluded because the rule is > 2.0.
        UsgsFeature magnitudeAtBoundary = feature(
                "magnitude-at-boundary",
                2.0,
                "2026-08-20T10:30:00Z",
                "earthquake",
                List.of(21.5, 42.0, 5.0)
        );

        // Exactly at the cutoff must be excluded because the rule is "after".
        UsgsFeature timeAtBoundary = feature(
                "time-at-boundary",
                4.0,
                "2026-08-20T09:00:00Z",
                "earthquake",
                List.of(21.6, 42.1, 15.0)
        );

        UsgsFeature wrongType = feature(
                "quarry-event",
                4.5,
                "2026-08-20T11:00:00Z",
                "quarry blast",
                List.of(21.7, 42.2, 8.0)
        );

        UsgsFeature missingId = feature(
                null,
                5.0,
                "2026-08-20T12:00:00Z",
                "earthquake",
                List.of(21.8, 42.3, 12.0)
        );

        when(usgsEarthquakeClient.fetchLatestEarthquakes())
                .thenReturn(new UsgsFeed(Arrays.asList(
                        valid,
                        magnitudeAtBoundary,
                        timeAtBoundary,
                        wrongType,
                        missingId,
                        null
                )));

        List<Earthquake> result =
                earthquakeService.refreshFromUsgs(cutoff, false);

        assertThat(result)
                .singleElement()
                .satisfies(earthquake -> {
                    assertThat(earthquake.getUsgsEventId())
                            .isEqualTo("valid-event");
                    assertThat(earthquake.getMagnitude()).isEqualTo(3.4);
                    assertThat(earthquake.getMagnitudeType()).isEqualTo("ml");
                    assertThat(earthquake.getEventTime())
                            .isEqualTo(Instant.parse(
                                    "2026-08-20T10:00:00Z"
                            ));
                    assertThat(earthquake.getLongitude()).isEqualTo(21.4);
                    assertThat(earthquake.getLatitude()).isEqualTo(41.9);
                    assertThat(earthquake.getDepth()).isEqualTo(10.0);
                });

        assertThat(earthquakeRepository.findAll())
                .extracting(Earthquake::getUsgsEventId)
                .containsExactly("valid-event");
    }

    @Test
    void refreshDoesNotDeleteExistingDataWhenUsgsFails() {
        Instant cutoff = Instant.parse("2026-08-20T09:00:00Z");

        saveEarthquake(
                "existing-event",
                4.0,
                "2026-08-20T10:00:00Z"
        );

        when(usgsEarthquakeClient.fetchLatestEarthquakes())
                .thenThrow(new UsgsApiException("USGS unavailable"));

        assertThatThrownBy(
                () -> earthquakeService.refreshFromUsgs(cutoff,false )
        )
                .isInstanceOf(UsgsApiException.class)
                .hasMessage("USGS unavailable");

        assertThat(earthquakeRepository.findAll())
                .extracting(Earthquake::getUsgsEventId)
                .containsExactly("existing-event");
    }
    @Test
    void refreshCanImportAllValidEarthquakes() {
        UsgsFeature lowMagnitude = feature(
                "low-event",
                1.2,
                "2026-08-20T08:00:00Z",
                "earthquake",
                List.of(21.4, 41.9, 10.0)
        );

        UsgsFeature higherMagnitude = feature(
                "higher-event",
                4.5,
                "2026-08-20T10:00:00Z",
                "earthquake",
                List.of(21.5, 42.0, 5.0)
        );

        when(usgsEarthquakeClient.fetchLatestEarthquakes())
                .thenReturn(new UsgsFeed(
                        List.of(lowMagnitude, higherMagnitude)
                ));

        List<Earthquake> result =
                earthquakeService.refreshFromUsgs(null, true);

        assertThat(result)
                .extracting(Earthquake::getUsgsEventId)
                .containsExactlyInAnyOrder(
                        "low-event",
                        "higher-event"
                );
    }
    @Test
    void refreshRejectsMissingTimeCutoffBeforeCallingUsgs() {
        assertThatThrownBy(
                () -> earthquakeService.refreshFromUsgs(null, false)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A time cutoff must be provided");

        verifyNoInteractions(usgsEarthquakeClient);
    }

    @Test
    void searchRejectsNonFiniteMagnitude() {
        assertThatThrownBy(
                () -> earthquakeService.search(Double.NaN, null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Magnitude must be a finite number");
    }

    private Earthquake saveEarthquake(
            String usgsId,
            double magnitude,
            String time
    ) {
        Earthquake earthquake = new Earthquake(
                usgsId,
                magnitude,
                "mw",
                "Test location",
                Instant.parse(time),
                "Test earthquake"
        );

        return earthquakeRepository.saveAndFlush(earthquake);
    }

    private UsgsFeature feature(
            String id,
            Double magnitude,
            String time,
            String type,
            List<Double> coordinates
    ) {
        return new UsgsFeature(
                id,
                new UsgsProperties(
                        magnitude,
                        "ml",
                        "Test location",
                        Instant.parse(time).toEpochMilli(),
                        "Test earthquake",
                        type
                ),
                new UsgsGeometry("Point", coordinates)
        );
    }
}