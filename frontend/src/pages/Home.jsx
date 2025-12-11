import React from 'react';
import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';

const Home = () => {
    return (
        <div className="min-h-screen bg-slate-900 text-white font-display overflow-x-hidden">
            {/* Navigation */}
            <nav className="fixed top-0 w-full z-50 bg-slate-900/80 backdrop-blur-md border-b border-white/10">
                <div className="max-w-7xl mx-auto px-6 py-4 flex justify-between items-center">
                    <div className="flex items-center gap-3">
                        <div className="w-8 h-8 bg-blue-500 rounded flex items-center justify-center font-bold text-lg">N</div>
                        <span className="font-bold text-xl tracking-tight">NIDAR <span className="text-blue-500">SYSTEMS</span></span>
                    </div>
                    <div className="hidden md:flex gap-8 text-sm font-medium text-slate-300">
                        <a href="#about" className="hover:text-white transition-colors">About</a>
                        <a href="#works" className="hover:text-white transition-colors">Works</a>
                        <a href="#solutions" className="hover:text-white transition-colors">Solutions</a>
                    </div>
                    <Link to="/app" className="bg-blue-600 hover:bg-blue-500 text-white px-5 py-2 rounded-full text-sm font-bold transition-all shadow-lg shadow-blue-500/20">
                        Launch GCS
                    </Link>
                </div>
            </nav>

            {/* Hero Section */}
            <section className="relative pt-32 pb-20 px-6 min-h-screen flex items-center justify-center overflow-hidden">
                {/* Abstract Background */}
                <div className="absolute top-0 left-0 w-full h-full">
                    <div className="absolute top-[-20%] right-[-10%] w-[600px] h-[600px] bg-blue-600/20 rounded-full blur-[120px]" />
                    <div className="absolute bottom-[-20%] left-[-10%] w-[500px] h-[500px] bg-purple-600/10 rounded-full blur-[100px]" />
                </div>

                <div className="max-w-5xl mx-auto text-center relative z-10">
                    <motion.div
                        initial={{ opacity: 0, y: 20 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.8 }}
                    >
                        <span className="inline-block py-1 px-3 rounded-full bg-blue-500/10 text-blue-400 text-xs font-bold tracking-widest uppercase mb-6 border border-blue-500/20">
                            Autonomous Aerial Surveillance
                        </span>
                        <h1 className="text-5xl md:text-7xl font-bold leading-tight mb-6">
                            The Future of <br />
                            <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-purple-400">Tactical Drone Operations</span>
                        </h1>
                        <p className="text-lg md:text-xl text-slate-400 mb-10 max-w-2xl mx-auto leading-relaxed">
                            Plan complex missions, execute autonomous flights, and analyze real-time intelligence with the NIDAR Ground Control System.
                        </p>

                        <div className="flex flex-col md:flex-row gap-4 justify-center">
                            <Link to="/app" className="bg-white text-slate-900 px-8 py-3.5 rounded-full font-bold hover:scale-105 transition-transform shadow-xl">
                                Enter Dashboard
                            </Link>
                            <a href="#works" className="bg-white/5 border border-white/10 text-white px-8 py-3.5 rounded-full font-bold hover:bg-white/10 transition-all backdrop-blur-sm">
                                View Documentation
                            </a>
                        </div>
                    </motion.div>

                    {/* Hero UI Mockup */}
                    <motion.div
                        initial={{ opacity: 0, y: 40 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ delay: 0.4, duration: 0.8 }}
                        className="mt-20 relative"
                    >
                        <div className="bg-slate-800 rounded-xl p-2 border border-white/10 shadow-2xl overflow-hidden">
                            <div className="aspect-[16/9] bg-slate-900 rounded-lg overflow-hidden relative group">
                                {/* Fake Map */}
                                <div className="absolute inset-0 bg-[#0f172a] opacity-50"></div>
                                <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 text-slate-600 font-mono text-sm">
                                    [ SYSTEM PREVIEW ]
                                </div>
                            </div>
                        </div>
                    </motion.div>
                </div>
            </section>

            {/* Features Grid */}
            <section id="works" className="py-24 px-6 bg-slate-900/50">
                <div className="max-w-7xl mx-auto">
                    <div className="text-center mb-16">
                        <h2 className="text-3xl font-bold mb-4">Advanced Capabilities</h2>
                        <p className="text-slate-400">Engineered for autonomous precision and reliability.</p>
                    </div>

                    <div className="grid md:grid-cols-3 gap-8">
                        {[
                            { title: "Mission Planning", desc: "Intuitive waypoint editor with drag-and-drop interface for complex flight paths.", icon: "🗺️" },
                            { title: "Real-time Telemetry", desc: "Live streaming of battery, altitude, and GPS data over low-latency connection.", icon: "📡" },
                            { title: "Visual Intelligence", desc: "Instant detection analysis and automatic mission dispatch for target interception.", icon: "👁️" }
                        ].map((feature, i) => (
                            <motion.div
                                key={i}
                                whileHover={{ y: -5 }}
                                className="bg-white/5 border border-white/10 p-8 rounded-2xl hover:bg-white/10 transition-all"
                            >
                                <div className="text-4xl mb-4">{feature.icon}</div>
                                <h3 className="text-xl font-bold mb-3">{feature.title}</h3>
                                <p className="text-slate-400 leading-relaxed text-sm">{feature.desc}</p>
                            </motion.div>
                        ))}
                    </div>
                </div>
            </section>

            {/* Footer */}
            <footer className="py-12 px-6 border-t border-white/5 text-center text-slate-500 text-sm">
                <p>&copy; 2025 NIDAR Systems. All rights reserved.</p>
            </footer>
        </div>
    );
};

export default Home;
