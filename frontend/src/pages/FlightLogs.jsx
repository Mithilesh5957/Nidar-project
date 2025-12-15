import React, { useState, useEffect } from 'react';
import { FlightLogService } from '../services/api';

const FlightLogs = () => {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedLog, setSelectedLog] = useState(null);

    useEffect(() => {
        fetchLogs();
    }, []);

    const fetchLogs = async () => {
        try {
            setLoading(true);
            const res = await FlightLogService.getAll();
            setLogs(res.data);
        } catch (err) {
            console.error('Failed to load flight logs', err);
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Delete this flight log?')) {
            try {
                await FlightLogService.delete(id);
                fetchLogs();
            } catch (err) {
                console.error('Failed to delete log', err);
            }
        }
    };

    const formatDuration = (seconds) => {
        if (!seconds) return '--';
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}m ${secs}s`;
    };

    const formatDate = (dateStr) => {
        if (!dateStr) return '--';
        return new Date(dateStr).toLocaleString();
    };

    return (
        <div className="p-6 space-y-6">
            {/* Header */}
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold text-slate-800">Flight Logs</h1>
                    <p className="text-slate-500">View and analyze past flight data</p>
                </div>
                <button onClick={fetchLogs} className="px-4 py-2 bg-slate-100 rounded-lg hover:bg-slate-200 transition">
                    🔄 Refresh
                </button>
            </div>

            {/* Stats Summary */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div className="bg-gradient-to-br from-blue-500 to-blue-600 rounded-xl p-4 text-white">
                    <p className="text-blue-100 text-sm">Total Flights</p>
                    <p className="text-3xl font-bold">{logs.length}</p>
                </div>
                <div className="bg-gradient-to-br from-green-500 to-green-600 rounded-xl p-4 text-white">
                    <p className="text-green-100 text-sm">Total Distance</p>
                    <p className="text-3xl font-bold">{logs.reduce((sum, l) => sum + (l.distanceFlown || 0), 0).toFixed(1)} km</p>
                </div>
                <div className="bg-gradient-to-br from-purple-500 to-purple-600 rounded-xl p-4 text-white">
                    <p className="text-purple-100 text-sm">Total Flight Time</p>
                    <p className="text-3xl font-bold">{formatDuration(logs.reduce((sum, l) => sum + (l.flightDuration || 0), 0))}</p>
                </div>
                <div className="bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl p-4 text-white">
                    <p className="text-orange-100 text-sm">Max Altitude</p>
                    <p className="text-3xl font-bold">{Math.max(...logs.map(l => l.maxAltitude || 0), 0).toFixed(0)} m</p>
                </div>
            </div>

            {/* Log Detail Modal */}
            {selectedLog && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
                    <div className="bg-white rounded-xl p-6 w-full max-w-lg shadow-xl">
                        <div className="flex justify-between items-start mb-4">
                            <h2 className="text-xl font-bold">Flight Details</h2>
                            <button onClick={() => setSelectedLog(null)} className="text-slate-400 hover:text-slate-600">✕</button>
                        </div>
                        <div className="space-y-3 text-sm">
                            <div className="grid grid-cols-2 gap-4">
                                <div><span className="text-slate-500">Start:</span> {formatDate(selectedLog.startTime)}</div>
                                <div><span className="text-slate-500">End:</span> {formatDate(selectedLog.endTime)}</div>
                                <div><span className="text-slate-500">Duration:</span> {formatDuration(selectedLog.flightDuration)}</div>
                                <div><span className="text-slate-500">Status:</span> <span className={selectedLog.flightStatus === 'COMPLETED' ? 'text-green-600' : 'text-yellow-600'}>{selectedLog.flightStatus}</span></div>
                                <div><span className="text-slate-500">Distance:</span> {selectedLog.distanceFlown?.toFixed(2)} km</div>
                                <div><span className="text-slate-500">Max Altitude:</span> {selectedLog.maxAltitude?.toFixed(0)} m</div>
                                <div><span className="text-slate-500">Max Speed:</span> {selectedLog.maxSpeed?.toFixed(1)} m/s</div>
                                <div><span className="text-slate-500">Avg Speed:</span> {selectedLog.avgSpeed?.toFixed(1)} m/s</div>
                                <div><span className="text-slate-500">Battery Used:</span> {selectedLog.batteryUsed?.toFixed(0)}%</div>
                            </div>
                            {selectedLog.notes && (
                                <div className="mt-4 p-3 bg-slate-50 rounded-lg">
                                    <span className="text-slate-500">Notes:</span>
                                    <p className="mt-1">{selectedLog.notes}</p>
                                </div>
                            )}
                        </div>
                        <div className="mt-6 flex justify-end">
                            <button onClick={() => setSelectedLog(null)} className="px-4 py-2 bg-slate-100 rounded-lg hover:bg-slate-200">Close</button>
                        </div>
                    </div>
                </div>
            )}

            {/* Flight Logs Table */}
            {loading ? (
                <div className="text-center py-12 text-slate-500">Loading flight logs...</div>
            ) : logs.length === 0 ? (
                <div className="text-center py-12 text-slate-500">No flight logs recorded yet.</div>
            ) : (
                <div className="bg-white rounded-xl border border-slate-200 overflow-hidden">
                    <table className="w-full">
                        <thead className="bg-slate-50 border-b border-slate-200">
                            <tr>
                                <th className="px-4 py-3 text-left text-sm font-semibold text-slate-600">Date</th>
                                <th className="px-4 py-3 text-left text-sm font-semibold text-slate-600">Duration</th>
                                <th className="px-4 py-3 text-left text-sm font-semibold text-slate-600">Distance</th>
                                <th className="px-4 py-3 text-left text-sm font-semibold text-slate-600">Max Alt</th>
                                <th className="px-4 py-3 text-left text-sm font-semibold text-slate-600">Status</th>
                                <th className="px-4 py-3 text-right text-sm font-semibold text-slate-600">Actions</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100">
                            {logs.map(log => (
                                <tr key={log.id} className="hover:bg-slate-50 transition">
                                    <td className="px-4 py-3 text-sm">{formatDate(log.startTime)}</td>
                                    <td className="px-4 py-3 text-sm">{formatDuration(log.flightDuration)}</td>
                                    <td className="px-4 py-3 text-sm">{log.distanceFlown?.toFixed(2) || '--'} km</td>
                                    <td className="px-4 py-3 text-sm">{log.maxAltitude?.toFixed(0) || '--'} m</td>
                                    <td className="px-4 py-3 text-sm">
                                        <span className={`px-2 py-1 rounded text-xs font-medium ${log.flightStatus === 'COMPLETED' ? 'bg-green-100 text-green-700' :
                                                log.flightStatus === 'ABORTED' ? 'bg-red-100 text-red-700' :
                                                    'bg-yellow-100 text-yellow-700'
                                            }`}>
                                            {log.flightStatus}
                                        </span>
                                    </td>
                                    <td className="px-4 py-3 text-right space-x-2">
                                        <button onClick={() => setSelectedLog(log)} className="text-blue-600 hover:text-blue-800 text-sm">View</button>
                                        <button onClick={() => handleDelete(log.id)} className="text-red-600 hover:text-red-800 text-sm">Delete</button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default FlightLogs;
