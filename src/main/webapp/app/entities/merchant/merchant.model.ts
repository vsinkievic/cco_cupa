import { MerchantMode } from 'app/entities/enumerations/merchant-mode.model';
import { MerchantStatus } from 'app/entities/enumerations/merchant-status.model';

export interface IMerchant {
  id: number;
  name?: string | null;
  mode?: keyof typeof MerchantMode | null;
  status?: keyof typeof MerchantStatus | null;
  balance?: number | null;
  cupaTestApiKey?: string | null;
  cupaProdApiKey?: string | null;
  remoteTestUrl?: string | null;
  remoteTestMerchantId?: string | null;
  remoteTestMerchantKey?: string | null;
  remoteTestApiKey?: string | null;
  remoteProdUrl?: string | null;
  remoteProdMerchantId?: string | null;
  remoteProdMerchantKey?: string | null;
  remoteProdApiKey?: string | null;
}

export type NewMerchant = Omit<IMerchant, 'id'> & { id: null };
