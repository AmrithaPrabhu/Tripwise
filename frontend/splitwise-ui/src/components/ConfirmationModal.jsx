// components/ConfirmationModal.jsx
import "./ConfirmationModal.css";

const ConfirmationModal = ({
  title,
  message,
  confirmText = "Confirm",
  cancelText = "Cancel",
  onConfirm,
  onCancel,
  loading = false
}) => {
  return (
    <div className="modal-overlay">
      <div className="modal-card">

        <h3 className="modal-title">{title}</h3>

        <p className="modal-message">{message}</p>

        <div className="modal-actions">
          <button
            className="cancel-btn"
            onClick={onCancel}
            disabled={loading}
          >
            {cancelText}
          </button>

          <button
            className="danger-btn"
            onClick={onConfirm}
            disabled={loading}
          >
            {loading ? "Deleting..." : confirmText}
          </button>
        </div>

      </div>
    </div>
  );
};

export default ConfirmationModal;
