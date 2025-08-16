import dayjs from 'dayjs/esm';
import { TransactionStatus } from 'app/entities/enumerations/transaction-status.model';
import { PaymentBrand } from 'app/entities/enumerations/payment-brand.model';
import { Currency } from 'app/entities/enumerations/currency.model';

export interface IPaymentTransaction {
  id: string;
  orderId?: string | null;
  gatewayTransactionId?: string | null;
  status?: keyof typeof TransactionStatus | null;
  statusDescription?: string | null;
  paymentBrand?: keyof typeof PaymentBrand | null;
  amount?: number | null;
  balance?: number | null;
  currency?: keyof typeof Currency | null;
  replyUrl?: string | null;
  backofficeUrl?: string | null;
  echo?: string | null;
  paymentFlow?: string | null;
  signature?: string | null;
  signatureVersion?: string | null;
  requestTimestamp?: dayjs.Dayjs | null;
  requestData?: string | null;
  initialResponseData?: string | null;
  callbackTimestamp?: dayjs.Dayjs | null;
  callbackData?: string | null;
  lastQueryData?: string | null;
  clientId?: string | null;
  merchantClientId?: string | null;
  merchantId?: string | null;
  clientName?: string | null;
  merchantName?: string | null;
  version?: number | null;
  createdBy?: string | null;
  createdDate?: dayjs.Dayjs | null;
  lastModifiedBy?: string | null;
  lastModifiedDate?: dayjs.Dayjs | null;
}

export type NewPaymentTransaction = Omit<IPaymentTransaction, 'id' | 'version'> & { id: null; version: null };
