import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { GCSProvider } from './context/GCSContext';
import Layout from './layouts/Layout';
import Dashboard from './pages/Dashboard';
import Planner from './pages/Planner';
import Intelligence from './pages/Intelligence';
import './index.css';

function App() {
    return (
        <BrowserRouter>
            <GCSProvider>
                <Routes>
                    <Route path="/" element={<Layout />}>
                        <Route index element={<Dashboard />} />
                        <Route path="planner" element={<Planner />} />
                        <Route path="intel" element={<Intelligence />} />
                        <Route path="*" element={<Navigate to="/" replace />} />
                    </Route>
                </Routes>
            </GCSProvider>
        </BrowserRouter>
    );
}

export default App;
