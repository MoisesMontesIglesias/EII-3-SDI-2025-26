const ITunesTrackCard = ({ track, onImport, importing, imported }) => {
    const imageUrl = track.artworkUrl100 || "https://via.placeholder.com/100";
    const buttonLabel = imported ? "Imported" : importing ? "Importing..." : "Import";

    return (
        <article className="itunes-track-card">
            <img src={imageUrl} alt={track.trackName} className="itunes-track-card__image" />

            <div className="itunes-track-card__body">
                <h3>{track.trackName}</h3>
                <p>{track.artistName}</p>
                <p>{track.primaryGenreName}</p>
                <p className="itunes-track-card__price">
                    {typeof track.trackPrice === "number"
                        ? `${track.trackPrice} ${track.currency || "USD"}`
                        : "Price not available"}
                </p>

                {track.previewUrl && (
                    <a href={track.previewUrl} target="_blank" rel="noreferrer">
                        Preview
                    </a>
                )}

                <button
                    type="button"
                    className={imported ? "is-imported" : ""}
                    onClick={() => onImport(track)}
                    disabled={importing || imported}
                >
                    {buttonLabel}
                </button>
            </div>
        </article>
    );
};

export default ITunesTrackCard;
