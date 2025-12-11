import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const gcsWebSocket = {
    client: null,
    callbacks: {},

    connect: (onConnected) => {
        // Spring Boot uses SockJS at /ws-gcs
        // Ensure we are pointing to the right place relative to current window
        const socket = new SockJS('/ws-gcs');
        const client = Stomp.over(socket);

        // Disable debug logs for cleaner console
        client.debug = () => { };

        client.connect({}, (frame) => {
            console.log('Connected: ' + frame);
            if (onConnected) onConnected();

            // Resubscribe to existing topics if reconnection happens
            Object.keys(gcsWebSocket.callbacks).forEach(topic => {
                client.subscribe(topic, (message) => {
                    const payload = JSON.parse(message.body);
                    gcsWebSocket.callbacks[topic](payload);
                });
            });

        }, (err) => {
            console.error('WebSocket Error:', err);
            // Reconnect logic could go here
        });

        gcsWebSocket.client = client;
    },

    subscribe: (topic, callback) => {
        if (!gcsWebSocket.callbacks) gcsWebSocket.callbacks = {};
        gcsWebSocket.callbacks[topic] = callback;

        if (gcsWebSocket.client && gcsWebSocket.client.connected) {
            gcsWebSocket.client.subscribe(topic, (message) => {
                callback(JSON.parse(message.body));
            });
        }
    }
};

export default gcsWebSocket;
