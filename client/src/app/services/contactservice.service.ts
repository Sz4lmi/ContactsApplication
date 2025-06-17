import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Contactrequest } from '../models/contactrequest';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class ContactserviceService {
  private apiUrl = 'http://localhost:8080/api/contacts';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  // Get all contacts for the current user
  getAllContacts(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl).pipe(
      map(contacts => {
        const userId = this.authService.getUserId();
        // If we have a userId, filter contacts by user
        if (userId) {
          return contacts.filter(contact => contact.user && contact.user.id.toString() === userId);
        }
        return contacts;
      })
    );
  }

  // Create a new contact
  createContact(contactRequest: Contactrequest): Observable<any> {
    // The backend should associate the contact with the current user
    // based on the JWT token in the Authorization header
    return this.http.post<any>(this.apiUrl, contactRequest);
  }
}
