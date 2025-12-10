import React from 'react';
import api from '../services/api';

const MissionPanel = ({ missions, vehicles }) => {

    const handleRTL = async (vehicleId) => {
        try {
            await api.post(`/vehicles/${vehicleId}/command/rtl`);
        } catch (e) {
            console.error(e);
            alert("Failed to send RTL");
        }
    }

    return (
        <div className="panel">
            <h2>Active Missions</h2>
            {Object.entries(missions).map(([vehicleId, items]) => (
                <div key={vehicleId} style={{ marginBottom: '15px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <h3 style={{ margin: '0 0 5px 0', fontSize: '1rem' }}>{vehicleId.toUpperCase()}</h3>
                        <button
                            onClick={() => handleRTL(vehicleId)}
                            style={{ backgroundColor: 'orange', border: 'none', borderRadius: '3px', padding: '2px 5px', cursor: 'pointer', fontWeight: 'bold' }}
                        >
                            RTL
                        </button>
                    </div>

                    {items.length === 0 ? (
                        <div style={{ color: '#666', fontStyle: 'italic' }}>No mission</div>
                    ) : (
                        <ul className="mission-list">
                            {items.map(item => (
                                <li key={item.seq} className="mission-step">
                                    <span className="step-seq">{item.seq}</span>
                                    {item.command}
                                    {item.command === 'WAYPOINT' && ` -> [${item.lat.toFixed(5)}, ${item.lon.toFixed(5)}]`}
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            ))}
        </div>
    );
};

export default MissionPanel;
