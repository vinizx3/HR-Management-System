import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse, TokenPayload } from '../models/auth.model';


@Injectable({
  providedIn: 'root'
  })

export class AuthService {

  private readonly TOKEN_KEY = 'auth_token';

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', request).pipe(
      tap(response => {
        localStorage.setItem(this.TOKEN_KEY, response.token);
        })
      );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this.router.navigate(['login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token ) return false;

    try {
      const payload = this.decodeToken(token);
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  getRole(): string | null {
    const token = this.getToken();
    if (!token) return null;
    
    try {
      return this.decodeToken(token).role;
    } catch {
      return null;
    }
  }

  getEmail(): string | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      return this.decodeToken(token).sub;
    } catch {
      return null;
    }
  }

  isHrManager(): boolean {
  return this.getRole() === 'HR_MANAGER' || this.getRole() === 'DEMO_ADMIN';
  }

  isDemoAccount(): boolean {
  const role = this.getRole();
  return role === 'DEMO_ADMIN' || role === 'DEMO_EMPLOYEE';
}

  private decodeToken(token: string): TokenPayload {
    const payload = token.split('.')[1];
    return JSON.parse(atob(payload));
  }
}