import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {UserlistDTO} from '../models/userlistDTO';
import {UserrequestDTO} from '../models/userrequestDTO';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) { }

  /**
   * Get all users (admin only)
   * @returns Observable of all users (excluding admins)
   */
  getAllUsers(): Observable<UserlistDTO[]> {
    return this.http.get<UserlistDTO[]>(`${this.apiUrl}/users`).pipe(
      map(users => users.filter(user => user.role !== 'ROLE_ADMIN'))
    );
  }

  /**
   * Create a new user (admin only)
   * @param user User data
   * @returns Observable of the created user
   */
  createUser(user: UserrequestDTO): Observable<any> {
    return this.http.post(`${this.apiUrl}/users`, user);
  }

  /**
   * Update a user (admin only)
   * @param id User ID
   * @param user User data
   * @returns Observable of the updated user
   */
  updateUser(id: number, user: UserrequestDTO): Observable<any> {
    return this.http.put(`${this.apiUrl}/users/${id}`, user);
  }

  /**
   * Delete a user (admin only)
   * @param id User ID
   * @returns Observable of the response
   */
  deleteUser(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/users/${id}`, { responseType: 'text' });
  }
}
