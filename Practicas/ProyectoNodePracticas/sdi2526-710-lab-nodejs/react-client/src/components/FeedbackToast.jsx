const FeedbackToast = ({ message, type = "info", onClose }) => {
    if (!message) {
        return null;
    }

    return (
        <div className={`feedback-toast feedback-toast--${type}`} role="alert" aria-live="polite">
            <span className="feedback-toast__message">{message}</span>
            <button type="button" className="feedback-toast__close" onClick={onClose}>
                Close
            </button>
        </div>
    );
};

export default FeedbackToast;
