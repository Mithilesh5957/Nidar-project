import React, { useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix for default icon issues
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
    iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

// Custom Icons
const scoutIcon = new L.Icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-blue.png',
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
});

const deliveryIcon = new L.Icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
});

const missionIcon = new L.Icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
});

// Component to recenter map
const RecenterMap = ({ lat, lon }) => {
    const map = useMap();
    useEffect(() => {
        if (lat !== 0 && lon !== 0) {
            map.setView([lat, lon], map.getZoom());
        }
    }, [lat, lon, map]);
    return null;
};

const MapView = ({ vehicles, missions }) => {
    // Default center
    const center = [0, 0];

    return (
        <MapContainer center={[20, 78]} zoom={5} style={{ height: '100%', width: '100%' }}>
            <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution='&copy; OpenStreetMap contributors'
            />

            {/* Vehicles */}
            {vehicles.map(v => (
                <Marker
                    key={v.id}
                    position={[v.lat || 0, v.lon || 0]}
                    icon={v.type === 'SCOUT' ? scoutIcon : deliveryIcon}
                >
                    <Popup>
                        <strong>{v.id.toUpperCase()}</strong><br />
                        Alt: {v.alt}m<br />
                        Bat: {v.battery}%
                    </Popup>
                </Marker>
            ))}

            {/* Missions Paths */}
            {Object.entries(missions).map(([vid, items]) => {
                if (!items || items.length === 0) return null;
                const positions = items
                    .filter(i => i.command === 'WAYPOINT' || i.command === 'TAKEOFF') // Filter valid coords
                    .map(i => [i.lat, i.lon]);

                return (
                    <React.Fragment key={'mission-' + vid}>
                        <Polyline positions={positions} color={vid === 'scout' ? 'blue' : 'red'} dashArray="5, 10" />
                        {items.filter(i => i.command === 'WAYPOINT').map((i, idx) => (
                            <Marker key={idx} position={[i.lat, i.lon]} icon={missionIcon} opacity={0.6}>
                                <Popup>WP {i.seq}</Popup>
                            </Marker>
                        ))}
                    </React.Fragment>
                )
            })}
        </MapContainer>
    );
};

export default MapView;
