import React from 'react';
import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';

const Home = () => {
    return (
        <div className="min-h-screen bg-slate-50 text-slate-900 font-display overflow-x-hidden flex flex-col">
            {/* Navigation */}
            <nav className="fixed top-0 w-full z-50 bg-white/80 backdrop-blur-md border-b border-slate-200">
                <div className="max-w-7xl mx-auto px-6 py-4 flex justify-between items-center">
                    <div className="flex items-center gap-3">
                        <div className="w-8 h-8 bg-slate-900 rounded-lg flex items-center justify-center text-white font-bold text-lg shadow-soft">N</div>
                        <span className="font-bold text-xl tracking-tight text-slate-900">NIDAR <span className="text-slate-400 font-light">GCS</span></span>
                    </div>
                    <div className="hidden md:flex gap-8 text-sm font-bold text-slate-500">
                        <a href="#about" className="hover:text-slate-900 transition-colors">About</a>
                        <a href="#features" className="hover:text-slate-900 transition-colors">Features</a>
                        <a href="#docs" className="hover:text-slate-900 transition-colors">Docs</a>
                    </div>
                    <Link to="/app" className="bg-slate-900 hover:bg-slate-800 text-white px-5 py-2 rounded-lg text-sm font-bold transition-all shadow-soft active:translate-y-0.5">
                        Launch Dashboard
                    </Link>
                </div>
            </nav>

            {/* Hero Section */}
            <section className="relative pt-40 pb-20 px-6 flex-1 flex items-center justify-center overflow-hidden">
                {/* Abstract Background Elements */}
                <div className="absolute top-0 left-0 w-full h-full -z-10 overflow-hidden">
                    <div className="absolute top-[-10%] right-[-5%] w-[600px] h-[600px] bg-accent-blue/5 rounded-full blur-[100px]" />
                    <div className="absolute bottom-[-10%] left-[-5%] w-[500px] h-[500px] bg-slate-200/40 rounded-full blur-[100px]" />
                </div>

                <div className="max-w-5xl mx-auto text-center relative z-10">
                    <motion.div
                        initial={{ opacity: 0, y: 20 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.8 }}
                    >
                        <span className="inline-block py-1.5 px-4 rounded-full bg-white border border-slate-200 text-slate-500 text-xs font-bold tracking-widest uppercase mb-8 shadow-sm">
                            Next-Gen Drone Command System
                        </span>
                        <h1 className="text-5xl md:text-7xl font-bold leading-tight mb-8 text-slate-900 tracking-tight">
                            Command Your Fleet <br />
                            <span className="text-accent-blue">With Precision.</span>
                        </h1>
                        <p className="text-lg md:text-xl text-slate-500 mb-10 max-w-2xl mx-auto leading-relaxed font-medium">
                            The advanced Ground Control Station for autonomous multi-drone operations. Planning, Telemetry, and AI Intelligence in one interface.
                        </p>

                        <div className="flex flex-col md:flex-row gap-4 justify-center">
                            <Link to="/app" className="bg-accent-blue hover:bg-blue-600 text-white px-8 py-4 rounded-xl font-bold hover:-translate-y-1 transition-all shadow-soft text-lg">
                                Enter Dashboard
                            </Link>
                            <a href="#docs" className="bg-white border border-slate-200 text-slate-700 px-8 py-4 rounded-xl font-bold hover:bg-slate-50 hover:-translate-y-1 transition-all shadow-soft text-lg">
                                View Documentation
                            </a>
                        </div>
                    </motion.div>

                    {/* Quick Stats Grid Mockup */}
                    <motion.div
                        initial={{ opacity: 0, y: 40 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ delay: 0.4, duration: 0.8 }}
                        className="mt-20 grid grid-cols-2 md:grid-cols-4 gap-4 max-w-4xl mx-auto"
                    >
                        {[
                            { label: "Active Drones", value: "3", color: "text-green-500" },
                            { label: "Missions Flown", value: "128", color: "text-blue-500" },
                            { label: "Targets Found", value: "842", color: "text-purple-500" },
                            { label: "System Status", value: "ONLINE", color: "text-slate-900" }
                        ].map((stat, i) => (
                            <div key={i} className="bg-white p-6 rounded-2xl shadow-soft border border-slate-100 flex flex-col items-center justify-center">
                                <span className={`text-3xl font-bold ${stat.color} mb-1`}>{stat.value}</span>
                                <span className="text-xs font-bold uppercase text-slate-400 tracking-wider">{stat.label}</span>
                            </div>
                        ))}
                    </motion.div>
                </div>
            </section>

            {/* Features Section */}
            <section id="features" className="py-24 px-6 bg-white border-t border-slate-200">
                <div className="max-w-7xl mx-auto">
                    <div className="text-center mb-16">
                        <h2 className="text-3xl font-bold mb-4 text-slate-900">Mission Critical Capabilities</h2>
                        <p className="text-slate-500 font-medium">Engineered for reliability in high-stakes environments.</p>
                    </div>

                    <div className="grid md:grid-cols-3 gap-8">
                        {[
                            { title: "Mission Planning", desc: "Intuitive drag-and-drop waypoint editor. Create complex flight paths in seconds.", icon: "🗺️" },
                            { title: "Live Telemetry", desc: "Real-time monitoring of battery, GPS, altitude, and velocity with sub-second latency.", icon: "📡" },
                            { title: "AI Intelligence", desc: "Automatic target detection and classification using onboard edge computing.", icon: "👁️" }
                        ].map((feature, i) => (
                            <motion.div
                                key={i}
                                whileHover={{ y: -5 }}
                                className="bg-slate-50 p-8 rounded-2xl border border-slate-200 hover:shadow-soft transition-all"
                            >
                                <div className="text-4xl mb-6 bg-white w-16 h-16 rounded-xl flex items-center justify-center shadow-sm border border-slate-100">{feature.icon}</div>
                                <h3 className="text-xl font-bold mb-3 text-slate-900">{feature.title}</h3>
                                <p className="text-slate-500 leading-relaxed font-medium text-sm">{feature.desc}</p>
                            </motion.div>
                        ))}
                    </div>
                </div>
            </section>

            {/* Footer */}
            <footer className="py-12 px-6 border-t border-slate-200 bg-slate-50 text-center">
                <p className="text-slate-400 text-sm font-bold">
                    &copy; 2025 NIDAR Systems. High Precision GCS.
                </p>
            </footer>
        </div>
    );
};

export default Home;
