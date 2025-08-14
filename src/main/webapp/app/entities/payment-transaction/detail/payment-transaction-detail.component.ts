import { Component, inject, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { DataUtils } from 'app/core/util/data-util.service';
import { JsonHighlightDirective } from 'app/shared/directive/json-highlight.directive';
import { IPaymentTransaction } from '../payment-transaction.model';

@Component({
  selector: 'jhi-payment-transaction-detail',
  templateUrl: './payment-transaction-detail.component.html',
  styleUrls: ['./payment-transaction-detail.component.scss'],
  imports: [SharedModule, RouterModule, FormatMediumDatetimePipe, JsonHighlightDirective],
})
export class PaymentTransactionDetailComponent {
  paymentTransaction = input<IPaymentTransaction | null>(null);

  protected dataUtils = inject(DataUtils);

  byteSize(base64String: string): string {
    return this.dataUtils.byteSize(base64String);
  }

  openFile(base64String: string, contentType: string | null | undefined): void {
    this.dataUtils.openFile(base64String, contentType);
  }

  previousState(): void {
    window.history.back();
  }
}
