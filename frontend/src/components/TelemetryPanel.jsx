import React from 'react';

const TelemetryPanel = ({ vehicles }) => {
    return (
        <div>
            <h2 className="text-2xl font-black mb-4 border-b-4 border-black inline-block">TELEMETRY</h2>

            {vehicles.map(v => (
                <div key={v.id} className="mb-4 last:mb-0 bg-neo-bg border-2 border-black p-3 relative group hover:bg-white transition-colors">
                    {/* Corner Accent */}
                    <div className={`absolute -top-2 -right-2 w-4 h-4 border-2 border-black ${v.id === 'scout' ? 'bg-neo-blue' : 'bg-neo-orange'}`}></div>

                    <div className="flex justify-between items-center mb-2">
                        <h3 className="text-xl font-bold uppercase">{v.id}</h3>
                        <span className={`px-2 py-0.5 text-xs font-bold border-2 border-black ${v.status === 'ARMED' || v.status === 'FLYING' ? 'bg-neo-green text-white' : 'bg-gray-300'
                            }`}>
                            {v.status}
                        </span>
                    </div>

                    <div className="grid grid-cols-2 gap-2 text-sm">
                        <div className="bg-white border text-center py-1 font-mono font-bold">
                            BAT: {v.battery}%
                        </div>
                        <div className="bg-white border text-center py-1 font-mono font-bold">
                            ALT: {v.alt.toFixed(1)}m
                        </div>
                        <div className="bg-white border text-center py-1 font-mono text-xs col-span-2">
                            {v.lat.toFixed(5)}, {v.lon.toFixed(5)}
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default TelemetryPanel;
