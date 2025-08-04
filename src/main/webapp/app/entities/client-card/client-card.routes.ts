import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import ClientCardResolve from './route/client-card-routing-resolve.service';

const clientCardRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/client-card.component').then(m => m.ClientCardComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/client-card-detail.component').then(m => m.ClientCardDetailComponent),
    resolve: {
      clientCard: ClientCardResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/client-card-update.component').then(m => m.ClientCardUpdateComponent),
    resolve: {
      clientCard: ClientCardResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/client-card-update.component').then(m => m.ClientCardUpdateComponent),
    resolve: {
      clientCard: ClientCardResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default clientCardRoute;
