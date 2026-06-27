import { useState } from "react";

const ITunesSearchForm = ({ onSearch, loading }) => {
    const [term, setTerm] = useState("");

    const handleSubmit = (e) => {
        e.preventDefault();
        onSearch(term);
    };

    return (
        <form className="itunes-search-form" onSubmit={handleSubmit}>
            <input
                type="text"
                value={term}
                placeholder="Search by title or artist"
                onChange={(e) => setTerm(e.target.value)}
            />
            <button type="submit" disabled={!term.trim() || loading}>
                {loading ? "Searching..." : "Search"}
            </button>
        </form>
    );
};

export default ITunesSearchForm;
