import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DashboardResponse } from '../../core/models/api.models';
import { BffService } from '../../core/services/bff.service';

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
    this.bff.getDashboard().subscribe({
      next: (response) => {
        this.data.set(response);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.message || 'Failed to load dashboard from BFF.');
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
}
