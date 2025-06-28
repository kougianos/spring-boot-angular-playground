import { Component, OnInit } from '@angular/core';
import { AuthService, UserInfo } from './core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  currentUser: UserInfo | null = null;

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    // Subscribe to user changes
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });

    // No need to manually load user profile here
    // It will be loaded by the AuthGuard when needed, or already available from JWT token
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
