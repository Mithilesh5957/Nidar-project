import React from 'react';
import { useGCS } from '../context/GCSContext';
import DetectionPanel from '../components/DetectionPanel';
import AnalyticsPanel from '../components/AnalyticsPanel';

const Intelligence = () => {
    const { detections, vehicles } = useGCS();

    return (
        <div className="grid grid-cols-12 gap-6 h-full">
            {/* Left Sidebar - Stats & Controls */}
            <div className="col-span-4 flex flex-col gap-6 h-full overflow-y-auto pr-1">
                {/* Header Card */}
                <div className="bg-white rounded-xl shadow-soft border border-slate-200 p-6 flex justify-between items-center">
                    <div>
                        <h2 className="text-xl font-bold text-slate-900">Intelligence</h2>
                        <p className="text-slate-500 text-sm">Target Analysis</p>
                    </div>
                    <div className="text-right">
                        <div className="text-3xl font-bold text-accent-blue leading-none">{detections.length}</div>
                        <div className="text-[10px] font-bold uppercase text-slate-400 tracking-wider">Targets</div>
                    </div>
                </div>

                {/* Analytics */}
                <div className="bg-white rounded-xl shadow-soft border border-slate-200 p-1">
                    <div className="px-4 py-2 border-b border-slate-100">
                        <h3 className="text-xs font-bold text-slate-500 uppercase tracking-wider">System Performance</h3>
                    </div>
                    <AnalyticsPanel vehicles={vehicles} />
                </div>

                {/* Placeholder for future module */}
                <div className="bg-slate-50 rounded-xl border border-dashed border-slate-300 flex items-center justify-center p-8">
                    <span className="text-slate-400 font-medium text-sm">AI Classification Module</span>
                </div>
            </div>

            {/* Main Content - Detection Feed */}
            <div className="col-span-8 bg-white rounded-2xl shadow-soft border border-slate-200 overflow-hidden flex flex-col relative h-[85vh]">
                <div className="absolute top-4 left-4 z-[40] bg-white/90 backdrop-blur px-3 py-1.5 rounded-lg border border-slate-200 shadow-sm">
                    <span className="text-xs font-bold text-slate-500 uppercase">Live Target Feed</span>
                </div>
                <div className="p-1 h-full overflow-hidden">
                    <DetectionPanel detections={detections} />
                </div>
            </div>
        </div>
    );
};

export default Intelligence;
