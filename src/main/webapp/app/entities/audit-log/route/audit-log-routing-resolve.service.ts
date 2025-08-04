import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IAuditLog } from '../audit-log.model';
import { AuditLogService } from '../service/audit-log.service';

const auditLogResolve = (route: ActivatedRouteSnapshot): Observable<null | IAuditLog> => {
  const id = route.params.id;
  if (id) {
    return inject(AuditLogService)
      .find(id)
      .pipe(
        mergeMap((auditLog: HttpResponse<IAuditLog>) => {
          if (auditLog.body) {
            return of(auditLog.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default auditLogResolve;
