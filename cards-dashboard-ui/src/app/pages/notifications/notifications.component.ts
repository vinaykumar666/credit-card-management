import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { NotificationItem } from '../../core/models/api.models';
import { BffService } from '../../core/services/bff.service';
import { toUserMessage } from '../../core/utils/user-error';

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
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.bff.getNotifications().subscribe({
      next: (items) => {
        this.items.set(items ?? []);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(toUserMessage(err, 'We could not load your messages. Please try again.'));
        this.loading.set(false);
      },
    });
  }

  friendlyTemplate(value?: string | null): string {
    if (!value) return 'Update';
    return value.replace(/_/g, ' ').toLowerCase().replace(/^\w/, (c) => c.toUpperCase());
  }

  friendlyChannel(value?: string | null): string {
    const map: Record<string, string> = {
      EMAIL: 'Email',
      SMS: 'Text message',
      PUSH: 'App alert',
    };
    if (!value) return 'Notice';
    return map[value.toUpperCase()] || value;
  }

  friendlyStatus(value?: string | null): string {
    const map: Record<string, string> = {
      SENT: 'Sent',
      PENDING: 'Pending',
      FAILED: 'Could not send',
      READ: 'Read',
    };
    if (!value) return '';
    return map[value.toUpperCase()] || value;
  }
}
