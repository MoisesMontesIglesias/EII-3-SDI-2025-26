const DeleteConfirmationModal = ({ isOpen, songName, onConfirm, onClose }) => {
    if (!isOpen || !songName.trim()) {
        return null;
    }

    return (
        <div className="delete-modal__backdrop" role="presentation">
            <div
                className="delete-modal"
                role="dialog"
                aria-modal="true"
                aria-labelledby="delete-modal-title"
            >
                <h3 id="delete-modal-title">Confirm delete</h3>
                <p>
                    You are about to delete <strong>{songName}</strong>.
                    This action cannot be undone.
                </p>

                <div className="delete-modal__actions">
                    <button type="button" className="delete-modal__cancel" onClick={onClose}>
                        Cancel
                    </button>
                    <button
                        type="button"
                        className="delete-modal__confirm"
                        onClick={onConfirm}
                        disabled={!songName.trim()}
                    >
                        Delete
                    </button>
                </div>
            </div>
        </div>
    );
};

export default DeleteConfirmationModal;
