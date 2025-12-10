import React from 'react';
import api from '../services/api';

const DetectionPanel = ({ detections }) => {

    const handleApprove = async (id) => {
        try {
            await api.post(`/detections/${id}/approve`);
            // Update handled via WebSocket
        } catch (error) {
            console.error("Failed to approve detection:", error);
            alert("Failed to approve detection");
        }
    };

    return (
        <div className="panel">
            <h2>Detections</h2>
            {detections.length === 0 && <p style={{ color: '#666' }}>No detections yet.</p>}
            {detections.map(d => (
                <div key={d.id} className="detection-item">
                    <img src={d.imageUrl} alt="Detection" className="detection-img" />
                    <div className="detection-info">
                        <div><strong>Vehicle:</strong> {d.vehicleId}</div>
                        <div><strong>Conf:</strong> {(d.confidence * 100).toFixed(1)}%</div>
                        {d.approved ? (
                            <div className="approved-badge">APPROVED</div>
                        ) : (
                            <button
                                className="approve-btn"
                                onClick={() => handleApprove(d.id)}
                            >
                                APPROVE & GENERATE
                            </button>
                        )}
                    </div>
                </div>
            ))}
        </div>
    );
};

export default DetectionPanel;
