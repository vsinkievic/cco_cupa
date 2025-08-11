import { IClientCard, NewClientCard } from './client-card.model';

export const sampleWithRequiredData: IClientCard = {
  id: '22313',
  maskedPan: 'respectful',
  version: 0,
};

export const sampleWithPartialData: IClientCard = {
  id: '1476',
  maskedPan: 'since zowie',
  expiryDate: 'complete deduction draw',
  cardholderName: 'unbearably',
  isDefault: true,
  version: 1,
};

export const sampleWithFullData: IClientCard = {
  id: '9165',
  maskedPan: 'er',
  expiryDate: 'uselessly wavy supposing',
  cardholderName: 'now',
  isDefault: true,
  isValid: false,
  version: 2,
};

export const sampleWithNewData: NewClientCard = {
  maskedPan: 'onto anti aw',
  id: null,
  version: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
