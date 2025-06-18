import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UserDTO {
  username: string;
  password: string;
  role?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) { }

  /**
   * Create a new user (admin only)
   * @param user User data
   * @returns Observable of the created user
   */
  createUser(user: UserDTO): Observable<any> {
    return this.http.post(`${this.apiUrl}/users`, user);
  }
}
