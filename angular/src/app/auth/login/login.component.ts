import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  loading = false;
  returnUrl: string = '/home';
  hidePassword = true;
  error: string = '';
  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly authService: AuthService,
    private readonly snackBar: MatSnackBar
  ) {

    this.loginForm = this.formBuilder.group({
      usernameOrEmail: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });
  }
  ngOnInit(): void {
    // Redirect to home if already logged in
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/home']);
      return;
    }

    // Get return url from route parameters or default to '/home'
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] ?? '/home';
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    this.error = '';

    this.authService.login(this.loginForm.value)
      .subscribe({
        next: () => {
          // Login successful, navigate to return URL
          // User profile is already fetched by the login method
          this.router.navigate([this.returnUrl]);
        },
        error: (error) => {
          this.handleError(error);
        }
      });
  }

  private handleError(error: any): void {
    this.error = error.status === 401 
      ? 'Invalid username/email or password' 
      : 'Login failed. Please try again.';
    this.snackBar.open(this.error, 'Close', {
      duration: 5000,
      panelClass: ['error-snackbar']
    });
    this.loading = false;
  }
}
