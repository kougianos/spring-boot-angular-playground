import {Injectable} from '@angular/core';
import {Router, ActivatedRouteSnapshot, RouterStateSnapshot} from '@angular/router';
import {Observable, of, map, catchError} from 'rxjs';
import {AuthService} from '../services/auth.service';

@Injectable({
    providedIn: 'root'
})
export class AuthGuard {

    constructor(
        private readonly authService: AuthService,
        private readonly router: Router
    ) {
    }

    canActivate(
        route: ActivatedRouteSnapshot,
        state: RouterStateSnapshot
    ): boolean | Observable<boolean> {
        // First, check if the token exists
        if (!this.authService.isLoggedIn()) {
            this.redirectToLogin(state.url);
            return false;
        }

        // Check if we already have a user in the current user subject
        let userAvailable = false;
        this.authService.currentUser$.subscribe(user => {
            userAvailable = !!user;
        });

        // If user is already loaded, just return true
        if (userAvailable) {
            return true;
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
            queryParams: {returnUrl: url}
        });
    }
}
