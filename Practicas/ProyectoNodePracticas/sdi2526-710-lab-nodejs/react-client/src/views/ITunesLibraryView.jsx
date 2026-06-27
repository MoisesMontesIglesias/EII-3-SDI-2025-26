import { useEffect, useState } from "react";
import { deleteTrack, getLibrary, updateTrackNote } from "../services/ITunesService";
import ITunesLibraryCard from "../components/ITunesLibraryCard";
import DeleteConfirmationModal from "../components/DeleteConfirmationModal";

const ITunesLibraryView = ({ refreshToken, onFeedback }) => {
    const [songs, setSongs] = useState([]);
    const [loading, setLoading] = useState(false);
    const [savingId, setSavingId] = useState(null);
    const [deletingId, setDeletingId] = useState(null);
    const [selectedSong, setSelectedSong] = useState(null);
    const [confirmOpen, setConfirmOpen] = useState(false);

    useEffect(() => {
        let active = true;

        const loadLibrary = async () => {
            setLoading(true);

            try {
                const data = await getLibrary();

                if (!active) {
                    return;
                }

                setSongs(Array.isArray(data.songs) ? data.songs : []);

                if (!Array.isArray(data.songs) || data.songs.length === 0) {
                    onFeedback("Your library is empty", "info");
                }
            } catch (err) {
                if (active) {
                    setSongs([]);
                    onFeedback(err.message, "error");
                }
            } finally {
                if (active) {
                    setLoading(false);
                }
            }
        };

        loadLibrary();

        return () => {
            active = false;
        };
    }, [refreshToken, onFeedback]);

    const handleSaveNote = async (songId, note) => {
        setSavingId(songId);

        try {
            await updateTrackNote(songId, note);
            onFeedback("Note updated", "success");
            const data = await getLibrary();
            setSongs(Array.isArray(data.songs) ? data.songs : []);
        } catch (err) {
            onFeedback(err.message, "error");
        } finally {
            setSavingId(null);
        }
    };

    const openDeleteModal = (song) => {
        setSelectedSong(song);
        setConfirmOpen(true);
    };

    const handleDelete = async () => {
        if (!selectedSong) {
            return;
        }

        setDeletingId(selectedSong.id);

        try {
            await deleteTrack(selectedSong.id);
            onFeedback(`Deleted ${selectedSong.title}`, "success");
            setConfirmOpen(false);
            setSelectedSong(null);
            const data = await getLibrary();
            setSongs(Array.isArray(data.songs) ? data.songs : []);
        } catch (err) {
            setConfirmOpen(false);
            onFeedback(err.message, "error");
        } finally {
            setDeletingId(null);
        }
    };
//
    return (
        <section className="itunes-panel">
            <div className="itunes-panel__header">
                <h2>My library</h2>
                <p>Manage imported songs, notes and removals.</p>
            </div>

            {loading && <p className="itunes-empty-state">Loading library...</p>}

            {!loading && songs.length === 0 && (
                <p className="itunes-empty-state">No imported songs yet</p>
            )}

            <div className="itunes-grid">
                {songs.map((song) => (
                    <ITunesLibraryCard
                        key={song.id}
                        song={song}
                        onSaveNote={handleSaveNote}
                        onDelete={openDeleteModal}
                        saving={savingId === song.id}
                        deleting={deletingId === song.id}
                    />
                ))}
            </div>

            <DeleteConfirmationModal
                isOpen={confirmOpen}
                songName={selectedSong?.title || ""}
                onConfirm={handleDelete}
                onClose={() => {
                    setConfirmOpen(false);
                    setSelectedSong(null);
                }}
            />
        </section>
    );
};

export default ITunesLibraryView;
