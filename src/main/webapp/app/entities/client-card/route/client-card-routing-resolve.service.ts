import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IClientCard } from '../client-card.model';
import { ClientCardService } from '../service/client-card.service';

const clientCardResolve = (route: ActivatedRouteSnapshot): Observable<null | IClientCard> => {
  const id = route.params.id;
  if (id) {
    return inject(ClientCardService)
      .find(id)
      .pipe(
        mergeMap((clientCard: HttpResponse<IClientCard>) => {
          if (clientCard.body) {
            return of(clientCard.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default clientCardResolve;
