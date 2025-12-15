import React, { useState, useEffect } from 'react';
import { DroneService } from '../services/api';

const DroneManagement = () => {
    const [drones, setDrones] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showForm, setShowForm] = useState(false);
    const [editDrone, setEditDrone] = useState(null);
    const [connectionStatus, setConnectionStatus] = useState({ connected: false });

    const [formData, setFormData] = useState({
        name: '',
        serialNumber: '',
        model: '',
        mavproxyHost: 'localhost',
        mavproxyPort: 14550
    });

    useEffect(() => {
        fetchDrones();
        fetchConnectionStatus();
    }, []);

    const fetchDrones = async () => {
        try {
            setLoading(true);
            const res = await DroneService.getAll();
            setDrones(res.data);
        } catch (err) {
            setError('Failed to load drones');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const fetchConnectionStatus = async () => {
        try {
            const res = await DroneService.getStatus();
            setConnectionStatus(res.data);
        } catch (err) {
            console.error('Failed to get connection status', err);
        }
    };

    const handleConnect = async () => {
        try {
            await DroneService.connect();
            fetchConnectionStatus();
        } catch (err) {
            setError('Failed to connect');
        }
    };

    const handleDisconnect = async () => {
        try {
            await DroneService.disconnect();
            fetchConnectionStatus();
        } catch (err) {
            setError('Failed to disconnect');
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (editDrone) {
                await DroneService.update(editDrone.id, formData);
            } else {
                await DroneService.create(formData);
            }
            setShowForm(false);
            setEditDrone(null);
            setFormData({ name: '', serialNumber: '', model: '', mavproxyHost: 'localhost', mavproxyPort: 14550 });
            fetchDrones();
        } catch (err) {
            setError('Failed to save drone');
        }
    };

    const handleEdit = (drone) => {
        setEditDrone(drone);
        setFormData({
            name: drone.name || '',
            serialNumber: drone.serialNumber || '',
            model: drone.model || '',
            mavproxyHost: drone.mavproxyHost || 'localhost',
            mavproxyPort: drone.mavproxyPort || 14550
        });
        setShowForm(true);
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this drone?')) {
            try {
                await DroneService.delete(id);
                fetchDrones();
            } catch (err) {
                setError('Failed to delete drone');
            }
        }
    };

    return (
        <div className="p-6 space-y-6">
            {/* Header */}
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold text-slate-800">Drone Management</h1>
                    <p className="text-slate-500">Manage and monitor your drone fleet</p>
                </div>
                <div className="flex gap-3">
                    <button
                        onClick={connectionStatus.connected ? handleDisconnect : handleConnect}
                        className={`px-4 py-2 rounded-lg font-medium transition ${connectionStatus.connected
                                ? 'bg-red-500 hover:bg-red-600 text-white'
                                : 'bg-green-500 hover:bg-green-600 text-white'
                            }`}
                    >
                        {connectionStatus.connected ? '🔴 Disconnect' : '🟢 Connect to MAVProxy'}
                    </button>
                    <button
                        onClick={() => { setShowForm(true); setEditDrone(null); setFormData({ name: '', serialNumber: '', model: '', mavproxyHost: 'localhost', mavproxyPort: 14550 }); }}
                        className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                    >
                        + Add Drone
                    </button>
                </div>
            </div>

            {/* Connection Status Card */}
            <div className={`p-4 rounded-xl border ${connectionStatus.connected ? 'bg-green-50 border-green-200' : 'bg-slate-50 border-slate-200'}`}>
                <div className="flex items-center gap-3">
                    <div className={`w-3 h-3 rounded-full ${connectionStatus.connected ? 'bg-green-500 animate-pulse' : 'bg-slate-400'}`} />
                    <span className="font-medium text-slate-700">
                        {connectionStatus.connected ? 'Connected to MAVProxy/QGC' : 'Not Connected'}
                    </span>
                </div>
            </div>

            {error && (
                <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-700">{error}</div>
            )}

            {/* Drone Form Modal */}
            {showForm && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
                    <div className="bg-white rounded-xl p-6 w-full max-w-md shadow-xl">
                        <h2 className="text-xl font-bold mb-4">{editDrone ? 'Edit Drone' : 'Add New Drone'}</h2>
                        <form onSubmit={handleSubmit} className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Name</label>
                                <input type="text" value={formData.name} onChange={(e) => setFormData({ ...formData, name: e.target.value })} className="w-full px-3 py-2 border rounded-lg" required />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Serial Number</label>
                                <input type="text" value={formData.serialNumber} onChange={(e) => setFormData({ ...formData, serialNumber: e.target.value })} className="w-full px-3 py-2 border rounded-lg" />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Model</label>
                                <input type="text" value={formData.model} onChange={(e) => setFormData({ ...formData, model: e.target.value })} className="w-full px-3 py-2 border rounded-lg" />
                            </div>
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">MAVProxy Host</label>
                                    <input type="text" value={formData.mavproxyHost} onChange={(e) => setFormData({ ...formData, mavproxyHost: e.target.value })} className="w-full px-3 py-2 border rounded-lg" />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Port</label>
                                    <input type="number" value={formData.mavproxyPort} onChange={(e) => setFormData({ ...formData, mavproxyPort: parseInt(e.target.value) })} className="w-full px-3 py-2 border rounded-lg" />
                                </div>
                            </div>
                            <div className="flex gap-3 justify-end">
                                <button type="button" onClick={() => setShowForm(false)} className="px-4 py-2 border rounded-lg hover:bg-slate-50">Cancel</button>
                                <button type="submit" className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">Save</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Drone List */}
            {loading ? (
                <div className="text-center py-12 text-slate-500">Loading drones...</div>
            ) : drones.length === 0 ? (
                <div className="text-center py-12 text-slate-500">No drones registered. Add one to get started.</div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {drones.map(drone => (
                        <div key={drone.id} className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm hover:shadow-md transition">
                            <div className="flex justify-between items-start mb-3">
                                <div>
                                    <h3 className="font-bold text-lg text-slate-800">{drone.name}</h3>
                                    <p className="text-sm text-slate-500">{drone.model || 'Unknown Model'}</p>
                                </div>
                                <div className={`px-2 py-1 rounded text-xs font-medium ${drone.connected ? 'bg-green-100 text-green-700' : 'bg-slate-100 text-slate-600'}`}>
                                    {drone.connected ? 'Online' : 'Offline'}
                                </div>
                            </div>
                            <div className="text-sm text-slate-600 space-y-1 mb-4">
                                <p>📍 {drone.lastLatitude?.toFixed(5) || '--'}, {drone.lastLongitude?.toFixed(5) || '--'}</p>
                                <p>🔋 {drone.lastBattery?.toFixed(0) || '--'}%</p>
                                <p>🌐 {drone.mavproxyHost}:{drone.mavproxyPort}</p>
                            </div>
                            <div className="flex gap-2">
                                <button onClick={() => handleEdit(drone)} className="flex-1 px-3 py-2 text-sm bg-slate-100 rounded-lg hover:bg-slate-200 transition">Edit</button>
                                <button onClick={() => handleDelete(drone.id)} className="px-3 py-2 text-sm bg-red-100 text-red-700 rounded-lg hover:bg-red-200 transition">Delete</button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default DroneManagement;
