import { useState } from "react";
import { importTrack, searchTracks } from "../services/ITunesService";
import ITunesSearchForm from "../components/ITunesSearchForm";
import ITunesTrackCard from "../components/ITunesTrackCard";

const ITunesSearchView = ({ onFeedback, onImported, importedTrackIds = [] }) => {
    const [results, setResults] = useState([]);
    const [loading, setLoading] = useState(false);
    const [importingId, setImportingId] = useState(null);
    const [lastTerm, setLastTerm] = useState("");

    const handleSearch = async (term) => {
        const trimmed = term.trim();

        if (!trimmed) {
            onFeedback("Introduce a search term", "warning");
            return;
        }

        setLoading(true);
        setLastTerm(trimmed);

        try {
            const data = await searchTracks(trimmed);
            setResults(Array.isArray(data.results) ? data.results : []);

            if (!Array.isArray(data.results) || data.results.length === 0) {
                onFeedback("No results found", "info");
            } else {
                onFeedback(`Found ${data.results.length} results for ${trimmed}`, "success");
            }
        } catch (err) {
            setResults([]);
            onFeedback(err.message, "error");
        } finally {
            setLoading(false);
        }
    };

    const handleImport = async (track) => {
        setImportingId(track.trackId);

        try {
            await importTrack(track);
            onFeedback(`Imported ${track.trackName}`, "success");

            if (onImported) {
                onImported(track.trackId);
            }
        } catch (err) {
            onFeedback(err.message, "error");
        } finally {
            setImportingId(null);
        }
    };

    return (
        <section className="itunes-panel">
            <div className="itunes-panel__header">
                <h2>Search iTunes</h2>
                <p>Find songs and import them into your personal library.</p>
            </div>

            <ITunesSearchForm onSearch={handleSearch} loading={loading} />

            {loading && <p className="itunes-empty-state">Searching songs...</p>}

            {!loading && results.length === 0 && lastTerm && (
                <p className="itunes-empty-state">No results for "{lastTerm}"</p>
            )}

            <div className="itunes-grid">
                {results.map((track) => (
                    <ITunesTrackCard
                        key={track.trackId}
                        track={track}
                        onImport={handleImport}
                        importing={importingId === track.trackId}
                        imported={importedTrackIds.includes(track.trackId)}
                    />
                ))}
            </div>
        </section>
    );
};

export default ITunesSearchView;
