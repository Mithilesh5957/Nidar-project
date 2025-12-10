import React from 'react';
import api from '../services/api';

const DetectionPanel = ({ detections }) => {

    const handleApprove = async (id) => {
        try {
            await api.post(`/detections/${id}/approve`);
        } catch (error) {
            console.error("Failed to approve detection:", error);
            alert("Failed to approve detection");
        }
    };

    return (
        <div>
            <h2 className="text-2xl font-black mb-4 border-b-4 border-black inline-block text-white bg-black px-2">DETECTIONS</h2>

            {detections.length === 0 && <div className="text-black font-bold italic opacity-50">SCANNING AREA...</div>}

            <div className="space-y-4">
                {detections.map(d => (
                    <div key={d.id} className="bg-white border-2 border-black p-2 shadow-neo-sm transform transition hover:-translate-y-1">
                        <div className="relative border-2 border-black mb-2">
                            <img src={d.imageUrl} alt="Detection" className="w-full h-32 object-cover grayscale contrast-125 hover:grayscale-0 transition-all" />
                            <div className="absolute top-0 right-0 bg-neo-yellow border-l-2 border-b-2 border-black px-2 font-bold text-xs">
                                {(d.confidence * 100).toFixed(0)}%
                            </div>
                        </div>

                        <div className="flex justify-between items-end">
                            <div className="text-xs font-mono font-bold leading-tight">
                                {d.vehicleId.toUpperCase()}<br />
                                LAT: {d.lat.toFixed(4)}<br />
                                LON: {d.lon.toFixed(4)}
                            </div>

                            {d.approved ? (
                                <div className="bg-neo-green text-white border-2 border-black px-2 py-1 font-black text-sm rotate-[-5deg]">
                                    APPROVED
                                </div>
                            ) : (
                                <button
                                    className="bg-neo-lime hover:bg-neo-green active:translate-y-1 active:shadow-none border-2 border-black shadow-[2px_2px_0_0_rgba(0,0,0,1)] px-3 py-1 font-black text-sm uppercase transition-all"
                                    onClick={() => handleApprove(d.id)}
                                >
                                    ENGAGE
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
