import { Component, inject, input, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { DataUtils } from 'app/core/util/data-util.service';
import { JsonHighlightDirective } from 'app/shared/directive/json-highlight.directive';
import { IPaymentTransaction } from '../payment-transaction.model';
import { PaymentTransactionService } from '../service/payment-transaction.service';
import { TransactionStatus } from 'app/entities/enumerations/transaction-status.model';

@Component({
  selector: 'jhi-payment-transaction-detail',
  templateUrl: './payment-transaction-detail.component.html',
  styleUrls: ['./payment-transaction-detail.component.scss'],
  imports: [SharedModule, RouterModule, FormatMediumDatetimePipe, JsonHighlightDirective],
})
export class PaymentTransactionDetailComponent {
  paymentTransaction = input<IPaymentTransaction | null>(null);

  protected dataUtils = inject(DataUtils);
  protected paymentTransactionService = inject(PaymentTransactionService);

  protected isQuerying = signal(false);

  byteSize(base64String: string): string {
    return this.dataUtils.byteSize(base64String);
  }

  openFile(base64String: string, contentType: string | null | undefined): void {
    this.dataUtils.openFile(base64String, contentType);
  }

  previousState(): void {
    window.history.back();
  }

  /**
   * Check if the query payment button should be enabled
   * Enabled for statuses: RECEIVED, PENDING, AWAITING_CALLBACK
   */
  isQueryButtonEnabled(): boolean {
    const transaction = this.paymentTransaction();
    if (!transaction?.status) {
      return false;
    }

    const enabledStatuses = [TransactionStatus.RECEIVED, TransactionStatus.PENDING, TransactionStatus.AWAITING_CALLBACK];

    return enabledStatuses.includes(transaction.status as TransactionStatus);
  }

  /**
   * Query payment from gateway
   */
  queryPaymentFromGateway(): void {
    const transaction = this.paymentTransaction();
    if (!transaction?.id) {
      return;
    }

    this.isQuerying.set(true);

    this.paymentTransactionService
      .queryPaymentFromGateway(transaction.id)
      .pipe(finalize(() => this.isQuerying.set(false)))
      .subscribe({
        next(response) {
          if (response.body) {
            // Update the payment transaction with the new data
            // Note: In a real application, you might want to use a service to manage state
            // or emit an event to refresh the data from the parent component
            window.location.reload(); // Simple approach for now
          }
        },
        error(error) {
          console.error('Error querying payment from gateway:', error);
          // You might want to show an error message to the user here
        },
      });
  }
}
