import axios, { InternalAxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
import { initialGroups, initialUser, initialFriends, TravelGroup, Expense, ChatMessage, RoadmapEvent } from './mockData';

// Create Axios Instance
export const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
  timeout: 4000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Attach JWT Token if available
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('tripmaker_token');
      if (token && config.headers) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// Response Interceptor: Handle 401 & propagate other errors
api.interceptors.response.use(
  (response: AxiosResponse) => response,
  async (error: AxiosError) => {
    // Check if unauthorized (401)
    if (error.response?.status === 401) {
      if (typeof window !== 'undefined') {
        localStorage.removeItem('tripmaker_token');
        localStorage.removeItem('tripmaker_user');
        // Redirect to login if not already there
        if (!window.location.pathname.includes('/login') && !window.location.pathname.includes('/register')) {
          window.location.href = '/login';
        }
      }
    }

    return Promise.reject(error);
  }
);
