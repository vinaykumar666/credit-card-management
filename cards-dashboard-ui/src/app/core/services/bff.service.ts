import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Account,
  Beneficiary,
  CreateBeneficiaryRequest,
  DashboardResponse,
  InitiatePaymentRequest,
  MakePaymentRequest,
  NotificationItem,
  PaymentResponse,
  Transaction,
  TransactionHistory,
  TransferMoneyRequest,
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

  getBeneficiaries(activeOnly = true): Observable<Beneficiary[]> {
    const params = new HttpParams().set('activeOnly', String(activeOnly));
    return this.http.get<Beneficiary[]>(`${this.base}/beneficiaries`, { params });
  }

  createBeneficiary(body: CreateBeneficiaryRequest): Observable<Beneficiary> {
    return this.http.post<Beneficiary>(`${this.base}/beneficiaries`, body);
  }

  deactivateBeneficiary(id: string): Observable<Beneficiary> {
    return this.http.delete<Beneficiary>(`${this.base}/beneficiaries/${id}`);
  }

  initiatePayment(request: InitiatePaymentRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(`${this.base}/payments`, request);
  }

  transferMoney(request: TransferMoneyRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(`${this.base}/payments/transfer`, request);
  }

  billPay(request: MakePaymentRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(`${this.base}/payments/bill-pay`, request);
  }

  paymentHistory(): Observable<PaymentResponse[]> {
    return this.http.get<PaymentResponse[]>(`${this.base}/payments`);
  }
}
