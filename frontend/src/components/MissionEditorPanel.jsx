import React, { useState } from 'react';
import api from '../services/api';

const MissionEditorPanel = ({ plannedWaypoints, setPlannedWaypoints, onUpload }) => {
    const [targetVehicle, setTargetVehicle] = useState('delivery');
    const [defaultAlt, setDefaultAlt] = useState(30);

    const clearMission = () => {
        setPlannedWaypoints([]);
    };

    const removeWaypoint = (index) => {
        setPlannedWaypoints(prev => prev.filter((_, i) => i !== index));
    };

    const handleUpload = async () => {
        if (plannedWaypoints.length === 0) {
            alert("No waypoints to upload!");
            return;
        }

        // MAVLink command IDs
        const MAV_CMD_NAV_WAYPOINT = 16;
        const MAV_CMD_NAV_TAKEOFF = 22;
        const MAV_CMD_NAV_LAND = 21;

        const missionItems = plannedWaypoints.map((wp, index) => {
            const isFirst = index === 0;
            const isLast = index === plannedWaypoints.length - 1;

            return {
                seq: index,
                frame: 3, // MAV_FRAME_GLOBAL_RELATIVE_ALT
                command: isFirst ? MAV_CMD_NAV_TAKEOFF : (isLast ? MAV_CMD_NAV_LAND : MAV_CMD_NAV_WAYPOINT),
                current: index === 0 ? 1 : 0,
                autocontinue: 1,
                param1: isFirst ? 15 : 0, // Min pitch for takeoff (degrees)
                param2: 0,
                param3: 0,
                param4: 0, // Yaw
                x: wp.lat,  // Latitude
                y: wp.lon,  // Longitude  
                z: defaultAlt // Altitude
            };
        });

        try {
            await api.post(`/vehicles/${targetVehicle}/mission-upload`, missionItems);
            alert(`✅ Mission uploaded successfully! ${missionItems.length} waypoints`);
            if (onUpload) onUpload();
        } catch (e) {
            console.error('Mission upload error:', e);
            const errorMsg = e.response?.data?.message || e.message || "Unknown error";
            alert(`❌ Failed to upload mission: ${errorMsg}`);
        }
    };

    return (
        <div className="h-full bg-slate-50 border border-slate-200 rounded-lg overflow-hidden flex flex-col">
            <div className="bg-white border-b border-slate-200 p-4">
                <h2 className="text-lg font-bold text-slate-800">Mission Planner</h2>
                <p className="text-xs text-slate-500 mt-1">Click map to add waypoints</p>
            </div>

            <div className="p-4 space-y-4 flex-1 overflow-y-auto">
                <div>
                    <label className="text-xs font-bold text-slate-500 uppercase block mb-1">Target Drone</label>
                    <select
                        value={targetVehicle}
                        onChange={(e) => setTargetVehicle(e.target.value)}
                        className="w-full bg-white border border-slate-300 rounded p-2 text-sm text-slate-700 focus:border-accent-blue focus:ring-1 focus:ring-accent-blue outline-none"
                    >
                        <option value="delivery">DELIVERY</option>
                        <option value="scout">SCOUT</option>
                    </select>
                </div>
                <div>
                    <label className="text-xs font-bold text-slate-500 uppercase block mb-1">Default Alt (m)</label>
                    <input
                        type="number"
                        value={defaultAlt}
                        onChange={(e) => setDefaultAlt(Number(e.target.value))}
                        className="w-full bg-white border border-slate-300 rounded p-2 text-sm text-slate-700 focus:border-accent-blue focus:ring-1 focus:ring-accent-blue outline-none"
                    />
                </div>

                <div className="mt-4">
                    <h3 className="text-xs font-bold text-slate-500 uppercase mb-2">Waypoints ({plannedWaypoints.length})</h3>
                    {plannedWaypoints.length === 0 ? (
                        <div className="text-center py-6 border-2 border-dashed border-slate-200 rounded text-slate-400 text-sm">
                            Map is empty
                        </div>
                    ) : (
                        <div className="space-y-1">
                            {plannedWaypoints.map((wp, idx) => (
                                <div key={idx} className="flex justify-between items-center bg-white border border-slate-200 rounded p-2 text-xs">
                                    <span className="font-mono text-slate-600">
                                        <b className="text-slate-800 mr-2">#{idx}</b>
                                        {wp.lat.toFixed(4)}, {wp.lon.toFixed(4)}
                                    </span>
                                    <button
                                        onClick={() => removeWaypoint(idx)}
                                        className="text-slate-400 hover:text-accent-red font-bold px-2"
                                    >
                                        ×
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>

            <div className="p-4 bg-white border-t border-slate-200 flex gap-3">
                <button
                    onClick={clearMission}
                    className="flex-1 py-2 px-4 rounded text-slate-600 font-bold text-sm hover:bg-slate-100 transition-colors"
                >
                    Clear
                </button>
                <button
                    onClick={handleUpload}
                    className="flex-1 py-2 px-4 rounded bg-accent-blue text-white font-bold text-sm hover:bg-blue-700 transition-colors shadow-sm"
                >
                    Upload
                </button>
            </div>
        </div>
    );
};

export default MissionEditorPanel;
