const API_URL = "/api/earthquake";

async function errorMessage(response) {
    const body = await response.text();

    if (!body) {
        return `Request failed with status ${response.status}`;
    }

    try {
        const error = JSON.parse(body);
        return error.message ?? body;
    } catch {
        return body;
    }
}

async function requestJson(url, options = {}) {
    const response = await fetch(url, options);

    if (!response.ok) {
        throw new Error(await errorMessage(response));
    }

    return response.json();
}

export async function getEarthquakes(filters = {}) {
    const parameters = new URLSearchParams();

    if (filters.minMagnitude !== undefined) {
        parameters.set(
            "minMagnitude",
            filters.minMagnitude.toString(),
        );
    }

    if (filters.after) {
        parameters.set("after", filters.after);
    }

    const query = parameters.toString();

    return requestJson(
        `${API_URL}/${query ? `?${query}` : ""}`,
    );
}

export async function refreshEarthquakes({after, includeAll = false,}) {
    const parameters = new URLSearchParams({
        includeAll: includeAll.toString(),
    });

    if (after) {
        parameters.set("after", after);
    }

    return requestJson(
        `${API_URL}/refresh?${parameters.toString()}`,
        { method: "POST" },
    );
}

export async function deleteEarthquake(id) {
    const response = await fetch(`${API_URL}/${id}`, {
        method: "DELETE",
    });

    if (!response.ok) {
        throw new Error(await errorMessage(response));
    }
}