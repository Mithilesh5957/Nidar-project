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
            } catch (err) {
                console.error("Initial fetch failed", err);
            }
        };

        fetchData();

        // 2. Connect WebSocket
        gcsWebSocket.connect(() => {
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

                gcsWebSocket.subscribe(`/topic/missions/${vid}`, (data) => {
                    setMissions(prev => ({
                        ...prev,
                        [vid]: data
                    }));
                });
            });

            gcsWebSocket.subscribe('/topic/detections', (data) => {
                setDetections(prev => {
                    if (Array.isArray(data)) return data;
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
        <div className="min-h-screen bg-neo-bg p-4 font-display">
            <div className="flex flex-col h-[95vh] gap-4">

                {/* Header */}
                <header className="bg-neo-yellow border-4 border-black shadow-neo rounded-none p-4 flex justify-between items-center">
                    <h1 className="text-4xl font-black uppercase tracking-tighter italic">
                        NIDAR <span className="text-neo-pink">GCS</span>
                    </h1>
                    <div className="flex gap-2">
                        <div className="px-4 py-2 bg-white border-2 border-black font-bold shadow-neo-sm animate-pulse">
                            LIVE SYSTEM
                        </div>
                        <div className="px-4 py-2 bg-neo-blue border-2 border-black font-bold shadow-neo-sm">
                            v1.0.0
                        </div>
                    </div>
                </header>

                {/* Main Grid */}
                <div className="flex-1 grid grid-cols-12 gap-4 overflow-hidden">

                    {/* Left Sidebar - Panels */}
                    <div className="col-span-3 flex flex-col gap-4 overflow-y-auto pr-2 pb-2">
                        <div className="bg-white border-4 border-black shadow-neo p-4">
                            <TelemetryPanel vehicles={vehicles} />
                        </div>

                        <div className="bg-neo-pink border-4 border-black shadow-neo p-4">
                            <MissionPanel missions={missions} vehicles={vehicles} />
                        </div>

                        <div className="bg-neo-blue border-4 border-black shadow-neo p-4">
                            <DetectionPanel detections={detections} />
                        </div>
                    </div>

                    {/* Right Content - Map */}
                    <div className="col-span-9 relative border-4 border-black shadow-neo bg-gray-200">
                        <div className="absolute top-0 left-0 z-[1000] m-4 bg-white border-2 border-black p-2 font-bold shadow-neo-sm">
                            TACTICAL MAP
                        </div>
                        <MapView vehicles={vehicles} missions={missions} />
                    </div>

                </div>
            </div>
        </div>
    );
}

export default App;
