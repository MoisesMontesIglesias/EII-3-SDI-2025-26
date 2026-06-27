import { useEffect, useState } from "react";

const ITunesLibraryCard = ({ song, onSaveNote, onDelete, saving, deleting }) => {
    const [note, setNote] = useState(song.note || "");

    useEffect(() => {
        setNote(song.note || "");
    }, [song.note]);

    const songId = song.id || song._id;
    const imageUrl = song.artworkUrl || "https://via.placeholder.com/100";

    return (
        <article className="itunes-library-card">
            <img src={imageUrl} alt={song.title} className="itunes-library-card__image" />

            <div className="itunes-library-card__body">
                <h3>{song.title}</h3>
                <p>{song.artist}</p>
                <p>{song.kind}</p>
                <p className="itunes-library-card__price">
                    {song.price} {song.currency || "EUR"}
                </p>

                <label className="itunes-library-card__label" htmlFor={`note-${songId}`}>
                    Note
                </label>
                <textarea
                    id={`note-${songId}`}
                    value={note}
                    onChange={(e) => setNote(e.target.value)}
                    placeholder="Add a note for this song"
                />

                <div className="itunes-library-card__actions">
                    <button
                        type="button"
                        onClick={() => onSaveNote(songId, note)}
                        disabled={saving || note === (song.note || "")}
                    >
                        {saving ? "Saving..." : "Save note"}
                    </button>
                    <button
                        type="button"
                        className="is-danger"
                        onClick={() => onDelete(song)}
                        disabled={deleting}
                    >
                        {deleting ? "Deleting..." : "Delete"}
                    </button>
                </div>
            </div>
        </article>
    );
};

export default ITunesLibraryCard;
