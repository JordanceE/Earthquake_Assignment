import { useEffect, useMemo } from "react";
import {
    CircleMarker,
    MapContainer,
    Popup,
    TileLayer,
    useMap,
} from "react-leaflet";
import "leaflet/dist/leaflet.css";

function markerStyle(magnitude) {
    if (magnitude >= 5) {
        return {
            color: "#991b1b",
            fillColor: "#dc2626",
            fillOpacity: 0.75,
        };
    }

    if (magnitude >= 3) {
        return {
            color: "#b45309",
            fillColor: "#f59e0b",
            fillOpacity: 0.75,
        };
    }

    return {
        color: "#0f6571",
        fillColor: "#22a6b3",
        fillOpacity: 0.75,
    };
}

function markerRadius(magnitude) {
    if (!Number.isFinite(magnitude)) {
        return 6;
    }

    return Math.max(6, magnitude * 3);
}

function MapBoundsUpdater({ positions }) {
    const map = useMap();

    useEffect(() => {
        if (positions.length === 1) {
            map.setView(positions[0], 7);
            return;
        }

        if (positions.length > 1) {
            map.fitBounds(positions, { padding: [35, 35], maxZoom: 7,});
        }
    }, [map, positions]);

    return null;
}

function EarthquakeMap({ earthquakes }) {
    const mappedEarthquakes = useMemo(
        () =>
            earthquakes.filter(
                (earthquake) => Number.isFinite(earthquake.latitude) && Number.isFinite(earthquake.longitude),
            ),
        [earthquakes],
    );

    const positions = useMemo(
        () =>
            mappedEarthquakes.map((earthquake) => [earthquake.latitude, earthquake.longitude,]),
        [mappedEarthquakes],
    );

    if (mappedEarthquakes.length === 0) {
        return (
            <div className="map-empty">
                No earthquake coordinates are available to display.
            </div>
        );
    }

    return (
        <MapContainer
            center={[20, 0]}
            zoom={2}
            minZoom={2}
            scrollWheelZoom
            className="earthquake-map"
        >
            <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
            />

            <MapBoundsUpdater positions={positions} />

            {mappedEarthquakes.map((earthquake) => (
                <CircleMarker
                    key={earthquake.id}
                    center={[earthquake.latitude, earthquake.longitude,]}
                    radius={markerRadius(earthquake.magnitude)}
                    pathOptions={markerStyle(earthquake.magnitude)}
                >
                    <Popup>
                        <div className="map-popup">
                            <strong>{earthquake.title}</strong>

                            <dl>
                                <div>
                                    <dt>Magnitude</dt>
                                    <dd>
                                        {earthquake.magnitude}{" "}
                                        {earthquake.magnitudeType ?? ""}
                                    </dd>
                                </div>

                                <div>
                                    <dt>Depth</dt>
                                    <dd>
                                        {Number.isFinite(earthquake.depth)
                                            ? `${earthquake.depth.toFixed(1)} km`
                                            : "Unknown"}
                                    </dd>
                                </div>

                                <div>
                                    <dt>Time</dt>
                                    <dd>
                                        {new Date(earthquake.eventTime,).toLocaleString()}
                                    </dd>
                                </div>
                            </dl>
                        </div>
                    </Popup>
                </CircleMarker>
            ))}
        </MapContainer>
    );
}

export default EarthquakeMap;