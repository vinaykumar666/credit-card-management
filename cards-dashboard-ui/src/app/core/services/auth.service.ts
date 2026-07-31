import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, RegisterRequest, TokenResponse } from '../models/api.models';

const ACCESS_TOKEN_KEY = 'accessToken';
const REFRESH_TOKEN_KEY = 'refreshToken';
const USER_ID_KEY = 'userId';
const EMAIL_KEY = 'email';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly authenticated = signal(this.hasToken());

  readonly isAuthenticated = this.authenticated.asReadonly();

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
  ) {}

  login(request: LoginRequest): Observable<TokenResponse> {
    return this.http
      .post<TokenResponse>(`${environment.authUrl}/api/v1/auth/login`, request)
      .pipe(tap((response) => this.persistSession(response)));
  }

  register(request: RegisterRequest): Observable<TokenResponse> {
    return this.http
      .post<TokenResponse>(`${environment.authUrl}/api/v1/auth/register`, request)
      .pipe(tap((response) => this.persistSession(response)));
  }

  logout(): void {
    sessionStorage.removeItem(ACCESS_TOKEN_KEY);
    sessionStorage.removeItem(REFRESH_TOKEN_KEY);
    sessionStorage.removeItem(USER_ID_KEY);
    sessionStorage.removeItem(EMAIL_KEY);
    this.authenticated.set(false);
    void this.router.navigate(['/login']);
  }

  getAccessToken(): string | null {
    return sessionStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getUserId(): string | null {
    return sessionStorage.getItem(USER_ID_KEY);
  }

  getEmail(): string | null {
    return sessionStorage.getItem(EMAIL_KEY);
  }

  private persistSession(response: TokenResponse): void {
    sessionStorage.setItem(ACCESS_TOKEN_KEY, response.accessToken);
    sessionStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken);
    sessionStorage.setItem(USER_ID_KEY, response.userId);
    sessionStorage.setItem(EMAIL_KEY, response.email);
    this.authenticated.set(true);
  }

  private hasToken(): boolean {
    return !!sessionStorage.getItem(ACCESS_TOKEN_KEY);
  }
}
