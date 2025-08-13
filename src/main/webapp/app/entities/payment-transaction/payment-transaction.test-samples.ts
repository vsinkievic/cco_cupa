import dayjs from 'dayjs/esm';

import { IPaymentTransaction, NewPaymentTransaction } from './payment-transaction.model';

export const sampleWithRequiredData: IPaymentTransaction = {
  id: '16213',
  orderId: 'icy',
  status: 'FAILED',
  paymentBrand: 'ALIPAY',
  amount: 15692.9,
  currency: 'CNY',
  requestTimestamp: dayjs('2025-08-03T22:52'),
};

export const sampleWithPartialData: IPaymentTransaction = {
  id: '20858',
  orderId: 'heartbeat failing whirlwind',
  status: 'FAILED',
  paymentBrand: 'ALIPAY',
  amount: 18862.95,
  balance: 15119.13,
  currency: 'MOP',
  replyUrl: 'topsail slather',
  backofficeUrl: 'statue roasted consequently',
  echo: 'revere inside',
  paymentFlow: 'FLOW_A',
  signatureVersion: 'toward incidentally',
  requestTimestamp: dayjs('2025-08-04T08:13'),
  requestData: '../fake-data/blob/hipster.txt',
  callbackData: '../fake-data/blob/hipster.txt',
  version: 1,
};

export const sampleWithFullData: IPaymentTransaction = {
  id: '8871',
  orderId: 'extricate searchingly',
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
  paymentFlow: 'FLOW_B',
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
  status: 'CANCELLED',
  paymentBrand: 'UNIONPAY',
  amount: 19938.86,
  currency: 'CAD',
  paymentFlow: 'FLOW_C',
  requestTimestamp: dayjs('2025-08-03T16:09'),
  id: null,
  version: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
