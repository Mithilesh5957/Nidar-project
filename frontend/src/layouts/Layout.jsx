import React from 'react';
import { Outlet, NavLink, useLocation } from 'react-router-dom';
import { useGCS } from '../context/GCSContext';

const Layout = () => {
    // const { audioEnabled, toggleVoice } = useGCS();
    const location = useLocation();

    const navItems = [
        { name: 'Dashboard', path: '/app' },
        { name: 'Mission Planner', path: '/app/planner' },
        { name: 'Intelligence', path: '/app/intel' }
    ];

    return (
        <div className="min-h-screen bg-slate-50 font-display text-slate-800 flex flex-col">
            {/* Header */}
            <header className="bg-white border-b border-slate-200 px-6 py-4 flex justify-between items-center sticky top-0 z-50">
                <div className="flex items-center gap-4">
                    <div className="w-10 h-10 bg-slate-900 rounded-lg flex items-center justify-center text-white font-bold text-xl shadow-soft">
                        N
                    </div>
                    <div>
                        <h1 className="text-xl font-bold tracking-tight text-slate-900 leading-none">
                            NIDAR <span className="text-slate-400 font-light">GCS</span>
                        </h1>
                        <p className="text-[10px] text-slate-500 font-bold tracking-widest uppercase mt-0.5">Multi-Drone Tactical Control</p>
                    </div>
                </div>

                <nav className="flex gap-1 bg-slate-100 p-1 rounded-lg">
                    {navItems.map(item => (
                        <NavLink
                            key={item.path}
                            to={item.path}
                            end={item.path === '/app'}
                            className={({ isActive }) => `px-4 py-2 rounded-md text-sm font-bold transition-all ${isActive ? 'bg-white text-accent-blue shadow-sm' : 'text-slate-500 hover:text-slate-700'}`}
                        >
                            {item.name}
                        </NavLink>
                    ))}
                </nav>


            </header>

            {/* Main Content Area */}
            <main className="flex-1 p-6">
                <Outlet />
            </main>
        </div>
    );
};

export default Layout;
