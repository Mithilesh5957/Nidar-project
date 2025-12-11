import React, { useState } from 'react';
import { useGCS } from '../context/GCSContext';
import MapView from '../components/MapView';
import MissionEditorPanel from '../components/MissionEditorPanel';

const Planner = () => {
    const { vehicles, missions } = useGCS();
    const [plannedWaypoints, setPlannedWaypoints] = useState([]);

    const handleMapClick = (latlng) => {
        setPlannedWaypoints(prev => [...prev, { lat: latlng.lat, lon: latlng.lng }]);
    };

    const handleUpload = () => {
        setPlannedWaypoints([]);
    };

    return (
        <div className="grid grid-cols-12 gap-6 h-full">
            <div className="col-span-3 h-full">
                <MissionEditorPanel
                    plannedWaypoints={plannedWaypoints}
                    setPlannedWaypoints={setPlannedWaypoints}
                    onUpload={handleUpload}
                />
            </div>

            <div className="col-span-9 bg-white rounded-2xl shadow-soft border border-slate-200 overflow-hidden relative">
                <div className="absolute top-4 left-4 z-[400] bg-accent-blue/10 backdrop-blur px-3 py-1.5 rounded-lg border border-accent-blue/20 shadow-sm">
                    <span className="text-xs font-bold text-accent-blue uppercase">Planning Mode Active</span>
                </div>
                <MapView
                    vehicles={vehicles}
                    missions={missions}
                    plannedWaypoints={plannedWaypoints}
                    onMapClick={handleMapClick}
                />
            </div>
        </div>
    );
};

export default Planner;
