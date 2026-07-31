export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
  userId: string;
  email: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
}

export interface Account {
  id: string;
  userId: string;
  accountNumber: string;
  cardLastFour: string;
  cardBrand: string;
  creditLimit: number;
  availableCredit: number;
  currency: string;
  status: string;
  holderName: string;
  email: string;
  phone: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Transaction {
  id: string;
  accountId: string;
  type: string;
  amount: number;
  currency: string;
  merchant: string;
  description: string;
  status: string;
  occurredAt: string;
  createdAt?: string;
}

export interface TransactionHistory {
  accountId: string;
  transactions: Transaction[];
  page?: number;
  size?: number;
  totalElements?: number;
}

export interface NotificationItem {
  id: string;
  userId: string;
  channel: string;
  template: string;
  recipient: string;
  payload: string;
  status: string;
  correlationId: string;
  errorMessage?: string;
  createdAt: string;
  sentAt?: string;
}

export interface DashboardResponse {
  userId: string;
  email: string;
  accounts: Account[];
  recentTransactions: Transaction[];
  notifications: NotificationItem[];
  channelId: string;
  clientId: string;
}

export type PaymentMethod = 'CARD' | 'UPI' | 'NET_BANKING' | 'EXTERNAL';

export interface InitiatePaymentRequest {
  accountId: string;
  userId: string;
  amount: number;
  currency: string;
  paymentMethod: PaymentMethod;
}

export interface PaymentResponse {
  id: string;
  accountId: string;
  userId: string;
  amount: number;
  currency: string;
  paymentMethod: PaymentMethod;
  status: string;
  externalRef?: string;
  failureReason?: string;
  correlationId?: string;
  createdAt?: string;
  updatedAt?: string;
}
