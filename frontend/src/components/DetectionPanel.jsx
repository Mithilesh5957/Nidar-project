import React from 'react';
import api from '../services/api';

const DetectionPanel = ({ detections }) => {
    const handleApprove = async (id) => {
        try {
            await api.post(`/detections/${id}/approve`);
        } catch (e) {
            console.error(e);
        }
    };

    return (
        <div className="h-full">
            <h2 className="text-sm font-semibold text-slate-500 uppercase tracking-wider mb-3">Recent Detections</h2>
            <div className="flex flex-col gap-3 h-[300px] overflow-y-auto">
                {detections.length === 0 && (
                    <div className="text-center py-8 text-slate-400 text-sm">No targets detected</div>
                )}
                {detections.map(d => (
                    <div key={d.id} className="bg-white border border-slate-200 rounded-lg overflow-hidden shadow-soft flex">
                        <div className="w-24 h-24 bg-slate-100 relative shrink-0">
                            <img src={d.imageUrl} alt="Detection" className="w-full h-full object-cover mix-blend-multiply" />
                        </div>
                        <div className="p-3 flex-1 flex flex-col justify-between">
                            <div>
                                <div className="flex justify-between items-center">
                                    <span className="text-xs font-bold text-slate-500 uppercase">{new Date(d.timestamp).toLocaleTimeString()}</span>
                                    <span className="text-xs font-bold text-accent-blue">{Math.round(d.confidence)}% CONF</span>
                                </div>
                                <h4 className="font-bold text-slate-800 text-sm mt-1">HUMAN DETECTED</h4>
                            </div>

                            {d.approved ? (
                                <div className="text-xs font-bold text-green-600 flex items-center gap-1">
                                    <span>✓ ENGAGED</span>
                                </div>
                            ) : (
                                <button
                                    onClick={() => handleApprove(d.id)}
                                    className="w-full bg-slate-900 text-white text-xs font-bold py-2 rounded hover:bg-slate-800 transition-colors"
                                >
                                    ENGAGE TARGET
                                </button>
                            )}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default DetectionPanel;
