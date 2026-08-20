package com.example.earthquake_assignment.clients;

import com.example.earthquake_assignment.dtos.UsgsFeed;
import com.example.earthquake_assignment.exceptions.UsgsApiException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UsgsEarthquakeClient {
    private static final String USGS_BASE_URL =
            "https://earthquake.usgs.gov";

    private static final String USGS_FEED_PATH =
            "/earthquakes/feed/v1.0/summary/all_hour.geojson";
    private final RestClient restClient;
    public UsgsEarthquakeClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl(USGS_BASE_URL).build();
    }
    public UsgsFeed fetchLatestEarthquakes(){
        try {
            UsgsFeed feed = restClient.get()
                    .uri(USGS_FEED_PATH)
                    .retrieve()
                    .body(UsgsFeed.class);
            if(feed == null){
                throw new UsgsApiException(
                        "USGS returned an empty response"
                );
            }
            return feed;
        }
        catch(UsgsApiException e){
            throw new UsgsApiException("Unable to retrieve the earthquake data from USGS", e);
        }
    }
}
