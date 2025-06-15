import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable, of, map, catchError } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | Observable<boolean> {
    // First, check if the token exists
    if (!this.authService.isLoggedIn()) {
      this.redirectToLogin(state.url);
      return false;
    }
    
    // If we have a token but no user yet, try to load the user profile
    return this.authService.fetchUserProfile().pipe(
      map(() => true),
      catchError(() => {
        this.redirectToLogin(state.url);
        return of(false);
      })
    );
  }
  
  private redirectToLogin(url: string): void {
    this.router.navigate(['/login'], { 
      queryParams: { returnUrl: url }
    });
  }
}
