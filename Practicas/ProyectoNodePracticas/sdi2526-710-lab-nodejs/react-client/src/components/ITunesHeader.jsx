const ITunesHeader = ({ currentView, onChangeView, onLogout }) => {
    return (
        <header className="itunes-header">
            <div>
                <p className="itunes-header__eyebrow">React client</p>
                <h1>iTunes browser</h1>
                <p className="itunes-header__subtitle">
                    Search songs, import them and manage your library.
                </p>
            </div>

            <nav className="itunes-header__nav">
                <button
                    type="button"
                    className={currentView === "search" ? "is-active" : ""}
                    onClick={() => onChangeView("search")}
                >
                    Search
                </button>
                <button
                    type="button"
                    className={currentView === "library" ? "is-active" : ""}
                    onClick={() => onChangeView("library")}
                >
                    Library
                </button>
                <button type="button" className="is-ghost" onClick={onLogout}>
                    Logout
                </button>
            </nav>
        </header>
    );
};

export default ITunesHeader;
