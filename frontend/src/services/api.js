import axios from 'axios';

const api = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

// ============ Vehicle Service ============
export const VehicleService = {
    getAll: () => api.get('/vehicles'),
    getHistory: (id) => api.get(`/vehicles/${id}/telemetry-history`),

    // Commands
    arm: (id) => api.post(`/vehicles/${id}/command/arm`),
    disarm: (id) => api.post(`/vehicles/${id}/command/disarm`),
    takeoff: (id, altitude) => api.post(`/vehicles/${id}/command/takeoff`, null, { params: { altitude } }),
    rtl: (id) => api.post(`/vehicles/${id}/command/rtl`),

    // Mode
    setMode: (id, mode) => api.post(`/vehicles/${id}/command/mode`, null, { params: { mode } }),

    // Navigation
    goto: (id, lat, lon, alt) => api.post(`/vehicles/${id}/command/goto`, null, { params: { lat, lon, alt } }),

    // Mission
    uploadMission: (id, mission) => api.post(`/vehicles/${id}/mission-upload`, mission),
    fetchMission: (id) => api.post(`/vehicles/${id}/mission-fetch`),

    // Stream
    requestStream: (id) => api.post(`/vehicles/${id}/command/stream`),

    // Diagnostics
    getDiagnostics: () => api.get('/vehicles/diagnostics'),
};

// ============ Drone Service (MAVProxy/QGC) ============
export const DroneService = {
    // Connection
    connect: () => api.post('/drone/connect'),
    disconnect: () => api.post('/drone/disconnect'),
    getStatus: () => api.get('/drone/status'),
    getLatestTelemetry: () => api.get('/drone/telemetry'),
    sendCommand: (command) => api.post('/drone/command', null, { params: { command } }),

    // Drone Management (CRUD)
    getAll: () => api.get('/drones'),
    getById: (id) => api.get(`/drones/${id}`),
    create: (drone) => api.post('/drones', drone),
    update: (id, drone) => api.put(`/drones/${id}`, drone),
    delete: (id) => api.delete(`/drones/${id}`),
    getConnected: () => api.get('/drones/connected'),
    updatePosition: (id, position) => api.post(`/drones/${id}/position`, position),
    updateStatus: (id, status) => api.post(`/drones/${id}/status`, status),
};

// ============ Mission Service ============
export const MissionService = {
    getAll: () => api.get('/mission'),
    getById: (id) => api.get(`/mission/${id}`),
    create: (mission) => api.post('/mission', mission),
    deploy: (id) => api.post(`/mission/${id}/deploy`),
    upload: (file) => {
        const formData = new FormData();
        formData.append('file', file);
        return api.post('/mission/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
    },
};

// ============ Geofence Service ============
export const GeofenceService = {
    getAll: () => api.get('/geofence'),
    getById: (id) => api.get(`/geofence/${id}`),
    create: (zone) => api.post('/geofence', zone),
    update: (id, zone) => api.put(`/geofence/${id}`, zone),
    delete: (id) => api.delete(`/geofence/${id}`),
    getByMission: (missionId) => api.get(`/geofence/mission/${missionId}`),
    validatePosition: (lat, lon, alt) => api.post('/geofence/validate', null, { params: { lat, lon, alt } }),
};

// ============ Flight Log Service ============
export const FlightLogService = {
    getAll: () => api.get('/logs'),
    getById: (id) => api.get(`/logs/${id}`),
    create: (log) => api.post('/logs', log),
    update: (id, log) => api.put(`/logs/${id}`, log),
    delete: (id) => api.delete(`/logs/${id}`),
};

// ============ Simulator Service ============
export const SimulatorService = {
    validateMission: (missionId) => api.post(`/simulator/validate/${missionId}`),
    validateMissionData: (mission) => api.post('/simulator/validate', mission),
};

// ============ Replay Service ============
export const ReplayService = {
    getMissionReplayData: (missionId) => api.get(`/replay/mission/${missionId}`),
    getTelemetryByMission: (missionId) => api.get(`/replay/telemetry/${missionId}`),
};

// ============ Vehicle Parameter Service ============
export const ParameterService = {
    getAll: () => api.get('/parameters'),
    getById: (id) => api.get(`/parameters/${id}`),
    create: (param) => api.post('/parameters', param),
    update: (id, param) => api.put(`/parameters/${id}`, param),
    delete: (id) => api.delete(`/parameters/${id}`),
    upload: () => api.post('/parameters/upload'),
};

// ============ Detection Service ============
export const DetectionService = {
    getAll: () => api.get('/detections'),
    getById: (id) => api.get(`/detections/${id}`),
    approve: (id) => api.post(`/detections/${id}/approve`),
};

export default api;
