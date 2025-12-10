import React from 'react';
import api from '../services/api';

const MissionPanel = ({ missions, vehicles }) => {

    const handleRTL = async (vehicleId) => {
        try {
            await api.post(`/vehicles/${vehicleId}/command/rtl`);
        } catch (e) {
            console.error(e);
            alert("Failed to send RTL");
        }
    }

    return (
        <div>
            <h2 className="text-2xl font-black mb-4 border-b-4 border-black inline-block">MISSIONS</h2>

            {Object.entries(missions).map(([vehicleId, items]) => (
                <div key={vehicleId} className="mb-4">
                    <div className="flex justify-between items-center mb-2 bg-black text-white p-1 px-2 border-2 border-white outline outline-2 outline-black">
                        <h3 className="font-bold uppercase tracking-widest">{vehicleId}</h3>
                        <button
                            onClick={() => handleRTL(vehicleId)}
                            className="bg-neo-orange text-black border-2 border-white px-2 text-xs font-bold hover:bg-white hover:border-black transition-colors"
                        >
                            ABORT / RTL
                        </button>
                    </div>

                    {items.length === 0 ? (
                        <div className="text-xs font-mono border-2 border-black p-2 bg-white/50 text-center">IDLE - NO ORDERS</div>
                    ) : (
                        <div className="space-y-1">
                            {items.map(item => (
                                <div key={item.seq} className="flex gap-2 text-xs font-mono border-b border-black/20 pb-1">
                                    <span className="font-bold text-neo-blue">#{item.seq}</span>
                                    <span className="font-bold">{item.command}</span>
                                    {item.command === 'WAYPOINT' && <span className="opacity-70">[{item.lat.toFixed(3)}, {item.lon.toFixed(3)}]</span>}
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            ))}
        </div>
    );
};

export default MissionPanel;
