import React from 'react';

const TelemetryPanel = ({ vehicles }) => {
    return (
        <div className="panel">
            <h2>Telemetry</h2>
            {vehicles.map(v => (
                <div key={v.id} style={{ marginBottom: '15px' }}>
                    <h3 style={{ margin: '0 0 5px 0', fontSize: '1rem' }}>{v.id.toUpperCase()}</h3>
                    <div className="telemetry-grid">
                        <div className="telemetry-item">
                            <label>Status</label>
                            <span className={`status-badge status-${v.status}`}>{v.status}</span>
                        </div>
                        <div className="telemetry-item">
                            <label>Battery</label>
                            <span>{v.battery}%</span>
                        </div>
                        <div className="telemetry-item">
                            <label>Alt</label>
                            <span>{v.alt.toFixed(1)}m</span>
                        </div>
                        <div className="telemetry-item">
                            <label>Speed</label>
                            <span>0.0 m/s</span> {/* speed placeholder */}
                        </div>
                        <div className="telemetry-item">
                            <label>Lat</label>
                            <span>{v.lat.toFixed(6)}</span>
                        </div>
                        <div className="telemetry-item">
                            <label>Lon</label>
                            <span>{v.lon.toFixed(6)}</span>
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default TelemetryPanel;
