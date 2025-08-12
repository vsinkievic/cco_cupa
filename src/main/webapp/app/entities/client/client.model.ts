export interface IClient {
  id: string;
  merchantClientId?: string | null;
  merchantId?: string | null;
  merchantName?: string | null;
  name?: string | null;
  emailAddress?: string | null;
  mobileNumber?: string | null;
  clientPhone?: string | null;
  streetNumber?: string | null;
  streetName?: string | null;
  streetSuffix?: string | null;
  city?: string | null;
  state?: string | null;
  postCode?: string | null;
  country?: string | null;
  gatewayClientId?: string | null;
  createdInGateway?: string | null;
  updatedInGateway?: string | null;
  valid?: boolean | null;
  isBlacklisted?: boolean | null;
  isCorrelatedBlacklisted?: boolean | null;
  version?: number | null;
}

export type NewClient = Omit<IClient, 'id' | 'version'> & { id: null; version: null };
