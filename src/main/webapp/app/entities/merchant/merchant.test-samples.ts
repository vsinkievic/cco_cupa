import { IMerchant, NewMerchant } from './merchant.model';

export const sampleWithRequiredData: IMerchant = {
  id: '31556',
  name: 'until',
  mode: 'TEST',
  status: 'INACTIVE',
  currency: 'USD',
  version: 1,
};

export const sampleWithPartialData: IMerchant = {
  id: '3470',
  name: 'briskly wedge',
  mode: 'LIVE',
  status: 'INACTIVE',
  balance: 684.06,
  currency: 'AUD',
  remoteTestMerchantKey: 'and cake bah',
  remoteTestApiKey: 'beneath ribbon',
  remoteProdMerchantId: 'apt',
  remoteProdApiKey: 'steeple stack',
  version: 1,
};

export const sampleWithFullData: IMerchant = {
  id: '10412',
  name: 'stool jealously uh-huh',
  mode: 'TEST',
  status: 'INACTIVE',
  balance: 9383.03,
  currency: 'USD',
  cupaTestApiKey: 'for under remorseful',
  cupaProdApiKey: 'thankfully maul',
  remoteTestUrl: 'inwardly ouch supposing',
  remoteTestMerchantId: 'educated secrecy zowie',
  remoteTestMerchantKey: 'abaft',
  remoteTestApiKey: 'oof crossly tinted',
  remoteProdUrl: 'versus blacken yahoo',
  remoteProdMerchantId: 'unto',
  remoteProdMerchantKey: 'inasmuch ack when',
  remoteProdApiKey: 'physically axe',
  version: 0,
};

export const sampleWithNewData: NewMerchant = {
  name: 'overreact festival',
  mode: 'TEST',
  status: 'INACTIVE',
  currency: 'USD',
  id: null,
  version: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
