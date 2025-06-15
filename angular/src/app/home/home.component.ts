import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService, UserInfo } from '../core/services/auth.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  currentUser: UserInfo | null = null;
    constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) { }
  ngOnInit(): void {
    // First check if we're logged in
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    
    // Then subscribe to user changes
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      
      // Only redirect if we've actually tried to load the user and failed
      if (user === null && !this.authService.isLoggedIn()) {
        this.router.navigate(['/login']);
      }
    });
    
    // If we have a token but no user data yet, try to load the profile
    if (this.authService.isLoggedIn() && !this.currentUser) {
      this.authService.loadUserProfile();
    }
  }
  
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
