import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IPaymentTransaction, NewPaymentTransaction } from '../payment-transaction.model';

export type PartialUpdatePaymentTransaction = Partial<IPaymentTransaction> & Pick<IPaymentTransaction, 'id'>;

type RestOf<T extends IPaymentTransaction | NewPaymentTransaction> = Omit<T, 'requestTimestamp' | 'callbackTimestamp'> & {
  requestTimestamp?: string | null;
  callbackTimestamp?: string | null;
};

export type RestPaymentTransaction = RestOf<IPaymentTransaction>;

export type NewRestPaymentTransaction = RestOf<NewPaymentTransaction>;

export type PartialUpdateRestPaymentTransaction = RestOf<PartialUpdatePaymentTransaction>;

export type EntityResponseType = HttpResponse<IPaymentTransaction>;
export type EntityArrayResponseType = HttpResponse<IPaymentTransaction[]>;

@Injectable({ providedIn: 'root' })
export class PaymentTransactionService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/payment-transactions');

  create(paymentTransaction: NewPaymentTransaction): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(paymentTransaction);
    return this.http
      .post<RestPaymentTransaction>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(paymentTransaction: IPaymentTransaction): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(paymentTransaction);
    return this.http
      .put<RestPaymentTransaction>(`${this.resourceUrl}/${this.getPaymentTransactionIdentifier(paymentTransaction)}`, copy, {
        observe: 'response',
      })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(paymentTransaction: PartialUpdatePaymentTransaction): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(paymentTransaction);
    return this.http
      .patch<RestPaymentTransaction>(`${this.resourceUrl}/${this.getPaymentTransactionIdentifier(paymentTransaction)}`, copy, {
        observe: 'response',
      })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: string): Observable<EntityResponseType> {
    return this.http
      .get<RestPaymentTransaction>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestPaymentTransaction[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  getPaymentTransactionIdentifier(paymentTransaction: Pick<IPaymentTransaction, 'id'>): string {
    return paymentTransaction.id;
  }

  comparePaymentTransaction(o1: Pick<IPaymentTransaction, 'id'> | null, o2: Pick<IPaymentTransaction, 'id'> | null): boolean {
    return o1 && o2 ? this.getPaymentTransactionIdentifier(o1) === this.getPaymentTransactionIdentifier(o2) : o1 === o2;
  }

  addPaymentTransactionToCollectionIfMissing<Type extends Pick<IPaymentTransaction, 'id'>>(
    paymentTransactionCollection: Type[],
    ...paymentTransactionsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const paymentTransactions: Type[] = paymentTransactionsToCheck.filter(isPresent);
    if (paymentTransactions.length > 0) {
      const paymentTransactionCollectionIdentifiers = paymentTransactionCollection.map(paymentTransactionItem =>
        this.getPaymentTransactionIdentifier(paymentTransactionItem),
      );
      const paymentTransactionsToAdd = paymentTransactions.filter(paymentTransactionItem => {
        const paymentTransactionIdentifier = this.getPaymentTransactionIdentifier(paymentTransactionItem);
        if (paymentTransactionCollectionIdentifiers.includes(paymentTransactionIdentifier)) {
          return false;
        }
        paymentTransactionCollectionIdentifiers.push(paymentTransactionIdentifier);
        return true;
      });
      return [...paymentTransactionsToAdd, ...paymentTransactionCollection];
    }
    return paymentTransactionCollection;
  }

  protected convertDateFromClient<T extends IPaymentTransaction | NewPaymentTransaction | PartialUpdatePaymentTransaction>(
    paymentTransaction: T,
  ): RestOf<T> {
    return {
      ...paymentTransaction,
      requestTimestamp: paymentTransaction.requestTimestamp?.toJSON() ?? null,
      callbackTimestamp: paymentTransaction.callbackTimestamp?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(restPaymentTransaction: RestPaymentTransaction): IPaymentTransaction {
    return {
      ...restPaymentTransaction,
      requestTimestamp: restPaymentTransaction.requestTimestamp ? dayjs(restPaymentTransaction.requestTimestamp) : undefined,
      callbackTimestamp: restPaymentTransaction.callbackTimestamp ? dayjs(restPaymentTransaction.callbackTimestamp) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestPaymentTransaction>): HttpResponse<IPaymentTransaction> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestPaymentTransaction[]>): HttpResponse<IPaymentTransaction[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
