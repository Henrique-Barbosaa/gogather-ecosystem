'use client';

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User, initialUser } from '../services/mockData';
import { useRouter } from 'next/navigation';
import { api } from '../services/api';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (token: string, userData: User) => void;
  logout: () => void;
  updateProfile: (updated: Partial<User>) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const router = useRouter();

  useEffect(() => {
    const token = localStorage.getItem('tripmaker_token');
    const savedUser = localStorage.getItem('tripmaker_user');
    if (token) {
      if (savedUser) {
        try {
          setUser(JSON.parse(savedUser));
        } catch (e) {
          // ignore
        }
      }
      api.get('/users/me')
        .then(res => {
          if (res.data) {
            setUser(res.data);
            localStorage.setItem('tripmaker_user', JSON.stringify(res.data));
          }
        })
        .catch(() => {
          // keep local or ignore
        })
        .finally(() => {
          setIsLoading(false);
        });
    } else {
      setUser(null);
      setIsLoading(false);
    }
  }, []);

  const login = (token: string, userData: User) => {
    localStorage.setItem('tripmaker_token', token);
    localStorage.setItem('tripmaker_user', JSON.stringify(userData));
    setUser(userData);
    router.push('/groups');
  };

  const logout = () => {
    localStorage.removeItem('tripmaker_token');
    localStorage.removeItem('tripmaker_user');
    localStorage.removeItem('tripmaker_notifications');
    localStorage.removeItem('tripmaker_muted_groups');
    localStorage.removeItem('tripmaker_friends');
    setUser(null);
    router.push('/login');
  };

  const updateProfile = (updated: Partial<User>) => {
    if (!user) return;
    const newUser = { ...user, ...updated };
    setUser(newUser);
    localStorage.setItem('tripmaker_user', JSON.stringify(newUser));
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        logout,
        updateProfile,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
