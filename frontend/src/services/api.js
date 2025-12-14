import axios from 'axios';

const api = axios.create({
    baseURL: '/api', // Proxied by Nginx to :8081/api/vehicles via Nginx or direct dev proxy
    headers: {
        'Content-Type': 'application/json',
    },
});

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
    
    // Stream
    requestStream: (id) => api.post(`/vehicles/${id}/command/stream`)
};

export default api;
