import { useState } from "react";

function defaultCutoff() {
    const oneHourAgo = new Date(Date.now() - 60 * 60 * 1000);
    const timezoneOffset = oneHourAgo.getTimezoneOffset() * 60_000;

    return new Date(oneHourAgo.getTime() - timezoneOffset)
        .toISOString()
        .slice(0, 16);
}

function RefreshControls({ refreshing, onRefresh }) {
    const [after, setAfter] = useState(defaultCutoff);
    const [validationError, setValidationError] = useState("");
    const [includeAll, setIncludeAll] = useState(false);
    function handleSubmit(event) {
        event.preventDefault();

        if (!includeAll && !after) {
            setValidationError("Choose a time cutoff.");
            return;
        }

        setValidationError("");

        onRefresh({
            after: includeAll
                ? undefined
                : new Date(after).toISOString(),
            includeAll,
        });
    }
    return (
        <form className="control-card" onSubmit={handleSubmit}>
            <div className="control-card__heading">
                <div>
                    <span className="eyebrow">USGS feed</span>
                    <h2>Refresh earthquake data</h2>
                </div>

                <span className="live-badge">
          <span />
          Live
        </span>
            </div>

            <label>
                Import events after
                <input
                    type="datetime-local"
                    value={after}
                    onChange={(event) => setAfter(event.target.value)}
                    disabled={refreshing || includeAll}
                    required={!includeAll}
                />
            </label>
            <label className="checkbox-row">
                <input
                    type="checkbox"
                    checked={includeAll}
                    onChange={(event) =>
                        setIncludeAll(event.target.checked)
                    }
                    disabled={refreshing}
                />

                Import every valid earthquake in the current USGS feed
            </label>

            <p className="field-help">
                Existing records are replaced by qualifying events from the
                latest USGS feed.
            </p>

            {validationError && (
                <p className="form-error">{validationError}</p>
            )}

            <button type="submit" disabled={refreshing}>
                {refreshing ? "Refreshing…" : "Refresh from USGS"}
            </button>
        </form>
    );
}

export default RefreshControls;