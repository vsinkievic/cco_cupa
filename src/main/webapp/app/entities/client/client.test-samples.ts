import { IClient, NewClient } from './client.model';

export const sampleWithRequiredData: IClient = {
  id: '16289',
  merchantClientId: 'save colour yippee',
};

export const sampleWithPartialData: IClient = {
  id: '29388',
  merchantClientId: 'blah absent',
  name: 'magnetize purse',
  emailAddress: '8@J}."~Hi',
  mobileNumber: 'whoever',
  clientPhone: 'hyena',
  valid: true,
  streetNumber: 'extremely until out',
  streetSuffix: 'gymnast',
  city: 'North Highlands',
  country: 'Gibraltar',
  isBlacklisted: true,
};

export const sampleWithFullData: IClient = {
  id: '31496',
  merchantClientId: 'boohoo shipper soggy',
  name: 'that consequently',
  emailAddress: 'V@Fqx=(Z.{Tqn',
  mobileNumber: 'ouch blah',
  clientPhone: 'earth',
  valid: true,
  streetNumber: 'ew plus elegant',
  streetName: 'Leffler Parkway',
  streetSuffix: 'agitated within ack',
  city: 'Sauermouth',
  state: 'unto loose ack',
  postCode: '40931',
  country: 'San Marino',
  isBlacklisted: true,
  isCorrelatedBlacklisted: false,
};

export const sampleWithNewData: NewClient = {
  merchantClientId: 'seemingly excepting',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
