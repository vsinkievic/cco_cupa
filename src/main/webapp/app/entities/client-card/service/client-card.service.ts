import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IClientCard, NewClientCard } from '../client-card.model';

export type PartialUpdateClientCard = Partial<IClientCard> & Pick<IClientCard, 'id'>;

export type EntityResponseType = HttpResponse<IClientCard>;
export type EntityArrayResponseType = HttpResponse<IClientCard[]>;

@Injectable({ providedIn: 'root' })
export class ClientCardService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/client-cards');

  create(clientCard: NewClientCard): Observable<EntityResponseType> {
    return this.http.post<IClientCard>(this.resourceUrl, clientCard, { observe: 'response' });
  }

  update(clientCard: IClientCard): Observable<EntityResponseType> {
    return this.http.put<IClientCard>(`${this.resourceUrl}/${this.getClientCardIdentifier(clientCard)}`, clientCard, {
      observe: 'response',
    });
  }

  partialUpdate(clientCard: PartialUpdateClientCard): Observable<EntityResponseType> {
    return this.http.patch<IClientCard>(`${this.resourceUrl}/${this.getClientCardIdentifier(clientCard)}`, clientCard, {
      observe: 'response',
    });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IClientCard>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IClientCard[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getClientCardIdentifier(clientCard: Pick<IClientCard, 'id'>): number {
    return clientCard.id;
  }

  compareClientCard(o1: Pick<IClientCard, 'id'> | null, o2: Pick<IClientCard, 'id'> | null): boolean {
    return o1 && o2 ? this.getClientCardIdentifier(o1) === this.getClientCardIdentifier(o2) : o1 === o2;
  }

  addClientCardToCollectionIfMissing<Type extends Pick<IClientCard, 'id'>>(
    clientCardCollection: Type[],
    ...clientCardsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const clientCards: Type[] = clientCardsToCheck.filter(isPresent);
    if (clientCards.length > 0) {
      const clientCardCollectionIdentifiers = clientCardCollection.map(clientCardItem => this.getClientCardIdentifier(clientCardItem));
      const clientCardsToAdd = clientCards.filter(clientCardItem => {
        const clientCardIdentifier = this.getClientCardIdentifier(clientCardItem);
        if (clientCardCollectionIdentifiers.includes(clientCardIdentifier)) {
          return false;
        }
        clientCardCollectionIdentifiers.push(clientCardIdentifier);
        return true;
      });
      return [...clientCardsToAdd, ...clientCardCollection];
    }
    return clientCardCollection;
  }
}
