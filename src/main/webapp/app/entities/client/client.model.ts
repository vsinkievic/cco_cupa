import { IMerchant } from 'app/entities/merchant/merchant.model';

export interface IClient {
  id: string;
  merchantClientId?: string | null;
  name?: string | null;
  emailAddress?: string | null;
  mobileNumber?: string | null;
  clientPhone?: string | null;
  valid?: boolean | null;
  streetNumber?: string | null;
  streetName?: string | null;
  streetSuffix?: string | null;
  city?: string | null;
  state?: string | null;
  postCode?: string | null;
  country?: string | null;
  isBlacklisted?: boolean | null;
  isCorrelatedBlacklisted?: boolean | null;
  createdInGateway?: string | null;
  updatedInGateway?: string | null;
  merchant?: Pick<IMerchant, 'id' | 'name'> | null;
}

export type NewClient = Omit<IClient, 'id'> & { id: null };
