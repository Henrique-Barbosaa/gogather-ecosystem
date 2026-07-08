"use client";

import { createContext, useContext, useEffect, useState, ReactNode } from "react";
import { useRouter, usePathname } from "next/navigation";

export interface User {
  id: number;
  username: string;
  displayName?: string;
  email: string;
}

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const router = useRouter();
  const pathname = usePathname();
  const isPublicRoute = pathname === "/" || pathname === "/login" || pathname === "/register";

  useEffect(() => {
    const verifyUser = async (): Promise<void> => {
      setIsLoading(true);

      try {
        const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

        const response = await fetch(`${apiUrl}/auth/verify`, {
          method: "GET",
          credentials: "include",
        });

        if (response.ok) {
          const userData = (await response.json()) as User;
          setUser(userData);
        } else {
          setUser(null);

          if (!isPublicRoute) {
            router.replace("/login");
          }
        }
      } catch (error) {
        console.error("Erro na autenticação:", error);
        setUser(null);

        if (!isPublicRoute) {
          router.replace("/login");
        }
      } finally {
        setIsLoading(false);
      }
    };

    verifyUser();
  }, [pathname, isPublicRoute, router]);

  const logout = async (): Promise<void> => {
    try {
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

      await fetch(`${apiUrl}/auth/logout`, {
        method: "POST",
        credentials: "include",
      });

      setUser(null);
      router.push("/login");
    } catch (error) {
      console.error("Erro ao fazer logout", error);
    }
  };

  const contextValue: AuthContextType = {
    user,
    isLoading,
    logout,
  };

  return <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth deve ser usado dentro de um AuthProvider");
  }

  return context;
};
