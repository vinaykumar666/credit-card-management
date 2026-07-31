import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { NotificationItem } from '../../core/models/api.models';
import { BffService } from '../../core/services/bff.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './notifications.component.html',
  styleUrl: './notifications.component.scss',
})
export class NotificationsComponent implements OnInit {
  private readonly bff = inject(BffService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly items = signal<NotificationItem[]>([]);

  ngOnInit(): void {
    this.bff.getNotifications().subscribe({
      next: (items) => {
        this.items.set(items ?? []);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.message || 'Failed to load notifications.');
        this.loading.set(false);
      },
    });
  }
}
