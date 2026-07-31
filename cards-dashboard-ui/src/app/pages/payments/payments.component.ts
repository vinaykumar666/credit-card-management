import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Account, PaymentMethod, PaymentResponse } from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { BffService } from '../../core/services/bff.service';

@Component({
  selector: 'app-payments',
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe],
  templateUrl: './payments.component.html',
  styleUrl: './payments.component.scss',
})
export class PaymentsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly bff = inject(BffService);
  private readonly auth = inject(AuthService);

  readonly methods: PaymentMethod[] = ['CARD', 'UPI', 'NET_BANKING', 'EXTERNAL'];
  readonly accounts = signal<Account[]>([]);
  readonly loading = signal(true);
  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);
  readonly result = signal<PaymentResponse | null>(null);

  readonly form = this.fb.nonNullable.group({
    accountId: ['', Validators.required],
    amount: [0, [Validators.required, Validators.min(0.01)]],
    currency: ['USD', [Validators.required, Validators.minLength(3), Validators.maxLength(3)]],
    paymentMethod: ['CARD' as PaymentMethod, Validators.required],
  });

  ngOnInit(): void {
    this.bff.getAccounts().subscribe({
      next: (accounts) => {
        this.accounts.set(accounts ?? []);
        if (accounts?.length) {
          this.form.patchValue({
            accountId: accounts[0].id,
            currency: accounts[0].currency || 'USD',
          });
        }
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.message || 'Failed to load accounts for payment.');
        this.loading.set(false);
      },
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const userId = this.auth.getUserId();
    if (!userId) {
      this.error.set('Session missing user id. Please sign in again.');
      return;
    }

    this.submitting.set(true);
    this.error.set(null);
    this.result.set(null);

    const value = this.form.getRawValue();
    this.bff
      .initiatePayment({
        accountId: value.accountId,
        userId,
        amount: Number(value.amount),
        currency: value.currency.toUpperCase(),
        paymentMethod: value.paymentMethod,
      })
      .subscribe({
        next: (response) => {
          this.result.set(response);
          this.submitting.set(false);
        },
        error: (err) => {
          this.error.set(err?.error?.message || 'Payment initiation failed.');
          this.submitting.set(false);
        },
      });
  }
}
