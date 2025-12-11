import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './layouts/Layout';
import Dashboard from './pages/Dashboard';
import Planner from './pages/Planner';
import Intelligence from './pages/Intelligence';
import Home from './pages/Home';
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
                    </Route>

                    {/* Fallback */}
                    <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
            </GCSProvider>
        </BrowserRouter>
    );
};

export default App;
