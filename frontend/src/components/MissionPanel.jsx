import React from 'react';
import api from '../services/api';

const MissionPanel = ({ missions, vehicles }) => {
    const handleRTL = async (vid) => {
        try {
            await api.post(`/vehicles/${vid}/rtl`);
            // voice.speak(`${vid} Returning to Launch`);
        } catch (e) {
            console.error(e);
        }
    };

    return (
        <div className="h-full">
            <h2 className="text-sm font-semibold text-slate-500 uppercase tracking-wider mb-3">Active Missions</h2>

            <div className="space-y-3">
                {Object.entries(missions).map(([vid, items]) => {
                    const vehicle = vehicles.find(v => v.id === vid);
                    if (!items || items.length === 0) return null;

                    return (
                        <div key={vid} className="bg-white border border-slate-200 rounded-lg p-4 shadow-soft">
                            <div className="flex justify-between items-center mb-3">
                                <span className="font-bold text-slate-800 uppercase">{vid} MISSION</span>
                                <span className="text-xs font-mono text-slate-400">{items.length} Hops</span>
                            </div>

                            {/* Minimal Progress Bar */}
                            <div className="w-full bg-slate-100 rounded-full h-1.5 mb-4">
                                <div
                                    className="bg-accent-blue h-1.5 rounded-full transition-all duration-500"
                                    style={{ width: '50%' }} // Mock progress
                                ></div>
                            </div>

                            <div className="grid grid-cols-4 gap-2 mb-4">
                                {items.map((step, idx) => (
                                    <div key={idx} className="bg-slate-50 rounded p-1 text-center">
                                        <span className={`text-[10px] font-bold ${step.command === 'TAKEOFF' ? 'text-accent-blue' : 'text-slate-600'}`}>
                                            {step.command === 'TAKEOFF' ? 'TO' : `WP${step.seq}`}
                                        </span>
                                    </div>
                                ))}
                            </div>

                            <div className="flex justify-end">
                                <button
                                    onClick={() => handleRTL(vid)}
                                    className="text-xs font-bold text-accent-red hover:bg-red-50 px-3 py-1 rounded transition-colors"
                                >
                                    ABORT / RTL
                                </button>
                            </div>
                        </div>
                    );
                })}
                {Object.keys(missions).length === 0 && (
                    <div className="text-center py-8 text-slate-400 text-sm">
                        No active missions
                    </div>
                )}
            </div>
        </div>
    );
};

export default MissionPanel;
