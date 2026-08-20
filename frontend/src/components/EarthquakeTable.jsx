function magnitudeClass(magnitude) {
    if (magnitude >= 5) {
        return "magnitude magnitude--high";
    }

    if (magnitude >= 3) {
        return "magnitude magnitude--medium";
    }

    return "magnitude magnitude--low";
}

function formatCoordinate(value) {
    return typeof value === "number" ? value.toFixed(3) : "—";
}

function EarthquakeTable({
                             earthquakes,
                             deletingId,
                             onDelete,
                         }) {
    if (earthquakes.length === 0) {
        return (
            <div className="empty-state">
                <span className="empty-state__icon">◎</span>
                <h3>No earthquakes found</h3>
                <p>Refresh the USGS feed or change the filters.</p>
            </div>
        );
    }

    return (
        <div className="table-wrapper">
            <table>
                <thead>
                <tr>
                    <th>Magnitude</th>
                    <th>Place</th>
                    <th>Time</th>
                    <th>Depth</th>
                    <th>Coordinates</th>
                    <th>
                        <span className="visually-hidden">Actions</span>
                    </th>
                </tr>
                </thead>

                <tbody>
                {earthquakes.map((earthquake) => {
                    const hasCoordinates =
                        typeof earthquake.latitude === "number" &&
                        typeof earthquake.longitude === "number";

                    const mapUrl = hasCoordinates
                        ? `https://www.openstreetmap.org/?mlat=${earthquake.latitude}&mlon=${earthquake.longitude}#map=7/${earthquake.latitude}/${earthquake.longitude}`
                        : null;

                    return (
                        <tr key={earthquake.id}>
                            <td>
                  <span
                      className={magnitudeClass(
                          earthquake.magnitude,
                      )}
                  >
                    {earthquake.magnitude.toFixed(1)}
                  </span>

                                <span className="magnitude-type">
                    {earthquake.magnitudeType ?? "Unknown"}
                  </span>
                            </td>

                            <td className="place-cell">
                                <strong>{earthquake.place}</strong>
                                <span>{earthquake.title}</span>
                            </td>

                            <td>
                                <time dateTime={earthquake.eventTime}>
                                    {new Date(earthquake.eventTime).toLocaleString()}
                                </time>
                            </td>

                            <td>
                                {earthquake.depth !== null
                                    ? `${earthquake.depth.toFixed(1)} km`
                                    : "—"}
                            </td>

                            <td>
                                {mapUrl ? (
                                    <a
                                        className="map-link"
                                        href={mapUrl}
                                        target="_blank"
                                        rel="noreferrer"
                                    >
                                        {formatCoordinate(earthquake.latitude)},{" "}
                                        {formatCoordinate(earthquake.longitude)}
                                    </a>
                                ) : (
                                    "—"
                                )}
                            </td>

                            <td className="action-cell">
                                <button
                                    type="button"
                                    className="delete-button"
                                    disabled={deletingId === earthquake.id}
                                    onClick={() => onDelete(earthquake)}
                                    aria-label={`Delete ${earthquake.title}`}
                                >
                                    {deletingId === earthquake.id
                                        ? "Deleting…"
                                        : "Delete"}
                                </button>
                            </td>
                        </tr>
                    );
                })}
                </tbody>
            </table>
        </div>
    );
}

export default EarthquakeTable;