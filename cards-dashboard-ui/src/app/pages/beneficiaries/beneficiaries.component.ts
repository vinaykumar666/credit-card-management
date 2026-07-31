import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Beneficiary, BeneficiaryType } from '../../core/models/api.models';
import { BffService } from '../../core/services/bff.service';
import { toUserMessage } from '../../core/utils/user-error';

@Component({
  selector: 'app-beneficiaries',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './beneficiaries.component.html',
  styleUrl: './beneficiaries.component.scss',
})
export class BeneficiariesComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly bff = inject(BffService);

  readonly types: BeneficiaryType[] = ['PERSON', 'MERCHANT', 'INTERNAL'];
  readonly beneficiaries = signal<Beneficiary[]>([]);
  readonly loading = signal(true);
  readonly submitting = signal(false);
  readonly deactivatingId = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    nickname: ['', [Validators.required, Validators.maxLength(100)]],
    beneficiaryName: ['', [Validators.required, Validators.maxLength(255)]],
    accountNumber: ['', [Validators.required, Validators.maxLength(34)]],
    bankName: ['', [Validators.required, Validators.maxLength(255)]],
    ifscOrRouting: ['', [Validators.required, Validators.maxLength(32)]],
    beneficiaryType: ['PERSON' as BeneficiaryType, Validators.required],
  });

  ngOnInit(): void {
    this.loadBeneficiaries();
  }

  loadBeneficiaries(): void {
    this.loading.set(true);
    this.error.set(null);
    this.bff.getBeneficiaries(true).subscribe({
      next: (list) => {
        this.beneficiaries.set(list ?? []);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(toUserMessage(err, 'We could not load your payees. Please try again.'));
        this.loading.set(false);
      },
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.error.set(null);
    this.success.set(null);

    this.bff.createBeneficiary(this.form.getRawValue()).subscribe({
      next: () => {
        this.success.set('Payee added successfully.');
        this.form.reset({
          nickname: '',
          beneficiaryName: '',
          accountNumber: '',
          bankName: '',
          ifscOrRouting: '',
          beneficiaryType: 'PERSON',
        });
        this.submitting.set(false);
        this.loadBeneficiaries();
      },
      error: (err) => {
        this.error.set(toUserMessage(err, 'We could not save this payee. Please check the details and try again.'));
        this.submitting.set(false);
      },
    });
  }

  deactivate(id: string): void {
    this.deactivatingId.set(id);
    this.error.set(null);
    this.success.set(null);

    this.bff.deactivateBeneficiary(id).subscribe({
      next: () => {
        this.success.set('Payee deactivated.');
        this.deactivatingId.set(null);
        this.loadBeneficiaries();
      },
      error: (err) => {
        this.error.set(toUserMessage(err, 'We could not remove this payee. Please try again.'));
        this.deactivatingId.set(null);
      },
    });
  }

  maskAccount(accountNumber: string): string {
    if (!accountNumber || accountNumber.length < 4) {
      return accountNumber || '—';
    }
    return `•••• ${accountNumber.slice(-4)}`;
  }
}
