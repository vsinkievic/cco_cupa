import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IMerchant, NewMerchant } from '../merchant.model';

export type PartialUpdateMerchant = Partial<IMerchant> & Pick<IMerchant, 'id'>;

export type EntityResponseType = HttpResponse<IMerchant>;
export type EntityArrayResponseType = HttpResponse<IMerchant[]>;

@Injectable({ providedIn: 'root' })
export class MerchantService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/merchants');

  create(merchant: NewMerchant): Observable<EntityResponseType> {
    return this.http.post<IMerchant>(this.resourceUrl, merchant, { observe: 'response' });
  }

  update(merchant: IMerchant): Observable<EntityResponseType> {
    return this.http.put<IMerchant>(`${this.resourceUrl}/${this.getMerchantIdentifier(merchant)}`, merchant, { observe: 'response' });
  }

  partialUpdate(merchant: PartialUpdateMerchant): Observable<EntityResponseType> {
    return this.http.patch<IMerchant>(`${this.resourceUrl}/${this.getMerchantIdentifier(merchant)}`, merchant, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IMerchant>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IMerchant[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getMerchantIdentifier(merchant: Pick<IMerchant, 'id'>): number {
    return merchant.id;
  }

  compareMerchant(o1: Pick<IMerchant, 'id'> | null, o2: Pick<IMerchant, 'id'> | null): boolean {
    return o1 && o2 ? this.getMerchantIdentifier(o1) === this.getMerchantIdentifier(o2) : o1 === o2;
  }

  addMerchantToCollectionIfMissing<Type extends Pick<IMerchant, 'id'>>(
    merchantCollection: Type[],
    ...merchantsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const merchants: Type[] = merchantsToCheck.filter(isPresent);
    if (merchants.length > 0) {
      const merchantCollectionIdentifiers = merchantCollection.map(merchantItem => this.getMerchantIdentifier(merchantItem));
      const merchantsToAdd = merchants.filter(merchantItem => {
        const merchantIdentifier = this.getMerchantIdentifier(merchantItem);
        if (merchantCollectionIdentifiers.includes(merchantIdentifier)) {
          return false;
        }
        merchantCollectionIdentifiers.push(merchantIdentifier);
        return true;
      });
      return [...merchantsToAdd, ...merchantCollection];
    }
    return merchantCollection;
  }
}
