import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Account,
  DashboardResponse,
  InitiatePaymentRequest,
  NotificationItem,
  PaymentResponse,
  Transaction,
  TransactionHistory,
} from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class BffService {
  private readonly base = `${environment.bffUrl}/bff/v1`;

  constructor(private readonly http: HttpClient) {}

  getDashboard(): Observable<DashboardResponse> {
    return this.http.get<DashboardResponse>(`${this.base}/dashboard`);
  }

  getAccounts(): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.base}/accounts`);
  }

  getTransactions(accountId: string): Observable<TransactionHistory | Transaction[]> {
    return this.http.get<TransactionHistory | Transaction[]>(
      `${this.base}/accounts/${accountId}/transactions`,
    );
  }

  getNotifications(): Observable<NotificationItem[]> {
    return this.http.get<NotificationItem[]>(`${this.base}/notifications`);
  }

  initiatePayment(request: InitiatePaymentRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(`${this.base}/payments`, request);
  }
}
