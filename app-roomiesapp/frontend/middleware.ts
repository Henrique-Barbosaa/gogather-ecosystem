import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

const publicRoutes: string[] = ['/', '/login', '/register'];

export function middleware(request: NextRequest): NextResponse {
  const { pathname } = request.nextUrl;

  const isPublicRoute = publicRoutes.includes(pathname);
  
  const hasToken = request.cookies.has('accessToken') || request.cookies.has('refreshToken');

  if (!isPublicRoute && !hasToken) {
    const loginUrl = new URL('/login', request.url);
    return NextResponse.redirect(loginUrl);
  }

  if ((pathname === '/login' || pathname === '/register') && hasToken) {
    const dashboardUrl = new URL('/area/dashboard', request.url);
    return NextResponse.redirect(dashboardUrl);
  }

  return NextResponse.next();
}

export const config = {
  matcher: ['/((?!api|_next/static|_next/image|favicon.ico).*)'],
};