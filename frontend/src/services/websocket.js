import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketService {
    constructor() {
        this.client = null;
        this.callbacks = {}; // topic -> [callback]
    }

    connect(onConnect) {
        this.client = new Client({
            // Use SockJS fallback
            webSocketFactory: () => new SockJS('http://localhost:8080/ws-gcs'),
            onConnect: (frame) => {
                console.log('Connected: ' + frame);
                if (onConnect) onConnect();
                this._resubscribe();
            },
            onStompError: (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
            },
        });

        this.client.activate();
    }

    subscribe(topic, callback) {
        if (!this.callbacks[topic]) {
            this.callbacks[topic] = [];
        }
        this.callbacks[topic].push(callback);

        if (this.client && this.client.connected) {
            this.client.subscribe(topic, (message) => {
                callback(JSON.parse(message.body));
            });
        }
    }

    _resubscribe() {
        for (const topic in this.callbacks) {
            this.callbacks[topic].forEach(callback => {
                this.client.subscribe(topic, (message) => {
                    callback(JSON.parse(message.body));
                });
            });
        }
    }
}

const gcsWebSocket = new WebSocketService();
export default gcsWebSocket;
