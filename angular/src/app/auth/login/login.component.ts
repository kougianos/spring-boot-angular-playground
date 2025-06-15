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
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {
    // Redirect to home if already logged in
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/home']);
    }

    this.loginForm = this.formBuilder.group({
      usernameOrEmail: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    // Get return url from route parameters or default to '/home'
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/home';
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
          // After login, fetch the user profile and then navigate
          this.authService.fetchUserProfile().subscribe({
            next: () => {
              this.router.navigate([this.returnUrl]);
            },
            error: (err) => {
              this.handleError(err);
            }
          });
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
