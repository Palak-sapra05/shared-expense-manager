import axios from 'axios';

// Use Vite env var when provided (VITE_API_BASE_URL), otherwise use relative paths
const baseURL = import.meta.env.VITE_API_BASE_URL || '';

const api = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export default api;
