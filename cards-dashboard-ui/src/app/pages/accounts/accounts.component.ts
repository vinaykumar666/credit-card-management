import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { Account, Transaction } from '../../core/models/api.models';
import { BffService } from '../../core/services/bff.service';

@Component({
  selector: 'app-accounts',
  standalone: true,
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './accounts.component.html',
  styleUrl: './accounts.component.scss',
})
export class AccountsComponent implements OnInit {
  private readonly bff = inject(BffService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly accounts = signal<Account[]>([]);
  readonly selectedId = signal<string | null>(null);
  readonly transactions = signal<Transaction[]>([]);
  readonly txLoading = signal(false);

  ngOnInit(): void {
    this.bff.getAccounts().subscribe({
      next: (accounts) => {
        this.accounts.set(accounts ?? []);
        this.loading.set(false);
        if (accounts?.length) {
          this.selectAccount(accounts[0].id);
        }
      },
      error: (err) => {
        this.error.set(err?.error?.message || 'Failed to load accounts.');
        this.loading.set(false);
      },
    });
  }

  selectAccount(accountId: string): void {
    this.selectedId.set(accountId);
    this.txLoading.set(true);
    this.bff.getTransactions(accountId).subscribe({
      next: (response) => {
        const list = Array.isArray(response) ? response : response.transactions ?? [];
        this.transactions.set(list);
        this.txLoading.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.message || 'Failed to load transactions.');
        this.txLoading.set(false);
      },
    });
  }
}
