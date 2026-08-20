import { useState } from "react";

function EarthquakeFilters({ disabled, onSearch, onClear }) {
    const [minimumMagnitude, setMinimumMagnitude] = useState("");
    const [after, setAfter] = useState("");
    const [validationError, setValidationError] = useState("");

    function handleSubmit(event) {
        event.preventDefault();

        const filters = {};

        if (minimumMagnitude !== "") {
            const parsedMagnitude = Number(minimumMagnitude);

            if (!Number.isFinite(parsedMagnitude)) {
                setValidationError("Enter a valid magnitude.");
                return;
            }

            filters.minMagnitude = parsedMagnitude;
        }

        if (after) {
            filters.after = new Date(after).toISOString();
        }

        setValidationError("");
        onSearch(filters);
    }

    function handleClear() {
        setMinimumMagnitude("");
        setAfter("");
        setValidationError("");
        onClear();
    }

    return (
        <form className="control-card" onSubmit={handleSubmit}>
            <div className="control-card__heading">
                <div>
                    <span className="eyebrow">Database search</span>
                    <h2>Filter stored events</h2>
                </div>
            </div>

            <div className="form-grid">
                <label>
                    Minimum magnitude
                    <input
                        type="number"
                        step="0.1"
                        value={minimumMagnitude}
                        onChange={(event) =>
                            setMinimumMagnitude(event.target.value)
                        }
                        placeholder="For example: 2.0"
                        disabled={disabled}
                    />
                </label>

                <label>
                    Event time after
                    <input
                        type="datetime-local"
                        value={after}
                        onChange={(event) => setAfter(event.target.value)}
                        disabled={disabled}
                    />
                </label>
            </div>

            {validationError && (
                <p className="form-error">{validationError}</p>
            )}

            <div className="button-row">
                <button type="submit" disabled={disabled}>
                    Apply filters
                </button>

                <button
                    type="button"
                    className="button-secondary"
                    onClick={handleClear}
                    disabled={disabled}
                >
                    Clear
                </button>
            </div>
        </form>
    );
}

export default EarthquakeFilters;