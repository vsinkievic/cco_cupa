import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'Authorities' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'merchant',
    data: { pageTitle: 'Merchants' },
    loadChildren: () => import('./merchant/merchant.routes'),
  },
  {
    path: 'client',
    data: { pageTitle: 'Clients' },
    loadChildren: () => import('./client/client.routes'),
  },
  {
    path: 'client-card',
    data: { pageTitle: 'ClientCards' },
    loadChildren: () => import('./client-card/client-card.routes'),
  },
  {
    path: 'payment-transaction',
    data: { pageTitle: 'PaymentTransactions' },
    loadChildren: () => import('./payment-transaction/payment-transaction.routes'),
  },
  {
    path: 'audit-log',
    data: { pageTitle: 'AuditLogs' },
    loadChildren: () => import('./audit-log/audit-log.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
