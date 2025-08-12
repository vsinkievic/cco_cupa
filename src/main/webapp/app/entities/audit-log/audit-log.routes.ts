import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import AuditLogResolve from './route/audit-log-routing-resolve.service';

const auditLogRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/audit-log.component').then(m => m.AuditLogComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/audit-log-detail.component').then(m => m.AuditLogDetailComponent),
    resolve: {
      auditLog: AuditLogResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default auditLogRoute;
