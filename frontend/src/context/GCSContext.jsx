import React, { createContext, useContext, useState, useEffect } from 'react';
import gcsWebSocket from '../services/websocket';
import api from '../services/api';
const GCSContext = createContext(null);

export const GCSProvider = ({ children }) => {
    const [vehicles, setVehicles] = useState([]);
    const [detections, setDetections] = useState([]);
    const [missions, setMissions] = useState({});
    // const [audioEnabled, setAudioEnabled] = useState(true);

    useEffect(() => {
        // Initial Fetch
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

        // WebSocket Setup
        gcsWebSocket.connect(() => {
            ['scout', 'delivery'].forEach(vid => {
                gcsWebSocket.subscribe(`/topic/telemetry/${vid}`, (data) => {
                    setVehicles(prev => {
                        const idx = prev.findIndex(v => v.id === data.id);
                        if (idx >= 0) {
                            // if (prev[idx].battery > 20 && data.battery <= 20) {
                            //     voice.speak(`${data.id} battery low`);
                            // }
                            const newV = [...prev];
                            newV[idx] = data;
                            return newV;
                        }
                        return [...prev, data];
                    });
                });

                gcsWebSocket.subscribe(`/topic/missions/${vid}`, (data) => {
                    setMissions(prev => ({ ...prev, [vid]: data }));
                    // voice.speak(`New mission uploaded for ${vid}`);
                });
            });

            gcsWebSocket.subscribe('/topic/detections', (data) => {
                setDetections(prev => {
                    if (Array.isArray(data)) return data;
                    const idx = prev.findIndex(d => d.id === data.id);
                    if (idx === -1) {
                        // voice.speak("Target Detected");
                        return [...prev, data];
                    }
                    if (prev[idx].approved === false && data.approved === true) {
                        // voice.speak("Detection Approved");
                    }
                    const newD = [...prev];
                    newD[idx] = data;
                    return newD;
                });
            });
        });
    }, []);

    return (
        <GCSContext.Provider value={{ vehicles, detections, missions }}>
            {children}
        </GCSContext.Provider>
    );
};

export const useGCS = () => useContext(GCSContext);
