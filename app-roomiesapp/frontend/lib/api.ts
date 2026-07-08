import axios from 'axios';

export const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
  timeout: 10000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
	if (error.response?.status === 401) {
      const isVerifyRoute = error.config?.url?.includes('/auth/verify');
      
      if (!isVerifyRoute && typeof window !== 'undefined') {
        console.log('Sessão expirada ou não autorizado. Redirecionando para login...');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);
