import React from 'react';
import { useGCS } from '../context/GCSContext';
import DetectionPanel from '../components/DetectionPanel';
import AnalyticsPanel from '../components/AnalyticsPanel';

const Intelligence = () => {
    const { detections, vehicles } = useGCS();

    return (
        <div className="h-full flex flex-col gap-6">
            <div className="flex justify-between items-end">
                <div>
                    <h2 className="text-2xl font-bold text-slate-900">Intelligence gathered</h2>
                    <p className="text-slate-500">Real-time analysis of targets and system performance.</p>
                </div>
                <div className="text-right">
                    <div className="text-3xl font-bold text-accent-blue">{detections.length}</div>
                    <div className="text-xs font-bold uppercase text-slate-400">Total Targets</div>
                </div>
            </div>

            <div className="grid grid-cols-2 gap-6 flex-1 min-h-0">
                <div className="bg-white rounded-xl shadow-soft border border-slate-200 p-4 overflow-hidden flex flex-col">
                    <DetectionPanel detections={detections} />
                </div>

                <div className="flex flex-col gap-6">
                    <div className="bg-white rounded-xl shadow-soft border border-slate-200 p-4">
                        <h3 className="text-sm font-bold text-slate-500 uppercase mb-4">System Telemetry</h3>
                        <AnalyticsPanel vehicles={vehicles} />
                    </div>
                    {/* Placeholder for future intel widgets */}
                    <div className="bg-slate-100 rounded-xl border border-dashed border-slate-300 flex items-center justify-center flex-1">
                        <span className="text-slate-400 font-medium">Future Intel Modules</span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Intelligence;
