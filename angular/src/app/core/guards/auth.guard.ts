import {Injectable} from '@angular/core';
import {Router, ActivatedRouteSnapshot, RouterStateSnapshot} from '@angular/router';
import {Observable, of, map, catchError, take, switchMap} from 'rxjs';
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

        // Check if we already have user data to avoid unnecessary API calls
        return this.authService.currentUser$.pipe(
            take(1),
            switchMap(currentUser => {
                if (currentUser) {
                    // We have some user data, that's enough for authentication
                    return of(true);
                }
                // No user data at all, need to fetch it for authentication
                return this.authService.fetchUserProfile().pipe(
                    map(() => true),
                    catchError(() => {
                        this.redirectToLogin(state.url);
                        return of(false);
                    })
                );
            })
        );
    }

    private redirectToLogin(url: string): void {
        this.router.navigate(['/login'], {
            queryParams: {returnUrl: url}
        });
    }
}
