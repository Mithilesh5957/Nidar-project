import axios from 'axios';

const api = axios.create({
    baseURL: '/api', // Proxied by Nginx to :8000/api
    headers: {
        'Content-Type': 'application/json',
    },
});

export default api;
