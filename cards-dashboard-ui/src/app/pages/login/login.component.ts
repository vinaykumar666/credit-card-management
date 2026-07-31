import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { toUserMessage } from '../../core/utils/user-error';

/**
 * Login / register screen for AURUM.
 * Talks to the authentication service, then routes into the shell dashboard.
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly mode = signal<'login' | 'register'>('login');
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    fullName: [''],
    email: ['ada.lovelace@cards.local', [Validators.required, Validators.email]],
    password: ['Password123!', [Validators.required, Validators.minLength(8)]],
  });

  /** Switches between sign-in and register panels. */
  setMode(next: 'login' | 'register'): void {
    this.error.set(null);
    this.mode.set(next);
  }

  /** Fills the form with a documented seed user email. */
  fillDemo(email: string): void {
    this.form.patchValue({ email, password: 'Password123!' });
  }

  /** Submits login or register, then navigates to the dashboard on success. */
  submit(): void {
    const { email, password, fullName } = this.form.getRawValue();
    if (this.form.controls.email.invalid || this.form.controls.password.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (this.mode() === 'register' && !fullName.trim()) {
      this.error.set('Full name is required to register.');
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    const request$ =
      this.mode() === 'login'
        ? this.auth.login({ email, password })
        : this.auth.register({ email, password, fullName: fullName.trim() });

    request$.subscribe({
      next: () => {
        this.loading.set(false);
        void this.router.navigate(['/dashboard']);
      },
      error: (err: unknown) => {
        this.loading.set(false);
        this.error.set(
          toUserMessage(err, 'Sign-in did not work. Check your email and password, then try again.'),
        );
      },
    });
  }
}
