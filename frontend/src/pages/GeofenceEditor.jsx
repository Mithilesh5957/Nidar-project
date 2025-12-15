import React, { useState, useEffect } from 'react';
import { useGCS } from '../context/GCSContext';
import { GeofenceService } from '../services/api';
import MapView from '../components/MapView';

const GeofenceEditor = () => {
    const { vehicles, missions } = useGCS();
    const [geofences, setGeofences] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showForm, setShowForm] = useState(false);
    const [editZone, setEditZone] = useState(null);
    const [validationResult, setValidationResult] = useState(null);

    const [formData, setFormData] = useState({
        name: '',
        type: 'INCLUSION',
        minAltitude: 0,
        maxAltitude: 120,
        violationAction: 'REPORT',
        active: true
    });

    const [testPosition, setTestPosition] = useState({ lat: '', lon: '', alt: '' });

    useEffect(() => {
        fetchGeofences();
    }, []);

    const fetchGeofences = async () => {
        try {
            setLoading(true);
            const res = await GeofenceService.getAll();
            setGeofences(res.data);
        } catch (err) {
            console.error('Failed to load geofences', err);
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (editZone) {
                await GeofenceService.update(editZone.id, formData);
            } else {
                await GeofenceService.create(formData);
            }
            setShowForm(false);
            setEditZone(null);
            resetForm();
            fetchGeofences();
        } catch (err) {
            console.error('Failed to save geofence', err);
        }
    };

    const resetForm = () => {
        setFormData({
            name: '',
            type: 'INCLUSION',
            minAltitude: 0,
            maxAltitude: 120,
            violationAction: 'REPORT',
            active: true
        });
    };

    const handleEdit = (zone) => {
        setEditZone(zone);
        setFormData({
            name: zone.name || '',
            type: zone.type || 'INCLUSION',
            minAltitude: zone.minAltitude || 0,
            maxAltitude: zone.maxAltitude || 120,
            violationAction: zone.violationAction || 'REPORT',
            active: zone.active !== false
        });
        setShowForm(true);
    };

    const handleDelete = async (id) => {
        if (window.confirm('Delete this geofence zone?')) {
            try {
                await GeofenceService.delete(id);
                fetchGeofences();
            } catch (err) {
                console.error('Failed to delete geofence', err);
            }
        }
    };

    const handleValidatePosition = async () => {
        if (!testPosition.lat || !testPosition.lon) return;
        try {
            const res = await GeofenceService.validatePosition(
                parseFloat(testPosition.lat),
                parseFloat(testPosition.lon),
                parseFloat(testPosition.alt) || 50
            );
            setValidationResult(res.data);
        } catch (err) {
            console.error('Validation failed', err);
        }
    };

    return (
        <div className="grid grid-cols-12 gap-6 h-[85vh]">
            {/* Left Panel */}
            <div className="col-span-4 flex flex-col gap-4 overflow-y-auto pr-2">
                {/* Header */}
                <div className="flex justify-between items-center">
                    <div>
                        <h1 className="text-xl font-bold text-slate-800">Geofence Zones</h1>
                        <p className="text-sm text-slate-500">Define safe flight boundaries</p>
                    </div>
                    <button
                        onClick={() => { setShowForm(true); setEditZone(null); resetForm(); }}
                        className="px-3 py-1.5 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700"
                    >
                        + Add Zone
                    </button>
                </div>

                {/* Position Validator */}
                <div className="bg-white rounded-xl border border-slate-200 p-4">
                    <h3 className="font-semibold text-slate-700 mb-3">Position Validator</h3>
                    <div className="grid grid-cols-3 gap-2 mb-3">
                        <input type="number" placeholder="Lat" value={testPosition.lat} onChange={(e) => setTestPosition({ ...testPosition, lat: e.target.value })} className="px-2 py-1.5 border rounded text-sm" step="0.0001" />
                        <input type="number" placeholder="Lon" value={testPosition.lon} onChange={(e) => setTestPosition({ ...testPosition, lon: e.target.value })} className="px-2 py-1.5 border rounded text-sm" step="0.0001" />
                        <input type="number" placeholder="Alt" value={testPosition.alt} onChange={(e) => setTestPosition({ ...testPosition, alt: e.target.value })} className="px-2 py-1.5 border rounded text-sm" />
                    </div>
                    <button onClick={handleValidatePosition} className="w-full px-3 py-1.5 bg-slate-100 rounded-lg hover:bg-slate-200 text-sm">
                        🔍 Check Position
                    </button>
                    {validationResult && (
                        <div className={`mt-3 p-2 rounded text-sm ${validationResult.valid ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                            {validationResult.valid ? '✅ Position valid' : `❌ ${validationResult.message}`}
                        </div>
                    )}
                </div>

                {/* Geofence List */}
                {loading ? (
                    <div className="text-center py-8 text-slate-500">Loading...</div>
                ) : geofences.length === 0 ? (
                    <div className="text-center py-8 text-slate-500">No geofence zones defined.</div>
                ) : (
                    <div className="space-y-3">
                        {geofences.map(zone => (
                            <div key={zone.id} className={`bg-white rounded-xl border p-4 ${zone.type === 'EXCLUSION' ? 'border-red-200' : 'border-green-200'}`}>
                                <div className="flex justify-between items-start mb-2">
                                    <div>
                                        <h3 className="font-semibold text-slate-800">{zone.name}</h3>
                                        <span className={`text-xs px-2 py-0.5 rounded ${zone.type === 'EXCLUSION' ? 'bg-red-100 text-red-700' : 'bg-green-100 text-green-700'}`}>
                                            {zone.type}
                                        </span>
                                    </div>
                                    <span className={`text-xs px-2 py-0.5 rounded ${zone.active ? 'bg-blue-100 text-blue-700' : 'bg-slate-100 text-slate-500'}`}>
                                        {zone.active ? 'Active' : 'Inactive'}
                                    </span>
                                </div>
                                <div className="text-sm text-slate-600 mb-3">
                                    <p>Alt: {zone.minAltitude}m - {zone.maxAltitude}m</p>
                                    <p>Action: {zone.violationAction}</p>
                                </div>
                                <div className="flex gap-2">
                                    <button onClick={() => handleEdit(zone)} className="flex-1 px-2 py-1 text-sm bg-slate-100 rounded hover:bg-slate-200">Edit</button>
                                    <button onClick={() => handleDelete(zone.id)} className="px-2 py-1 text-sm bg-red-100 text-red-700 rounded hover:bg-red-200">Delete</button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Map */}
            <div className="col-span-8 bg-white rounded-2xl shadow-soft border border-slate-200 overflow-hidden relative">
                <div className="absolute top-4 left-4 z-[400] bg-orange-500/10 backdrop-blur px-3 py-1.5 rounded-lg border border-orange-500/20">
                    <span className="text-xs font-bold text-orange-600 uppercase">Geofence View</span>
                </div>
                <MapView vehicles={vehicles} missions={missions} />
            </div>

            {/* Form Modal */}
            {showForm && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
                    <div className="bg-white rounded-xl p-6 w-full max-w-md shadow-xl">
                        <h2 className="text-xl font-bold mb-4">{editZone ? 'Edit Zone' : 'Add Geofence Zone'}</h2>
                        <form onSubmit={handleSubmit} className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Name</label>
                                <input type="text" value={formData.name} onChange={(e) => setFormData({ ...formData, name: e.target.value })} className="w-full px-3 py-2 border rounded-lg" required />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Type</label>
                                <select value={formData.type} onChange={(e) => setFormData({ ...formData, type: e.target.value })} className="w-full px-3 py-2 border rounded-lg">
                                    <option value="INCLUSION">Inclusion (Fly inside)</option>
                                    <option value="EXCLUSION">Exclusion (No-fly zone)</option>
                                </select>
                            </div>
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Min Altitude (m)</label>
                                    <input type="number" value={formData.minAltitude} onChange={(e) => setFormData({ ...formData, minAltitude: parseFloat(e.target.value) })} className="w-full px-3 py-2 border rounded-lg" />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Max Altitude (m)</label>
                                    <input type="number" value={formData.maxAltitude} onChange={(e) => setFormData({ ...formData, maxAltitude: parseFloat(e.target.value) })} className="w-full px-3 py-2 border rounded-lg" />
                                </div>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Violation Action</label>
                                <select value={formData.violationAction} onChange={(e) => setFormData({ ...formData, violationAction: e.target.value })} className="w-full px-3 py-2 border rounded-lg">
                                    <option value="REPORT">Report Only</option>
                                    <option value="WARN">Warn Pilot</option>
                                    <option value="LOITER">Loiter in Place</option>
                                    <option value="RTL">Return to Launch</option>
                                    <option value="LAND">Land Immediately</option>
                                </select>
                            </div>
                            <div className="flex items-center gap-2">
                                <input type="checkbox" id="active" checked={formData.active} onChange={(e) => setFormData({ ...formData, active: e.target.checked })} className="w-4 h-4" />
                                <label htmlFor="active" className="text-sm text-slate-700">Active</label>
                            </div>
                            <div className="flex gap-3 justify-end pt-2">
                                <button type="button" onClick={() => setShowForm(false)} className="px-4 py-2 border rounded-lg hover:bg-slate-50">Cancel</button>
                                <button type="submit" className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">Save</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default GeofenceEditor;
