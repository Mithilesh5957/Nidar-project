import React from 'react';

const TelemetryPanel = ({ vehicles, selectedId, onSelect }) => {
    return (
        <div className="h-full">
            <h2 className="text-sm font-semibold text-slate-500 uppercase tracking-wider mb-3">Fleet Status</h2>
            <div className="flex flex-col gap-3">
                {vehicles.map(v => (
                    <div
                        key={v.id}
                        onClick={() => onSelect && onSelect(v.id)}
                        className={`bg-white border rounded-lg p-4 shadow-soft transition-all cursor-pointer ${selectedId === v.id
                                ? 'border-indigo-500 ring-2 ring-indigo-500/20'
                                : 'border-slate-200 hover:shadow-medium hover:border-slate-300'
                            }`}
                    >
                        <div className="flex justify-between items-start mb-2">
                            <div>
                                <h3 className="text-lg font-bold text-slate-800 uppercase leading-none">{v.id}</h3>
                                <span className="text-xs text-slate-400 font-mono">ID: {v.id.substring(0, 4)}...</span>
                            </div>
                            <div className={`px-2 py-0.5 rounded text-xs font-bold ${v.status === 'ACTIVE' ? 'bg-green-100 text-green-700' : 'bg-slate-100 text-slate-500'}`}>
                                {v.status}
                            </div>
                        </div>

                        <div className="grid grid-cols-3 gap-4 mt-4">
                            <div className="flex flex-col">
                                <span className="text-[10px] uppercase text-slate-400 font-semibold">Alt (m)</span>
                                <span className="text-xl font-medium text-slate-700">{v.alt.toFixed(1)}</span>
                            </div>
                            <div className="flex flex-col">
                                <span className="text-[10px] uppercase text-slate-400 font-semibold">Speed</span>
                                <span className="text-xl font-medium text-slate-700">12.4</span>
                            </div>
                            <div className="flex flex-col">
                                <span className="text-[10px] uppercase text-slate-400 font-semibold">Battery</span>
                                <span className={`text-xl font-medium ${v.battery < 20 ? 'text-accent-red' : 'text-slate-700'}`}>
                                    {v.battery}%
                                </span>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default TelemetryPanel;
