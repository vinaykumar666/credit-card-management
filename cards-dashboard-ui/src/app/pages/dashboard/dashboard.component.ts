import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DashboardResponse } from '../../core/models/api.models';
import { BffService } from '../../core/services/bff.service';
import { toUserMessage } from '../../core/utils/user-error';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  private readonly bff = inject(BffService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly data = signal<DashboardResponse | null>(null);

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.bff.getDashboard().subscribe({
      next: (response) => {
        this.data.set(response);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(toUserMessage(err, 'We could not load your home screen. Please try again.'));
        this.loading.set(false);
      },
    });
  }

  totalAvailable(): number {
    return (this.data()?.accounts ?? []).reduce(
      (sum, account) => sum + Number(account.availableCredit || 0),
      0,
    );
  }

  friendlyStatus(value?: string | null): string {
    if (!value) return 'Updated';
    const map: Record<string, string> = {
      ACTIVE: 'Active',
      PENDING: 'Pending',
      COMPLETED: 'Completed',
      FAILED: 'Failed',
      SENT: 'Sent',
      READ: 'Read',
    };
    return map[value.toUpperCase()] || value.replace(/_/g, ' ').toLowerCase().replace(/^\w/, (c) => c.toUpperCase());
  }

  friendlyType(value?: string | null): string {
    if (!value) return 'Activity';
    return value.replace(/_/g, ' ').toLowerCase().replace(/^\w/, (c) => c.toUpperCase());
  }

  friendlyTemplate(value?: string | null): string {
    if (!value) return 'Update';
    return value.replace(/_/g, ' ').toLowerCase().replace(/^\w/, (c) => c.toUpperCase());
  }
}
