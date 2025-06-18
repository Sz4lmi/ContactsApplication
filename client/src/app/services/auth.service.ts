import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { LoggingService } from './logging.service';

interface LoginResponse {
  token: string;
  userId: number;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_ID_KEY = 'user_id';
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
  private apiUrl = 'http://localhost:8080/api/auth';

  isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, { username, password })
      .pipe(
        tap(response => {
          console.log('AuthService: login response =', response);
          this.setToken(response.token);

          // Store user ID from response
          this.setUserId(response.userId.toString());

          this.isAuthenticatedSubject.next(true);
        })
      );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_ID_KEY);
    this.isAuthenticatedSubject.next(false);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getUserId(): string | null {
    return localStorage.getItem(this.USER_ID_KEY);
  }

  getUserRole(): string | null {
    const token = this.getToken();
    if (!token) return null;

    const decodedToken = this.parseJwt(token);
    console.log('AuthService: token =', token);
    console.log('AuthService: decodedToken =', decodedToken);
    return decodedToken?.role || null;
  }

  isAdmin(): boolean {
    const role = this.getUserRole();
    const isAdmin = role === 'ROLE_ADMIN';
    console.log('AuthService: isAdmin check - role =', role, ', isAdmin =', isAdmin);
    return isAdmin;
  }

  private setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  private setUserId(userId: string): void {
    localStorage.setItem(this.USER_ID_KEY, userId);
  }

  private hasToken(): boolean {
    return !!this.getToken();
  }

  private parseJwt(token: string): any {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));
      return JSON.parse(jsonPayload);
    } catch (e) {
      return null;
    }
  }
}
