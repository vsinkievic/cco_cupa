import { IClient } from 'app/entities/client/client.model';

export interface IClientCard {
  id: number;
  maskedPan?: string | null;
  expiryDate?: string | null;
  cardholderName?: string | null;
  isDefault?: boolean | null;
  isValid?: boolean | null;
  client?: Pick<IClient, 'id' | 'merchantClientId'> | null;
}

export type NewClientCard = Omit<IClientCard, 'id'> & { id: null };
