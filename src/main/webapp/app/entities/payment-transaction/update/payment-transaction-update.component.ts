import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AlertError } from 'app/shared/alert/alert-error.model';
import { EventManager, EventWithContent } from 'app/core/util/event-manager.service';
import { DataUtils, FileLoadError } from 'app/core/util/data-util.service';
import { IClient } from 'app/entities/client/client.model';
import { ClientService } from 'app/entities/client/service/client.service';
import { IMerchant } from 'app/entities/merchant/merchant.model';
import { MerchantService } from 'app/entities/merchant/service/merchant.service';
import { TransactionStatus } from 'app/entities/enumerations/transaction-status.model';
import { PaymentBrand } from 'app/entities/enumerations/payment-brand.model';
import { Currency } from 'app/entities/enumerations/currency.model';
import { PaymentTransactionService } from '../service/payment-transaction.service';
import { IPaymentTransaction } from '../payment-transaction.model';
import { PaymentTransactionFormGroup, PaymentTransactionFormService } from './payment-transaction-form.service';

@Component({
  selector: 'jhi-payment-transaction-update',
  templateUrl: './payment-transaction-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class PaymentTransactionUpdateComponent implements OnInit {
  isSaving = false;
  paymentTransaction: IPaymentTransaction | null = null;
  transactionStatusValues = Object.keys(TransactionStatus);
  paymentBrandValues = Object.keys(PaymentBrand);
  currencyValues = Object.keys(Currency);

  clientsSharedCollection: IClient[] = [];
  merchantsSharedCollection: IMerchant[] = [];

  protected dataUtils = inject(DataUtils);
  protected eventManager = inject(EventManager);
  protected paymentTransactionService = inject(PaymentTransactionService);
  protected paymentTransactionFormService = inject(PaymentTransactionFormService);
  protected clientService = inject(ClientService);
  protected merchantService = inject(MerchantService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: PaymentTransactionFormGroup = this.paymentTransactionFormService.createPaymentTransactionFormGroup();

  compareClient = (o1: IClient | null, o2: IClient | null): boolean => this.clientService.compareClient(o1, o2);

  compareMerchant = (o1: IMerchant | null, o2: IMerchant | null): boolean => this.merchantService.compareMerchant(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ paymentTransaction }) => {
      this.paymentTransaction = paymentTransaction;
      if (paymentTransaction) {
        this.updateForm(paymentTransaction);
      }

      this.loadRelationshipsOptions();
    });
  }

  byteSize(base64String: string): string {
    return this.dataUtils.byteSize(base64String);
  }

  openFile(base64String: string, contentType: string | null | undefined): void {
    this.dataUtils.openFile(base64String, contentType);
  }

  setFileData(event: Event, field: string, isImage: boolean): void {
    this.dataUtils.loadFileToForm(event, this.editForm, field, isImage).subscribe({
      error: (err: FileLoadError) =>
        this.eventManager.broadcast(new EventWithContent<AlertError>('cupaApp.error', { message: err.message })),
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const paymentTransaction = this.paymentTransactionFormService.getPaymentTransaction(this.editForm);
    if (paymentTransaction.id !== null) {
      this.subscribeToSaveResponse(this.paymentTransactionService.update(paymentTransaction));
    } else {
      this.subscribeToSaveResponse(this.paymentTransactionService.create(paymentTransaction));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPaymentTransaction>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(paymentTransaction: IPaymentTransaction): void {
    this.paymentTransaction = paymentTransaction;
    this.paymentTransactionFormService.resetForm(this.editForm, paymentTransaction);

    this.clientsSharedCollection = this.clientService.addClientToCollectionIfMissing<IClient>(
      this.clientsSharedCollection,
      paymentTransaction.client,
    );
    this.merchantsSharedCollection = this.merchantService.addMerchantToCollectionIfMissing<IMerchant>(
      this.merchantsSharedCollection,
      paymentTransaction.merchant,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.clientService
      .query()
      .pipe(map((res: HttpResponse<IClient[]>) => res.body ?? []))
      .pipe(
        map((clients: IClient[]) => this.clientService.addClientToCollectionIfMissing<IClient>(clients, this.paymentTransaction?.client)),
      )
      .subscribe((clients: IClient[]) => (this.clientsSharedCollection = clients));

    this.merchantService
      .query()
      .pipe(map((res: HttpResponse<IMerchant[]>) => res.body ?? []))
      .pipe(
        map((merchants: IMerchant[]) =>
          this.merchantService.addMerchantToCollectionIfMissing<IMerchant>(merchants, this.paymentTransaction?.merchant),
        ),
      )
      .subscribe((merchants: IMerchant[]) => (this.merchantsSharedCollection = merchants));
  }
}
