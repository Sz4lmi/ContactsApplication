import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: false,
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnInit {
  isAuthenticated = false;
  isAdmin = false;
  username: string | null = null;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    // Subscribe to authentication state changes
    this.authService.isAuthenticated$.subscribe(
      isAuthenticated => {
        this.isAuthenticated = isAuthenticated;
        // Check if user is admin
        this.isAdmin = this.authService.isAdmin();
        // Get username if authenticated
        if (isAuthenticated) {
          this.username = this.authService.getUsername();
        } else {
          this.username = null;
        }
      }
    );
  }

  logout(): void {
    this.authService.logout();
  }
}
