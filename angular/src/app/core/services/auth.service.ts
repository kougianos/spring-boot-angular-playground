import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

// Models
export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface SignupRequest {
  username: string;
  email: string;
  password: string;
}

export interface JwtResponse {
  token: string;
  type: string;
}

export interface UserInfo {
  username: string;
  email: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly TOKEN_TYPE_KEY = 'auth_token_type';
  
  private readonly currentUserSubject: BehaviorSubject<UserInfo | null>;
  public currentUser$: Observable<UserInfo | null>;
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
    const currentUser = this.getUserFromStorage();
    this.currentUserSubject = new BehaviorSubject<UserInfo | null>(currentUser);
    this.currentUser$ = this.currentUserSubject.asObservable();
  }

  register(request: SignupRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/auth/signup`, request);
  }
  login(request: LoginRequest): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${this.apiUrl}/auth/login`, request)
      .pipe(
        tap(response => {
          this.storeToken(response);
          // Update user info from JWT token first for immediate UI update
          const userFromToken = this.getUserFromStorage();
          if (userFromToken) {
            this.currentUserSubject.next(userFromToken);
          }
          // Only fetch complete user profile if JWT doesn't have complete info
          // Let the home component handle the /me call to avoid duplicate requests
          // if (!this.hasCompleteUserInfo()) {
          //   this.isLoadingProfile = true;
          //   this.fetchUserProfile().subscribe({
          //     next: () => {
          //       this.isLoadingProfile = false;
          //     },
          //     error: () => {
          //       this.isLoadingProfile = false;
          //     }
          //   });
          // }
        })
      );
  }

  fetchUserProfile(): Observable<UserInfo> {
    return this.http.get<UserInfo>(`${this.apiUrl}/auth/me`)
      .pipe(
        tap(user => {
          this.currentUserSubject.next(user);
        })
      );
  }

  loadUserProfile(): void {
    if (this.isLoggedIn()) {
      this.fetchUserProfile().subscribe({
        error: (err) => {
          console.error('Failed to load user profile', err);
          this.logout();
        }
      });
    }
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.TOKEN_TYPE_KEY);
    this.currentUserSubject.next(null);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }
  getTokenType(): string {
    return localStorage.getItem(this.TOKEN_TYPE_KEY) ?? 'Bearer';
  }

  private storeToken(authResult: JwtResponse): void {
    localStorage.setItem(this.TOKEN_KEY, authResult.token);
    localStorage.setItem(this.TOKEN_TYPE_KEY, authResult.type);
  }

  private getUserFromStorage(): UserInfo | null {
    const token = this.getToken();
    if (!token) {
      return null;
    }

    try {
      // Try to extract user info from JWT token
      // This is a simplified approach
      const payload = JSON.parse(atob(token.split('.')[1]));
      
      // Try different possible field names for email
      const email = payload.email || payload.emailAddress || payload.mail || '';
      const username = payload.sub || payload.username || payload.user || '';
      
      return {
        username,
        email
      };
    } catch (e) {
      console.error('Failed to decode token', e);
      return null;
    }
  }

  hasCompleteUserInfo(): boolean {
    const currentUser = this.currentUserSubject.value;
    return !!(currentUser && currentUser.username && currentUser.email);
  }

  getUsername(): string | null {
    const currentUser = this.currentUserSubject.value;
    return currentUser?.username || null;
  }
}
