import React, { useState } from 'react';
import { VehicleService } from '../services/api';

const MissionUploader = ({ vehicleId }) => {
  const [jsonInput, setJsonInput] = useState('');
  const [uploading, setUploading] = useState(false);

  const handleUpload = async () => {
    if (!vehicleId) return;
    try {
      const mission = JSON.parse(jsonInput);
      if (!Array.isArray(mission)) {
        alert("Mission must be a JSON array of items.");
        return;
      }
      setUploading(true);
      await VehicleService.uploadMission(vehicleId, mission);
      alert("Mission uploaded successfully!");
      setJsonInput('');
    } catch (e) {
      console.error(e);
      alert("Failed to upload: " + (e.message || "Unknown error"));
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="bg-gray-800 p-4 rounded-lg shadow-lg text-white mt-4">
      <h3 className="text-xl font-bold mb-4">Mission Uploader</h3>
      <textarea
        className="w-full h-32 bg-gray-900 border border-gray-700 rounded p-2 text-sm font-mono text-green-400"
        placeholder='[{"seq":0, "frame":3, "command":16, "x":-35.36, "y":149.16, "z":20}]'
        value={jsonInput}
        onChange={(e) => setJsonInput(e.target.value)}
      />
      <button
        onClick={handleUpload}
        disabled={uploading || !vehicleId}
        className="w-full mt-2 bg-indigo-600 hover:bg-indigo-700 py-2 rounded font-bold disabled:bg-gray-600"
      >
        {uploading ? 'Uploading...' : 'Upload Mission (JSON)'}
      </button>
    </div>
  );
};

export default MissionUploader;
