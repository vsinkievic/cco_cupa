import dayjs from 'dayjs/esm';
import { IMerchant } from 'app/entities/merchant/merchant.model';

export interface IAuditLog {
  id: number;
  requestTimestamp?: dayjs.Dayjs | null;
  apiEndpoint?: string | null;
  httpMethod?: string | null;
  httpStatusCode?: number | null;
  orderId?: string | null;
  responseDescription?: string | null;
  cupaApiKey?: string | null;
  environment?: string | null;
  requestData?: string | null;
  responseData?: string | null;
  requesterIpAddress?: string | null;
  merchantId?: string | null;
}
