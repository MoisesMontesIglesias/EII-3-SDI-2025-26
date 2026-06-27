import { useEffect, useState } from "react";
import "../assets/ITunes.css";
import ITunesHeader from "../components/ITunesHeader";
import FeedbackToast from "../components/FeedbackToast";
import ITunesSearchView from "./ITunesSearchView";
import ITunesLibraryView from "./ITunesLibraryView";
import { getLibrary } from "../services/ITunesService";

const MainPage = ({ onLogout }) => {
    const [view, setView] = useState("search");
    const [toast, setToast] = useState(null);
    const [refreshLibrary, setRefreshLibrary] = useState(0);
    const [importedTrackIds, setImportedTrackIds] = useState([]);

    const showToast = (message, type = "info") => {
        setToast({ message, type });
    };

    const handleImported = (trackId) => {
        if (trackId != null) {
            setImportedTrackIds((current) =>
                current.includes(trackId) ? current : [...current, trackId]
            );
        }
        setRefreshLibrary((value) => value + 1);
        setView("library");
    };

    useEffect(() => {
        const loadImportedTrackIds = async () => {
            try {
                const data = await getLibrary();
                const ids = Array.isArray(data.songs)
                    ? data.songs.map((song) => song.trackId).filter((id) => id != null)
                    : [];
                setImportedTrackIds(ids);
            } catch {
                setImportedTrackIds([]);
            }
        };

        loadImportedTrackIds();
    }, [refreshLibrary]);

    return (
        <div className="itunes-app">
            <ITunesHeader
                currentView={view}
                onChangeView={setView}
                onLogout={onLogout}
            />

            {toast && (
                <FeedbackToast
                    message={toast.message}
                    type={toast.type}
                    onClose={() => setToast(null)}
                />
            )}

            <main className="itunes-app__content">
                {view === "search" && (
                    <ITunesSearchView
                        onFeedback={showToast}
                        onImported={handleImported}
                        importedTrackIds={importedTrackIds}
                    />
                )}

                {view === "library" && (
                    <ITunesLibraryView
                        refreshToken={refreshLibrary}
                        onFeedback={showToast}
                    />
                )}
            </main>
        </div>
    );
};

export default MainPage;
