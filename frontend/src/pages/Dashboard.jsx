import React from 'react';
import { useGCS } from '../context/GCSContext';
import MapView from '../components/MapView';
import TelemetryPanel from '../components/TelemetryPanel';
import MissionPanel from '../components/MissionPanel';
import AnalyticsPanel from '../components/AnalyticsPanel';

const Dashboard = () => {
    const { vehicles, missions } = useGCS();

    return (
        <div className="grid grid-cols-12 gap-6 h-[85vh] min-h-[600px]">
            {/* Left Sidebar - Quick Stats */}
            <div className="col-span-3 flex flex-col gap-6 h-full overflow-y-auto pr-1">
                <div className="bg-white rounded-xl shadow-soft border border-slate-200 p-1">
                    <TelemetryPanel vehicles={vehicles} />
                </div>
                <div className="bg-white rounded-xl shadow-soft border border-slate-200 p-1">
                    <MissionPanel missions={missions} vehicles={vehicles} />
                </div>
                <AnalyticsPanel vehicles={vehicles} />
            </div>

            {/* Main Map */}
            <div className="col-span-9 bg-white rounded-2xl shadow-soft border border-slate-200 overflow-hidden relative">
                <div className="absolute top-4 left-4 z-[400] bg-white/90 backdrop-blur px-3 py-1.5 rounded-lg border border-slate-200 shadow-sm">
                    <span className="text-xs font-bold text-slate-500 uppercase">Live Operations Map</span>
                </div>
                <MapView vehicles={vehicles} missions={missions} />
            </div>
        </div>
    );
};

export default Dashboard;
