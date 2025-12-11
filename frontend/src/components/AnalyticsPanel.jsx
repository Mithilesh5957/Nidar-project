import React from 'react';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend,
} from 'chart.js';
import { Line } from 'react-chartjs-2';

ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend
);

const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
        legend: {
            position: 'top',
            align: 'end',
            labels: {
                color: '#64748b', // slate-500
                boxWidth: 8,
                usePointStyle: true,
                font: { family: 'Space Grotesk', size: 10 }
            }
        },
        title: { display: false },
    },
    scales: {
        x: {
            grid: { display: false }, // Cleaner look
            ticks: { color: '#94a3b8', font: { size: 9 } }
        },
        y: {
            grid: { color: '#f1f5f9', borderDash: [4, 4] },
            ticks: { color: '#94a3b8', font: { size: 9 } }
        }
    },
    elements: {
        line: { tension: 0.4, borderWidth: 2 },
        point: { radius: 0, hoverRadius: 4 }
    }
};

const AnalyticsPanel = ({ vehicles }) => {
    const labels = ['-60s', '-50s', '-40s', '-30s', '-20s', '-10s', 'Now'];

    const getMockData = (veh) => {
        if (!veh) return [100, 99, 98, 97, 96, 95, 95];
        return Array(7).fill(0).map((_, i) => Math.min(100, veh.battery + (i - 6)));
    };

    const scout = vehicles.find(v => v.id === 'scout');
    const delivery = vehicles.find(v => v.id === 'delivery');

    const data = {
        labels,
        datasets: [
            {
                label: 'SCOUT',
                data: getMockData(scout),
                borderColor: '#2563eb', // accent-blue
                backgroundColor: 'transparent',
            },
            {
                label: 'DELIVERY',
                data: getMockData(delivery),
                borderColor: '#64748b', // slate-500
                backgroundColor: 'transparent',
            },
        ],
    };

    return (
        <div className="h-48 bg-white border border-slate-200 rounded-lg p-4 shadow-soft">
            <h3 className="text-sm font-semibold text-slate-500 uppercase tracking-wider mb-2">Battery Trend</h3>
            <div className="h-32">
                <Line options={options} data={data} />
            </div>
        </div>
    );
};

export default AnalyticsPanel;
