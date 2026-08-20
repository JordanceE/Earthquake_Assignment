import { useEffect, useState } from "react";
import {
  deleteEarthquake,
  getEarthquakes,
  refreshEarthquakes,
} from "./api/earthquakeApi";
import EarthquakeFilters from "./components/EarthquakeFilters";
import EarthquakeTable from "./components/EarthquakeTable";
import RefreshControls from "./components/RefreshControls";
import EarthquakeMap from "./components/EarthquakeMap";
import "./App.css";

function messageFrom(error) {
  return error instanceof Error
      ? error.message
      : "An unexpected error occurred.";
}

function App() {
  const [earthquakes, setEarthquakes] = useState([]);
  const [activeFilters, setActiveFilters] = useState({});
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadInitialData() {
      try {
        const data = await getEarthquakes();

        if (!cancelled) {
          setEarthquakes(data);
        }
      } catch (requestError) {
        if (!cancelled) {
          setError(messageFrom(requestError));
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadInitialData();

    return () => {
      cancelled = true;
    };
  }, []);

  async function handleSearch(filters) {
    setLoading(true);
    setError("");
    setNotice("");

    try {
      const data = await getEarthquakes(filters);

      setActiveFilters(filters);
      setEarthquakes(data);
    } catch (requestError) {
      setError(messageFrom(requestError));
    } finally {
      setLoading(false);
    }
  }

  async function handleRefresh(after) {
    setRefreshing(true);
    setError("");
    setNotice("");

    try {
      const imported = await refreshEarthquakes(after);
      const visible = await getEarthquakes(activeFilters);

      setEarthquakes(visible);
      setNotice(
          `Refresh completed. ${imported.length} event${
              imported.length === 1 ? "" : "s"
          } stored.`,
      );
    } catch (requestError) {
      setError(messageFrom(requestError));
    } finally {
      setRefreshing(false);
    }
  }

  async function handleDelete(earthquake) {
    const confirmed = window.confirm(
        `Delete "${earthquake.title}"?`,
    );

    if (!confirmed) {
      return;
    }

    setDeletingId(earthquake.id);
    setError("");
    setNotice("");

    try {
      await deleteEarthquake(earthquake.id);

      setEarthquakes((current) =>
          current.filter((item) => item.id !== earthquake.id),
      );

      setNotice("Earthquake deleted.");
    } catch (requestError) {
      setError(messageFrom(requestError));
    } finally {
      setDeletingId(null);
    }
  }

  const validMagnitudes = earthquakes
      .map((earthquake) => earthquake.magnitude)
      .filter(Number.isFinite);

  const strongestMagnitude =
      validMagnitudes.length > 0
          ? Math.max(...validMagnitudes).toFixed(1)
          : "—";

  const mappedEvents = earthquakes.filter(
      (earthquake) =>
          typeof earthquake.latitude === "number" &&
          typeof earthquake.longitude === "number",
  ).length;

  return (
      <div className="app">
        <header className="hero">
          <div>
            <span className="eyebrow">Earthquake monitor</span>
            <h1>Recent seismic activity</h1>
            <p>
              Fetch, filter, store, and explore recent earthquake
              events supplied by the USGS.
            </p>
          </div>

          <a
              className="usgs-link"
              href="https://earthquake.usgs.gov/"
              target="_blank"
              rel="noreferrer"
          >
            Data source: USGS
          </a>
        </header>

        <section className="summary-grid" aria-label="Summary">
          <article className="summary-card">
            <span>Visible events</span>
            <strong>{earthquakes.length}</strong>
          </article>

          <article className="summary-card">
            <span>Strongest magnitude</span>
            <strong>{strongestMagnitude}</strong>
          </article>

          <article className="summary-card">
            <span>Mapped locations</span>
            <strong>{mappedEvents}</strong>
          </article>
        </section>

        <section className="controls-grid">
          <RefreshControls
              refreshing={refreshing}
              onRefresh={handleRefresh}
          />

          <EarthquakeFilters
              disabled={loading || refreshing}
              onSearch={handleSearch}
              onClear={() => handleSearch({})}
          />
        </section>

        {error && (
            <div className="message message--error" role="alert">
              {error}
            </div>
        )}

        {notice && (
            <div className="message message--success" role="status">
              {notice}
            </div>
        )}
        {!loading && (
            <section className="map-card">
              <div className="map-heading">
                <div>
                  <span className="eyebrow">Location overview</span>
                  <h2>Earthquake map</h2>
                </div>

                <span className="result-count">
        {mappedEvents} mapped
      </span>
              </div>

              <EarthquakeMap earthquakes={earthquakes} />
            </section>
        )}
        <section className="results-card">
          <div className="results-heading">
            <div>
              <span className="eyebrow">PostgreSQL records</span>
              <h2>Stored earthquakes</h2>
            </div>

            <span className="result-count">
            {earthquakes.length} result
              {earthquakes.length === 1 ? "" : "s"}
          </span>
          </div>

          {loading ? (
              <div className="loading-state" role="status">
                <span className="spinner" />
                Loading earthquakes…
              </div>
          ) : (
              <EarthquakeTable
                  earthquakes={earthquakes}
                  deletingId={deletingId}
                  onDelete={handleDelete}
              />
          )}
        </section>

        <footer>
          Earthquake times are displayed in your local timezone.
        </footer>
      </div>
  );
}

export default App;