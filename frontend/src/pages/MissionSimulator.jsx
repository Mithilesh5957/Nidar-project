import React, { useState } from 'react';
import { useGCS } from '../context/GCSContext';
import { SimulatorService, MissionService } from '../services/api';

const MissionSimulator = () => {
    const { missions } = useGCS();
    const [missionList, setMissionList] = useState([]);
    const [selectedMissionId, setSelectedMissionId] = useState(null);
    const [validationResult, setValidationResult] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    // Fetch missions on mount
    React.useEffect(() => {
        const fetchMissions = async () => {
            try {
                const res = await MissionService.getAll();
                setMissionList(res.data);
            } catch (err) {
                console.error('Failed to fetch missions', err);
            }
        };
        fetchMissions();
    }, []);

    const handleValidate = async () => {
        if (!selectedMissionId) return;

        setLoading(true);
        setError(null);
        setValidationResult(null);

        try {
            const res = await SimulatorService.validateMission(selectedMissionId);
            setValidationResult(res.data);
        } catch (err) {
            setError('Failed to validate mission');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-6 space-y-6">
            {/* Header */}
            <div>
                <h1 className="text-2xl font-bold text-slate-800">Mission Simulator</h1>
                <p className="text-slate-500">Validate missions before deployment to check for issues</p>
            </div>

            {/* Mission Selection */}
            <div className="bg-white rounded-xl border border-slate-200 p-6">
                <h2 className="text-lg font-semibold mb-4">Select Mission to Validate</h2>
                <div className="flex gap-4">
                    <select
                        value={selectedMissionId || ''}
                        onChange={(e) => setSelectedMissionId(e.target.value)}
                        className="flex-1 px-4 py-2 border rounded-lg bg-white"
                    >
                        <option value="">-- Select a mission --</option>
                        {missionList.map(m => (
                            <option key={m.id} value={m.id}>{m.name || `Mission ${m.id}`}</option>
                        ))}
                    </select>
                    <button
                        onClick={handleValidate}
                        disabled={!selectedMissionId || loading}
                        className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-slate-300 disabled:cursor-not-allowed transition"
                    >
                        {loading ? 'Validating...' : '🔍 Validate Mission'}
                    </button>
                </div>
            </div>

            {error && (
                <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-700">{error}</div>
            )}

            {/* Validation Results */}
            {validationResult && (
                <div className="space-y-4">
                    {/* Summary Card */}
                    <div className={`p-6 rounded-xl border ${validationResult.valid ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'}`}>
                        <div className="flex items-center gap-3 mb-4">
                            <span className="text-4xl">{validationResult.valid ? '✅' : '❌'}</span>
                            <div>
                                <h3 className="text-xl font-bold">{validationResult.valid ? 'Mission Valid' : 'Validation Failed'}</h3>
                                <p className="text-slate-600">{validationResult.waypointCount} waypoints analyzed</p>
                            </div>
                        </div>
                    </div>

                    {/* Metrics Grid */}
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                        <div className="bg-white rounded-xl border border-slate-200 p-4">
                            <p className="text-slate-500 text-sm">Total Distance</p>
                            <p className="text-2xl font-bold text-slate-800">{validationResult.totalDistance?.toFixed(0)} m</p>
                        </div>
                        <div className="bg-white rounded-xl border border-slate-200 p-4">
                            <p className="text-slate-500 text-sm">Est. Flight Time</p>
                            <p className="text-2xl font-bold text-slate-800">{Math.floor(validationResult.estimatedFlightTime / 60)}m {Math.floor(validationResult.estimatedFlightTime % 60)}s</p>
                        </div>
                        <div className="bg-white rounded-xl border border-slate-200 p-4">
                            <p className="text-slate-500 text-sm">Est. Battery Usage</p>
                            <p className={`text-2xl font-bold ${validationResult.estimatedBatteryUsage > 80 ? 'text-red-600' : 'text-slate-800'}`}>
                                {validationResult.estimatedBatteryUsage?.toFixed(0)}%
                            </p>
                        </div>
                        <div className="bg-white rounded-xl border border-slate-200 p-4">
                            <p className="text-slate-500 text-sm">Max Altitude</p>
                            <p className={`text-2xl font-bold ${validationResult.maxAltitude > 120 ? 'text-orange-600' : 'text-slate-800'}`}>
                                {validationResult.maxAltitude?.toFixed(0)} m
                            </p>
                        </div>
                    </div>

                    {/* Errors */}
                    {validationResult.errors && validationResult.errors.length > 0 && (
                        <div className="bg-red-50 rounded-xl border border-red-200 p-4">
                            <h4 className="font-semibold text-red-800 mb-2">❌ Errors ({validationResult.errors.length})</h4>
                            <ul className="space-y-1">
                                {validationResult.errors.map((err, i) => (
                                    <li key={i} className="text-red-700 text-sm">• {err}</li>
                                ))}
                            </ul>
                        </div>
                    )}

                    {/* Warnings */}
                    {validationResult.warnings && validationResult.warnings.length > 0 && (
                        <div className="bg-yellow-50 rounded-xl border border-yellow-200 p-4">
                            <h4 className="font-semibold text-yellow-800 mb-2">⚠️ Warnings ({validationResult.warnings.length})</h4>
                            <ul className="space-y-1">
                                {validationResult.warnings.map((warn, i) => (
                                    <li key={i} className="text-yellow-700 text-sm">• {warn}</li>
                                ))}
                            </ul>
                        </div>
                    )}

                    {/* All Good */}
                    {validationResult.valid && validationResult.warnings?.length === 0 && (
                        <div className="bg-green-50 rounded-xl border border-green-200 p-4 text-center">
                            <p className="text-green-700 font-medium">✨ No issues found! Mission is ready for deployment.</p>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default MissionSimulator;
