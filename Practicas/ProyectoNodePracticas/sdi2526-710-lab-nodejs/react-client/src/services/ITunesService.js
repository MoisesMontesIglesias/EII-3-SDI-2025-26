const API_BASE = "http://localhost:8081/api/v1.0";

async function request(path, options = {}) {
    const response = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            token: localStorage.getItem("token"),
            ...(options.headers || {})
        }
    });

    const rawBody = await response.text();
    let data = {};

    if (rawBody) {
        try {
            data = JSON.parse(rawBody);
        } catch {
            data = {};
        }
    }

    if (!response.ok) {
        if (response.status === 401) {
            localStorage.removeItem("token");
            throw new Error("UNAUTHORIZED");
        }

        throw new Error(data.error || data.message || "Error en la API");
    }

    return data;
}

export function login(email, password) {
    return request("/users/login", {
        method: "POST",
        body: JSON.stringify({ email, password })
    });
}

export function searchTracks(term) {
    return request(`/itunes/search?term=${encodeURIComponent(term)}`);
}

export function getLibrary() {
    return request("/itunes/library");
}

export function importTrack(track) {
    return request("/itunes/import", {
        method: "POST",
        body: JSON.stringify({
            trackId: track.trackId,
            priceEur: typeof track.trackPrice === "number" ? track.trackPrice : 0
        })
    });
}

export function updateTrackNote(id, note) {
    return request(`/itunes/${id}`, {
        method: "PUT",
        body: JSON.stringify({ note })
    });
}

export function deleteTrack(id) {
    return request(`/itunes/${id}`, {
        method: "DELETE"
    });
}
