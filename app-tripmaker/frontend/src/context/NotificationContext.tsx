'use client';

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';

export interface AppNotification {
  id: string;
  groupId: string;
  groupTitle: string;
  type: 'rateio' | 'chat' | 'roadmap' | 'invite';
  title: string;
  message: string;
  timestamp: string;
  read: boolean;
}

interface NotificationContextType {
  notifications: AppNotification[];
  mutedGroupIds: string[];
  unreadCount: number;
  toggleMuteGroup: (groupId: string) => void;
  isGroupMuted: (groupId: string) => boolean;
  addNotification: (notif: Omit<AppNotification, 'id' | 'timestamp' | 'read'>) => void;
  markAllAsRead: () => void;
  markAsRead: (id: string) => void;
  clearAll: () => void;
}

const initialNotifications: AppNotification[] = [];

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const NotificationProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [notifications, setNotifications] = useState<AppNotification[]>([]);
  const [mutedGroupIds, setMutedGroupIds] = useState<string[]>([]);

  useEffect(() => {
    const savedNotifs = localStorage.getItem('tripmaker_notifications');
    const savedMuted = localStorage.getItem('tripmaker_muted_groups');
    if (savedNotifs) {
      try {
        const parsed: AppNotification[] = JSON.parse(savedNotifs);
        const cleanNotifs = parsed.filter(
          (n) => !['nt-1', 'nt-2', 'nt-3'].includes(n.id) && !['grp-1', 'grp-2', 'grp-3'].includes(n.groupId)
        );
        setNotifications(cleanNotifs);
        if (cleanNotifs.length !== parsed.length) {
          localStorage.setItem('tripmaker_notifications', JSON.stringify(cleanNotifs));
        }
      } catch (e) {
        setNotifications([]);
      }
    } else {
      setNotifications([]);
    }
    if (savedMuted) {
      try {
        const parsed: string[] = JSON.parse(savedMuted);
        const cleanMuted = parsed.filter((id) => !['grp-1', 'grp-2', 'grp-3'].includes(id));
        setMutedGroupIds(cleanMuted);
        if (cleanMuted.length !== parsed.length) {
          localStorage.setItem('tripmaker_muted_groups', JSON.stringify(cleanMuted));
        }
      } catch (e) {
        setMutedGroupIds([]);
      }
    } else {
      setMutedGroupIds([]);
    }
  }, []);

  const toggleMuteGroup = (groupId: string) => {
    setMutedGroupIds((prev) => {
      const isMuted = prev.includes(groupId);
      const updated = isMuted ? prev.filter((id) => id !== groupId) : [...prev, groupId];
      localStorage.setItem('tripmaker_muted_groups', JSON.stringify(updated));
      return updated;
    });
  };

  const isGroupMuted = (groupId: string): boolean => {
    return mutedGroupIds.includes(groupId);
  };

  const addNotification = (notif: Omit<AppNotification, 'id' | 'timestamp' | 'read'>) => {
    // If group is muted, we can either ignore or add it silently as read
    const isMuted = mutedGroupIds.includes(notif.groupId);
    const newNotif: AppNotification = {
      ...notif,
      id: `nt-${Date.now()}`,
      timestamp: 'Agora',
      read: isMuted, // automatically mark as read if muted so it won't trigger unread badge!
    };
    setNotifications((prev) => {
      const updated = [newNotif, ...prev];
      localStorage.setItem('tripmaker_notifications', JSON.stringify(updated));
      return updated;
    });
  };

  const markAllAsRead = () => {
    setNotifications((prev) => {
      const updated = prev.map((n) => ({ ...n, read: true }));
      localStorage.setItem('tripmaker_notifications', JSON.stringify(updated));
      return updated;
    });
  };

  const markAsRead = (id: string) => {
    setNotifications((prev) => {
      const updated = prev.map((n) => (n.id === id ? { ...n, read: true } : n));
      localStorage.setItem('tripmaker_notifications', JSON.stringify(updated));
      return updated;
    });
  };

  const clearAll = () => {
    setNotifications([]);
    localStorage.removeItem('tripmaker_notifications');
  };

  // Only count unread notifications belonging to UNMUTED groups!
  const unreadCount = notifications.filter((n) => !n.read && !mutedGroupIds.includes(n.groupId)).length;

  return (
    <NotificationContext.Provider
      value={{
        notifications,
        mutedGroupIds,
        unreadCount,
        toggleMuteGroup,
        isGroupMuted,
        addNotification,
        markAllAsRead,
        markAsRead,
        clearAll,
      }}
    >
      {children}
    </NotificationContext.Provider>
  );
};

export const useNotifications = (): NotificationContextType => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotifications must be used within a NotificationProvider');
  }
  return context;
};
