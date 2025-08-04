import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IAuditLog, NewAuditLog } from '../audit-log.model';

export type PartialUpdateAuditLog = Partial<IAuditLog> & Pick<IAuditLog, 'id'>;

type RestOf<T extends IAuditLog | NewAuditLog> = Omit<T, 'requestTimestamp'> & {
  requestTimestamp?: string | null;
};

export type RestAuditLog = RestOf<IAuditLog>;

export type NewRestAuditLog = RestOf<NewAuditLog>;

export type PartialUpdateRestAuditLog = RestOf<PartialUpdateAuditLog>;

export type EntityResponseType = HttpResponse<IAuditLog>;
export type EntityArrayResponseType = HttpResponse<IAuditLog[]>;

@Injectable({ providedIn: 'root' })
export class AuditLogService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/audit-logs');

  create(auditLog: NewAuditLog): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(auditLog);
    return this.http
      .post<RestAuditLog>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(auditLog: IAuditLog): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(auditLog);
    return this.http
      .put<RestAuditLog>(`${this.resourceUrl}/${this.getAuditLogIdentifier(auditLog)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(auditLog: PartialUpdateAuditLog): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(auditLog);
    return this.http
      .patch<RestAuditLog>(`${this.resourceUrl}/${this.getAuditLogIdentifier(auditLog)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

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

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
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

  protected convertDateFromClient<T extends IAuditLog | NewAuditLog | PartialUpdateAuditLog>(auditLog: T): RestOf<T> {
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
