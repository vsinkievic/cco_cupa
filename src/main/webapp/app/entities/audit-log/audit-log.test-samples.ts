import dayjs from 'dayjs/esm';

import { IAuditLog } from './audit-log.model';

export const sampleWithRequiredData: IAuditLog = {
  id: 24557,
  requestTimestamp: dayjs('2025-08-03T22:14'),
  apiEndpoint: 'animated cruelty poorly',
  httpMethod: 'completion amid',
};

export const sampleWithPartialData: IAuditLog = {
  id: 16741,
  requestTimestamp: dayjs('2025-08-03T17:43'),
  apiEndpoint: 'an round',
  httpMethod: 'untimely frizzy out',
  httpStatusCode: 9195,
  orderId: 'inside',
  responseDescription: 'consequently',
  cupaApiKey: 'faraway',
  requestData: '../fake-data/blob/hipster.txt',
  responseData: '../fake-data/blob/hipster.txt',
  requesterIpAddress: 'always',
};

export const sampleWithFullData: IAuditLog = {
  id: 16792,
  requestTimestamp: dayjs('2025-08-04T09:47'),
  apiEndpoint: 'needily knowingly',
  httpMethod: 'times bright',
  httpStatusCode: 24278,
  orderId: 'restfully hence gazebo',
  responseDescription: 'dowse',
  cupaApiKey: 'at typeface curse',
  environment: 'but while ape',
  requestData: '../fake-data/blob/hipster.txt',
  responseData: '../fake-data/blob/hipster.txt',
  requesterIpAddress: 'ew tinted',
};

Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
