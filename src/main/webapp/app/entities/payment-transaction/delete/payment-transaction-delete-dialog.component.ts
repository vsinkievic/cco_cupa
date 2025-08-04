import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IPaymentTransaction } from '../payment-transaction.model';
import { PaymentTransactionService } from '../service/payment-transaction.service';

@Component({
  templateUrl: './payment-transaction-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class PaymentTransactionDeleteDialogComponent {
  paymentTransaction?: IPaymentTransaction;

  protected paymentTransactionService = inject(PaymentTransactionService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.paymentTransactionService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
