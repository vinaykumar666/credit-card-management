import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { Account, Transaction } from '../../core/models/api.models';
import { BffService } from '../../core/services/bff.service';
import { toUserMessage } from '../../core/utils/user-error';

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
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.bff.getAccounts().subscribe({
      next: (accounts) => {
        this.accounts.set(accounts ?? []);
        this.loading.set(false);
        if (accounts?.length) {
          this.selectAccount(accounts[0].id);
        }
      },
      error: (err) => {
        this.error.set(toUserMessage(err, 'We could not load your cards. Please try again.'));
        this.loading.set(false);
      },
    });
  }

  selectAccount(accountId: string): void {
    this.selectedId.set(accountId);
    this.txLoading.set(true);
    this.error.set(null);
    this.bff.getTransactions(accountId).subscribe({
      next: (response) => {
        const list = Array.isArray(response) ? response : response.transactions ?? [];
        this.transactions.set(list);
        this.txLoading.set(false);
      },
      error: (err) => {
        this.error.set(toUserMessage(err, 'We could not load spending history. Please try again.'));
        this.txLoading.set(false);
      },
    });
  }

  friendlyStatus(value?: string | null): string {
    if (!value) return '—';
    const map: Record<string, string> = {
      ACTIVE: 'Active',
      INACTIVE: 'Inactive',
      BLOCKED: 'Blocked',
      PENDING: 'Pending',
      COMPLETED: 'Completed',
      FAILED: 'Failed',
      POSTED: 'Posted',
    };
    return map[value.toUpperCase()] || value.replace(/_/g, ' ');
  }

  friendlyType(value?: string | null): string {
    if (!value) return '—';
    return value.replace(/_/g, ' ').toLowerCase().replace(/^\w/, (c) => c.toUpperCase());
  }
}
