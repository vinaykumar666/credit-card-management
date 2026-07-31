import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  Account,
  Beneficiary,
  PaymentMethod,
  PaymentResponse,
} from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { BffService } from '../../core/services/bff.service';

type PaymentsTab = 'transfer' | 'bill' | 'card' | 'history';

@Component({
  selector: 'app-payments',
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe, DatePipe],
  templateUrl: './payments.component.html',
  styleUrl: './payments.component.scss',
})
export class PaymentsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly bff = inject(BffService);
  private readonly auth = inject(AuthService);

  readonly methods: PaymentMethod[] = ['CARD', 'UPI', 'NET_BANKING', 'EXTERNAL'];
  readonly tabs: { id: PaymentsTab; label: string }[] = [
    { id: 'transfer', label: 'Transfer' },
    { id: 'bill', label: 'Bill pay' },
    { id: 'card', label: 'Card settle' },
    { id: 'history', label: 'History' },
  ];

  readonly activeTab = signal<PaymentsTab>('transfer');
  readonly accounts = signal<Account[]>([]);
  readonly beneficiaries = signal<Beneficiary[]>([]);
  readonly history = signal<PaymentResponse[]>([]);
  readonly loading = signal(true);
  readonly historyLoading = signal(false);
  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);
  readonly result = signal<PaymentResponse | null>(null);

  readonly personBeneficiaries = computed(() =>
    this.beneficiaries().filter((b) => b.beneficiaryType === 'PERSON'),
  );

  readonly merchantBeneficiaries = computed(() =>
    this.beneficiaries().filter((b) => b.beneficiaryType === 'MERCHANT'),
  );

  readonly transferBeneficiaries = computed(() => {
    const persons = this.personBeneficiaries();
    return persons.length ? persons : this.beneficiaries();
  });

  readonly transferForm = this.fb.nonNullable.group({
    accountId: ['', Validators.required],
    beneficiaryId: ['', Validators.required],
    amount: [0, [Validators.required, Validators.min(0.01)]],
    currency: ['USD', [Validators.required, Validators.minLength(3), Validators.maxLength(3)]],
    paymentMethod: ['UPI' as PaymentMethod, Validators.required],
    remarks: [''],
  });

  readonly billForm = this.fb.nonNullable.group({
    accountId: ['', Validators.required],
    beneficiaryId: [''],
    useOneTime: [false],
    payeeName: [''],
    payeeAccountNumber: [''],
    payeeBankName: [''],
    payeeIfscOrRouting: [''],
    amount: [0, [Validators.required, Validators.min(0.01)]],
    currency: ['USD', [Validators.required, Validators.minLength(3), Validators.maxLength(3)]],
    paymentMethod: ['UPI' as PaymentMethod, Validators.required],
    billReference: [''],
    remarks: [''],
  });

  readonly cardForm = this.fb.nonNullable.group({
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
          const defaults = {
            accountId: accounts[0].id,
            currency: accounts[0].currency || 'USD',
          };
          this.transferForm.patchValue(defaults);
          this.billForm.patchValue(defaults);
          this.cardForm.patchValue(defaults);
        }
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.message || 'Failed to load accounts for payment.');
        this.loading.set(false);
      },
    });

    this.bff.getBeneficiaries(true).subscribe({
      next: (list) => {
        this.beneficiaries.set(list ?? []);
        const transferList = list?.filter((b) => b.beneficiaryType === 'PERSON') ?? [];
        const preferred = transferList.length ? transferList : list ?? [];
        if (preferred.length) {
          this.transferForm.patchValue({ beneficiaryId: preferred[0].id });
        }
        const merchants = list?.filter((b) => b.beneficiaryType === 'MERCHANT') ?? [];
        if (merchants.length) {
          this.billForm.patchValue({ beneficiaryId: merchants[0].id });
        }
      },
      error: (err) => {
        this.error.set(err?.error?.message || 'Failed to load payees.');
      },
    });
  }

  setTab(tab: PaymentsTab): void {
    this.activeTab.set(tab);
    this.error.set(null);
    this.result.set(null);
    if (tab === 'history') {
      this.loadHistory();
    }
  }

  loadHistory(): void {
    this.historyLoading.set(true);
    this.bff.paymentHistory().subscribe({
      next: (rows) => {
        this.history.set(rows ?? []);
        this.historyLoading.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.message || 'Failed to load payment history.');
        this.historyLoading.set(false);
      },
    });
  }

  submitTransfer(): void {
    if (this.transferForm.invalid) {
      this.transferForm.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.error.set(null);
    this.result.set(null);

    const value = this.transferForm.getRawValue();
    this.bff
      .transferMoney({
        accountId: value.accountId,
        beneficiaryId: value.beneficiaryId,
        amount: Number(value.amount),
        currency: value.currency.toUpperCase(),
        paymentMethod: value.paymentMethod,
        remarks: value.remarks || undefined,
      })
      .subscribe({
        next: (response) => {
          this.result.set(response);
          this.submitting.set(false);
        },
        error: (err) => {
          this.error.set(err?.error?.message || 'Transfer failed.');
          this.submitting.set(false);
        },
      });
  }

  submitBill(): void {
    const value = this.billForm.getRawValue();
    const useOneTime = value.useOneTime;

    if (useOneTime) {
      this.billForm.controls.payeeName.setValidators([Validators.required]);
      this.billForm.controls.payeeAccountNumber.setValidators([Validators.required]);
      this.billForm.controls.payeeBankName.setValidators([Validators.required]);
      this.billForm.controls.payeeIfscOrRouting.setValidators([Validators.required]);
      this.billForm.controls.beneficiaryId.clearValidators();
    } else {
      this.billForm.controls.beneficiaryId.setValidators([Validators.required]);
      this.billForm.controls.payeeName.clearValidators();
      this.billForm.controls.payeeAccountNumber.clearValidators();
      this.billForm.controls.payeeBankName.clearValidators();
      this.billForm.controls.payeeIfscOrRouting.clearValidators();
    }
    this.billForm.controls.beneficiaryId.updateValueAndValidity();
    this.billForm.controls.payeeName.updateValueAndValidity();
    this.billForm.controls.payeeAccountNumber.updateValueAndValidity();
    this.billForm.controls.payeeBankName.updateValueAndValidity();
    this.billForm.controls.payeeIfscOrRouting.updateValueAndValidity();

    if (this.billForm.invalid) {
      this.billForm.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.error.set(null);
    this.result.set(null);

    this.bff
      .billPay({
        accountId: value.accountId,
        beneficiaryId: useOneTime ? undefined : value.beneficiaryId || undefined,
        payeeName: useOneTime ? value.payeeName : undefined,
        payeeAccountNumber: useOneTime ? value.payeeAccountNumber : undefined,
        payeeBankName: useOneTime ? value.payeeBankName : undefined,
        payeeIfscOrRouting: useOneTime ? value.payeeIfscOrRouting : undefined,
        amount: Number(value.amount),
        currency: value.currency.toUpperCase(),
        paymentMethod: value.paymentMethod,
        remarks: value.remarks || undefined,
        billReference: value.billReference || undefined,
      })
      .subscribe({
        next: (response) => {
          this.result.set(response);
          this.submitting.set(false);
        },
        error: (err) => {
          this.error.set(err?.error?.message || 'Bill payment failed.');
          this.submitting.set(false);
        },
      });
  }

  submitCard(): void {
    if (this.cardForm.invalid) {
      this.cardForm.markAllAsTouched();
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

    const value = this.cardForm.getRawValue();
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

  onAccountChange(accountId: string, form: 'transfer' | 'bill' | 'card'): void {
    const account = this.accounts().find((a) => a.id === accountId);
    if (!account) {
      return;
    }
    const patch = { currency: account.currency || 'USD' };
    if (form === 'transfer') {
      this.transferForm.patchValue(patch);
    } else if (form === 'bill') {
      this.billForm.patchValue(patch);
    } else {
      this.cardForm.patchValue(patch);
    }
  }
}
