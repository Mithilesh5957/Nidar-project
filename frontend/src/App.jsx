import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './layouts/Layout';
import Dashboard from './pages/Dashboard';
import Planner from './pages/Planner';
import Intelligence from './pages/Intelligence';
import Home from './pages/Home';
import DroneManagement from './pages/DroneManagement';
import GeofenceEditor from './pages/GeofenceEditor';
import FlightLogs from './pages/FlightLogs';
import MissionSimulator from './pages/MissionSimulator';
import { GCSProvider } from './context/GCSContext';
import './index.css';

const App = () => {
    return (
        <BrowserRouter>
            <GCSProvider>
                <Routes>
                    {/* Public Landing Page */}
                    <Route path="/" element={<Home />} />

                    {/* Protected App Routes */}
                    <Route path="/app" element={<Layout />}>
                        <Route index element={<Dashboard />} />
                        <Route path="planner" element={<Planner />} />
                        <Route path="intel" element={<Intelligence />} />
                        <Route path="drones" element={<DroneManagement />} />
                        <Route path="geofence" element={<GeofenceEditor />} />
                        <Route path="logs" element={<FlightLogs />} />
                        <Route path="simulator" element={<MissionSimulator />} />
                    </Route>

                    {/* Fallback */}
                    <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
            </GCSProvider>
        </BrowserRouter>
    );
};

export default App;
