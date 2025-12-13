import React, { useState } from 'react';
import { VehicleService } from '../services/api';

const VehicleControlPanel = ({ vehicleId, isArmed, currentMode }) => {
  const [targetAlt, setTargetAlt] = useState(10);
  const [loading, setLoading] = useState(false);

  const handleCommand = async (actionName, actionFn) => {
    if (!vehicleId) return;
    setLoading(true);
    try {
      await actionFn();
      console.log(`${actionName} command sent successfully`);
    } catch (error) {
      console.error(`Failed to send ${actionName}:`, error);
      alert(`Failed to send ${actionName}`);
    }
    setLoading(false);
  };

  const MODES = ['STABILIZE', 'GUIDED', 'AUTO', 'RTL', 'LAND', 'LOITER'];

  return (
    <div className="bg-gray-800 p-4 rounded-lg shadow-lg text-white">
      <h3 className="text-xl font-bold mb-4">Vehicle Control: {vehicleId || 'None'}</h3>

      <div className="grid grid-cols-2 gap-4 mb-4">
        <div className="bg-gray-700 p-3 rounded">
          <span className="text-gray-400 text-sm">Status</span>
          <div className={`text-lg font-bold ${isArmed ? 'text-red-500' : 'text-green-500'}`}>
            {isArmed ? 'ARMED' : 'DISARMED'}
          </div>
        </div>
        <div className="bg-gray-700 p-3 rounded">
          <span className="text-gray-400 text-sm">Mode</span>
          <div className="text-lg font-bold text-blue-400">{currentMode || 'UNKNOWN'}</div>
        </div>
      </div>

      <div className="space-y-3">
        <div className="flex gap-2">
          <button
            onClick={() => handleCommand('Arm', () => VehicleService.arm(vehicleId))}
            disabled={isArmed || loading}
            className={`flex-1 py-2 rounded font-bold transition ${isArmed ? 'bg-gray-600 cursor-not-allowed' : 'bg-red-600 hover:bg-red-700'}`}
          >
            ARM
          </button>
          <button
            onClick={() => handleCommand('Disarm', () => VehicleService.disarm(vehicleId))}
            disabled={!isArmed || loading}
            className={`flex-1 py-2 rounded font-bold transition ${!isArmed ? 'bg-gray-600 cursor-not-allowed' : 'bg-green-600 hover:bg-green-700'}`}
          >
            DISARM
          </button>
        </div>

        <div className="flex gap-2 items-center">
          <input
            type="number"
            value={targetAlt}
            onChange={(e) => setTargetAlt(e.target.value)}
            className="bg-gray-900 text-white p-2 rounded w-20 text-center"
            min="1"
          />
          <button
            onClick={() => handleCommand('Takeoff', () => VehicleService.takeoff(vehicleId, targetAlt))}
            disabled={!isArmed || loading}
            className="flex-1 bg-yellow-600 hover:bg-yellow-700 py-2 rounded font-bold"
          >
            TAKEOFF (m)
          </button>
        </div>

        <div className="grid grid-cols-2 gap-2">
          <button
            onClick={() => handleCommand('RTL', () => VehicleService.rtl(vehicleId))}
            className="bg-purple-600 hover:bg-purple-700 py-2 rounded font-bold"
          >
            RTL
          </button>
          <button
            onClick={() => handleCommand('Stream', () => VehicleService.requestStream(vehicleId))}
            className="bg-blue-600 hover:bg-blue-700 py-2 rounded font-bold"
          >
            REQ STREAM
          </button>
        </div>

        <div className="mt-4">
          <label className="text-sm text-gray-400">Set Flight Mode</label>
          <select
            onChange={(e) => handleCommand(`Set Mode ${e.target.value}`, () => VehicleService.setMode(vehicleId, e.target.value))}
            className="w-full bg-gray-900 border border-gray-700 rounded p-2 mt-1"
            value={currentMode || ''}
          >
            <option value="" disabled>Select Mode</option>
            {MODES.map(m => (
              <option key={m} value={m}>{m}</option>
            ))}
          </select>
        </div>
      </div>
    </div>
  );
};

export default VehicleControlPanel;
