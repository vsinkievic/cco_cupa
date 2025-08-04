import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import PaymentTransactionResolve from './route/payment-transaction-routing-resolve.service';

const paymentTransactionRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/payment-transaction.component').then(m => m.PaymentTransactionComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/payment-transaction-detail.component').then(m => m.PaymentTransactionDetailComponent),
    resolve: {
      paymentTransaction: PaymentTransactionResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/payment-transaction-update.component').then(m => m.PaymentTransactionUpdateComponent),
    resolve: {
      paymentTransaction: PaymentTransactionResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/payment-transaction-update.component').then(m => m.PaymentTransactionUpdateComponent),
    resolve: {
      paymentTransaction: PaymentTransactionResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default paymentTransactionRoute;
