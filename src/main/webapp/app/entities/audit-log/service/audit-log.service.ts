import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IAuditLog } from '../audit-log.model';

type RestOf<T extends IAuditLog> = Omit<T, 'requestTimestamp'> & {
  requestTimestamp?: string | null;
};

export type RestAuditLog = RestOf<IAuditLog>;

export type EntityResponseType = HttpResponse<IAuditLog>;
export type EntityArrayResponseType = HttpResponse<IAuditLog[]>;

@Injectable({ providedIn: 'root' })
export class AuditLogService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/audit-logs');

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestAuditLog>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestAuditLog[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  getAuditLogIdentifier(auditLog: Pick<IAuditLog, 'id'>): number {
    return auditLog.id;
  }

  compareAuditLog(o1: Pick<IAuditLog, 'id'> | null, o2: Pick<IAuditLog, 'id'> | null): boolean {
    return o1 && o2 ? this.getAuditLogIdentifier(o1) === this.getAuditLogIdentifier(o2) : o1 === o2;
  }

  addAuditLogToCollectionIfMissing<Type extends Pick<IAuditLog, 'id'>>(
    auditLogCollection: Type[],
    ...auditLogsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const auditLogs: Type[] = auditLogsToCheck.filter(isPresent);
    if (auditLogs.length > 0) {
      const auditLogCollectionIdentifiers = auditLogCollection.map(auditLogItem => this.getAuditLogIdentifier(auditLogItem));
      const auditLogsToAdd = auditLogs.filter(auditLogItem => {
        const auditLogIdentifier = this.getAuditLogIdentifier(auditLogItem);
        if (auditLogCollectionIdentifiers.includes(auditLogIdentifier)) {
          return false;
        }
        auditLogCollectionIdentifiers.push(auditLogIdentifier);
        return true;
      });
      return [...auditLogsToAdd, ...auditLogCollection];
    }
    return auditLogCollection;
  }

  protected convertDateFromClient<T extends IAuditLog>(auditLog: T): RestOf<T> {
    return {
      ...auditLog,
      requestTimestamp: auditLog.requestTimestamp?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(restAuditLog: RestAuditLog): IAuditLog {
    return {
      ...restAuditLog,
      requestTimestamp: restAuditLog.requestTimestamp ? dayjs(restAuditLog.requestTimestamp) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestAuditLog>): HttpResponse<IAuditLog> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestAuditLog[]>): HttpResponse<IAuditLog[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
