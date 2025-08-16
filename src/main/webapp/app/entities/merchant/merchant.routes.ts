import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import MerchantResolve from './route/merchant-routing-resolve.service';

const merchantRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/merchant.component').then(m => m.MerchantComponent),
    data: {
      defaultSort: `id,${ASC}`,
      authorities: ['ROLE_ADMIN'],
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/merchant-detail.component').then(m => m.MerchantDetailComponent),
    resolve: {
      merchant: MerchantResolve,
    },
    data: {
      authorities: ['ROLE_ADMIN'],
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/merchant-update.component').then(m => m.MerchantUpdateComponent),
    resolve: {
      merchant: MerchantResolve,
    },
    data: {
      authorities: ['ROLE_ADMIN'],
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/merchant-update.component').then(m => m.MerchantUpdateComponent),
    resolve: {
      merchant: MerchantResolve,
    },
    data: {
      authorities: ['ROLE_ADMIN'],
    },
    canActivate: [UserRouteAccessService],
  },
];

export default merchantRoute;
