import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IPaymentTransaction } from '../payment-transaction.model';
import { PaymentTransactionService } from '../service/payment-transaction.service';

const paymentTransactionResolve = (route: ActivatedRouteSnapshot): Observable<null | IPaymentTransaction> => {
  const id = route.params.id;
  if (id) {
    return inject(PaymentTransactionService)
      .find(id)
      .pipe(
        mergeMap((paymentTransaction: HttpResponse<IPaymentTransaction>) => {
          if (paymentTransaction.body) {
            return of(paymentTransaction.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default paymentTransactionResolve;
