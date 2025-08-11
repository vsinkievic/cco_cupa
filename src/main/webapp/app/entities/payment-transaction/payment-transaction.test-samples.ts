import dayjs from 'dayjs/esm';

import { IPaymentTransaction, NewPaymentTransaction } from './payment-transaction.model';

export const sampleWithRequiredData: IPaymentTransaction = {
  id: '16213',
  orderId: 'icy',
  cupaTransactionId: '84ea121b-e25a-43a3-b0a5-68c77b5fba45',
  status: 'FAILED',
  paymentBrand: 'ALIPAY',
  amount: 15692.9,
  currency: 'CNY',
  requestTimestamp: dayjs('2025-08-03T22:52'),
};

export const sampleWithPartialData: IPaymentTransaction = {
  id: '20858',
  orderId: 'heartbeat failing whirlwind',
  cupaTransactionId: '5ac96a25-1193-498d-9291-704df414ce6a',
  status: 'FAILED',
  paymentBrand: 'ALIPAY',
  amount: 18862.95,
  balance: 15119.13,
  currency: 'MOP',
  replyUrl: 'topsail slather',
  backofficeUrl: 'statue roasted consequently',
  echo: 'revere inside',
  signatureVersion: 'toward incidentally',
  requestTimestamp: dayjs('2025-08-04T08:13'),
  requestData: '../fake-data/blob/hipster.txt',
  callbackData: '../fake-data/blob/hipster.txt',
  version: 1,
};

export const sampleWithFullData: IPaymentTransaction = {
  id: '8871',
  orderId: 'extricate searchingly',
  cupaTransactionId: '991ccee8-8567-45f4-acd0-73ebb0727ec5',
  gatewayTransactionId: 'busily foot adumbrate',
  status: 'QUERY_SUCCESS',
  statusDescription: 'purse',
  paymentBrand: 'UNIONPAY',
  amount: 2228.38,
  balance: 10790.7,
  currency: 'SGD',
  replyUrl: 'typify ascertain delightfully',
  backofficeUrl: 'provision whoever fooey',
  echo: 'dependent ectoderm unto',
  sendEmail: false,
  signature: 'drab',
  signatureVersion: 'nervously impractical',
  requestTimestamp: dayjs('2025-08-04T11:25'),
  requestData: '../fake-data/blob/hipster.txt',
  initialResponseData: '../fake-data/blob/hipster.txt',
  callbackTimestamp: dayjs('2025-08-04T09:00'),
  callbackData: '../fake-data/blob/hipster.txt',
  lastQueryData: '../fake-data/blob/hipster.txt',
  version: 0,
};

export const sampleWithNewData: NewPaymentTransaction = {
  orderId: 'slake option since',
  cupaTransactionId: '936e8586-8488-4959-b1d9-6b172f8a0cd8',
  status: 'CANCELLED',
  paymentBrand: 'UNIONPAY',
  amount: 19938.86,
  currency: 'CAD',
  requestTimestamp: dayjs('2025-08-03T16:09'),
  id: null,
  version: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
