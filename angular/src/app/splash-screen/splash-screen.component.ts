import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-splash-screen',
  templateUrl: './splash-screen.component.html',
  styleUrls: ['./splash-screen.component.scss']
})
export class SplashScreenComponent implements OnInit {
  
  constructor(
    private router: Router,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    // After a delay, redirect to login or home based on auth state
    setTimeout(() => {
      if (this.authService.isLoggedIn()) {
        this.authService.loadUserProfile();
        this.router.navigate(['/home']);
      } else {
        this.router.navigate(['/login']);
      }
    }, 2000); // 2 seconds delay
  }
}
