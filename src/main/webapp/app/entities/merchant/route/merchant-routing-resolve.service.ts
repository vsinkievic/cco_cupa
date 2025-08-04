import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IMerchant } from '../merchant.model';
import { MerchantService } from '../service/merchant.service';

const merchantResolve = (route: ActivatedRouteSnapshot): Observable<null | IMerchant> => {
  const id = route.params.id;
  if (id) {
    return inject(MerchantService)
      .find(id)
      .pipe(
        mergeMap((merchant: HttpResponse<IMerchant>) => {
          if (merchant.body) {
            return of(merchant.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default merchantResolve;
