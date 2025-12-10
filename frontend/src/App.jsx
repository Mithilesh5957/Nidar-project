import React, { useEffect, useState } from 'react';
import gcsWebSocket from './services/websocket';
import api from './services/api';
import MapView from './components/MapView';
import TelemetryPanel from './components/TelemetryPanel';
import MissionPanel from './components/MissionPanel';
import DetectionPanel from './components/DetectionPanel';
import './index.css';

function App() {
    const [vehicles, setVehicles] = useState([]);
    const [detections, setDetections] = useState([]);
    const [missions, setMissions] = useState({});

    useEffect(() => {
        // 1. Initial Data Fetch
        const fetchData = async () => {
            try {
                const vRes = await api.get('/vehicles');
                setVehicles(vRes.data);

                const dRes = await api.get('/detections');
                setDetections(dRes.data);

                // Fetch missions for each vehicle
                // For prototype, we might skip this or do it loop
                // vRes.data.forEach(async v => { ... })
            } catch (err) {
                console.error("Initial fetch failed", err);
            }
        };

        fetchData();

        // 2. Connect WebSocket
        gcsWebSocket.connect(() => {
            // Subscribe to Veh Telemetry
            ['scout', 'delivery'].forEach(vid => {
                gcsWebSocket.subscribe(`/topic/telemetry/${vid}`, (data) => {
                    setVehicles(prev => {
                        const idx = prev.findIndex(v => v.id === data.id);
                        if (idx >= 0) {
                            const newV = [...prev];
                            newV[idx] = data;
                            return newV;
                        }
                        return [...prev, data];
                    });
                });

                // Subscribe to Missions
                gcsWebSocket.subscribe(`/topic/missions/${vid}`, (data) => {
                    setMissions(prev => ({
                        ...prev,
                        [vid]: data
                    }));
                });
            });

            // Subscribe to Detections
            gcsWebSocket.subscribe('/topic/detections', (data) => {
                setDetections(prev => {
                    // If array (full list update)
                    if (Array.isArray(data)) return data;

                    // If single item (new detection or update)
                    const idx = prev.findIndex(d => d.id === data.id);
                    if (idx >= 0) {
                        const newD = [...prev];
                        newD[idx] = data;
                        return newD;
                    }
                    return [...prev, data];
                });
            });
        });

    }, []);

    return (
        <div className="app-container">
            <div className="sidebar">
                <h1 style={{ textAlign: 'center', color: '#007acc', margin: '10px 0' }}>NIDAR GCS</h1>
                <TelemetryPanel vehicles={vehicles} />
                <MissionPanel missions={missions} vehicles={vehicles} />
                <DetectionPanel detections={detections} />
            </div>
            <div className="main-content">
                <MapView vehicles={vehicles} missions={missions} />
            </div>
        </div>
    );
}

export default App;
