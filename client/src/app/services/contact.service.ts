import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { ContactrequestDTO } from '../models/contactrequestDTO';
import { ContactlistDTO } from '../models/contactlistDTO';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class ContactService {
  private apiUrl = 'http://localhost:8080/api/contacts';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  // Get all contacts for the current user
  getAllContacts(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl).pipe(
      map(contacts => {
        // If user is admin, return all contacts
        if (this.authService.isAdmin()) {
          return contacts;
        }

        // Otherwise, filter contacts by user
        const userId = this.authService.getUserId();
        if (userId) {
          return contacts.filter(contact => contact.user && contact.user.id.toString() === userId);
        }
        return contacts;
      })
    );
  }

  // Get a contact by ID
  getContactById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  // Get contact list for the current user or all contacts for admin
  getContactList(): Observable<ContactlistDTO[]> {
    console.log('ContactService: getContactList - isAdmin =', this.authService.isAdmin());
    return this.http.get<ContactlistDTO[]>(`${this.apiUrl}/list`).pipe(
      map(contacts => {
        console.log('ContactService: getContactList response =', contacts);
        console.log('ContactService: getContactList response length =', contacts.length);
        return contacts;
      })
    );
  }

  // Create a new contact
  createContact(contactRequest: ContactrequestDTO): Observable<any> {
    // The backend should associate the contact with the current user
    // based on the JWT token in the Authorization header
    return this.http.post<any>(this.apiUrl, contactRequest);
  }

  // Update an existing contact
  updateContact(id: number, contactRequest: ContactrequestDTO): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, contactRequest);
  }

  // Delete a contact
  deleteContact(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
}
