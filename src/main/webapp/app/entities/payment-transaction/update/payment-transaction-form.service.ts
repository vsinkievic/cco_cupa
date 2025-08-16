import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IPaymentTransaction, NewPaymentTransaction } from '../payment-transaction.model';
import { IClient } from 'app/entities/client/client.model';
import { IMerchant } from 'app/entities/merchant/merchant.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IPaymentTransaction for edit and NewPaymentTransactionFormGroupInput for create.
 */
type PaymentTransactionFormGroupInput = IPaymentTransaction | PartialWithRequiredKeyOf<NewPaymentTransaction>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IPaymentTransaction | NewPaymentTransaction> = Omit<T, 'requestTimestamp'> & {
  requestTimestamp?: string | null;
};

type PaymentTransactionFormRawValue = FormValueOf<IPaymentTransaction>;

type NewPaymentTransactionFormRawValue = FormValueOf<NewPaymentTransaction>;

type PaymentTransactionFormDefaults = Pick<NewPaymentTransaction, 'id' | 'paymentFlow' | 'currency'>;

type PaymentTransactionFormGroupContent = {
  id: FormControl<PaymentTransactionFormRawValue['id'] | NewPaymentTransaction['id']>;
  orderId: FormControl<PaymentTransactionFormRawValue['orderId']>;
  gatewayTransactionId: FormControl<PaymentTransactionFormRawValue['gatewayTransactionId']>;
  status: FormControl<PaymentTransactionFormRawValue['status']>;
  statusDescription: FormControl<PaymentTransactionFormRawValue['statusDescription']>;
  paymentBrand: FormControl<PaymentTransactionFormRawValue['paymentBrand']>;
  amount: FormControl<PaymentTransactionFormRawValue['amount']>;
  currency: FormControl<PaymentTransactionFormRawValue['currency']>;
  replyUrl: FormControl<PaymentTransactionFormRawValue['replyUrl']>;
  backofficeUrl: FormControl<PaymentTransactionFormRawValue['backofficeUrl']>;
  echo: FormControl<PaymentTransactionFormRawValue['echo']>;
  paymentFlow: FormControl<PaymentTransactionFormRawValue['paymentFlow']>;
  requestTimestamp: FormControl<PaymentTransactionFormRawValue['requestTimestamp']>;
  clientId: FormControl<PaymentTransactionFormRawValue['clientId']>;
  merchantId: FormControl<PaymentTransactionFormRawValue['merchantId']>;
};

export type PaymentTransactionFormGroup = FormGroup<PaymentTransactionFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class PaymentTransactionFormService {
  private originalPaymentTransaction: IPaymentTransaction | null = null;
  private clientsSharedCollection: IClient[] = [];
  private merchantsSharedCollection: IMerchant[] = [];
  private pendingAutoAssignment: { merchantId: string; currency: string | null | undefined } | null = null;

  createPaymentTransactionFormGroup(paymentTransaction: PaymentTransactionFormGroupInput = { id: null }): PaymentTransactionFormGroup {
    // Store the original payment transaction for later use when preserving fields
    if ('id' in paymentTransaction && paymentTransaction.id !== null) {
      this.originalPaymentTransaction = paymentTransaction;
    } else {
      this.originalPaymentTransaction = null;
    }

    const paymentTransactionRawValue = this.convertPaymentTransactionToPaymentTransactionRawValue({
      ...this.getFormDefaults(),
      ...paymentTransaction,
    });

    const form = new FormGroup<PaymentTransactionFormGroupContent>({
      id: new FormControl(
        { value: paymentTransactionRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      orderId: new FormControl(paymentTransactionRawValue.orderId, {
        validators: [Validators.required],
      }),
      gatewayTransactionId: new FormControl(paymentTransactionRawValue.gatewayTransactionId),
      status: new FormControl(paymentTransactionRawValue.status),
      statusDescription: new FormControl(paymentTransactionRawValue.statusDescription),
      paymentBrand: new FormControl(paymentTransactionRawValue.paymentBrand, {
        validators: [Validators.required],
      }),
      amount: new FormControl(paymentTransactionRawValue.amount, {
        validators: [Validators.required],
      }),
      currency: new FormControl(paymentTransactionRawValue.currency, {
        validators: [Validators.required],
      }),
      replyUrl: new FormControl(paymentTransactionRawValue.replyUrl),
      backofficeUrl: new FormControl(paymentTransactionRawValue.backofficeUrl),
      echo: new FormControl(paymentTransactionRawValue.echo),
      paymentFlow: new FormControl(paymentTransactionRawValue.paymentFlow),
      requestTimestamp: new FormControl(paymentTransactionRawValue.requestTimestamp),
      clientId: new FormControl(paymentTransactionRawValue.clientId, {
        validators: [Validators.required],
      }),
      merchantId: new FormControl(paymentTransactionRawValue.merchantId, {
        validators: [Validators.required],
      }),
    });

    // Apply pending auto-assignment if available
    this.applyPendingAutoAssignment(form);

    return form;
  }

  setSharedCollections(clients: IClient[], merchants: IMerchant[]): void {
    this.clientsSharedCollection = clients;
    this.merchantsSharedCollection = merchants;

    // Auto-assign merchant and currency whenever collections are updated
    // This handles cases where collections are loaded after form creation
    this.triggerAutoAssignment();
  }

  /**
   * Public method to manually trigger auto-assignment for a form.
   * Useful when components want to ensure auto-assignment happens after collections are loaded.
   * @param form The payment transaction form to update
   */
  public triggerAutoAssignmentForForm(form: PaymentTransactionFormGroup): void {
    // First try to apply pending auto-assignment
    this.applyPendingAutoAssignment(form);

    // Then try direct auto-assignment with current collections
    this.autoAssignMerchantAndCurrency(form);
  }

  onClientChange(form: PaymentTransactionFormGroup): void {
    const selectedClientId = form.get('clientId')?.value;
    const selectedClient = this.clientsSharedCollection.find(client => client.id === selectedClientId);

    if (selectedClient) {
      // Update the form with client-related fields
      form.patchValue({
        // Note: We don't set merchantClientId here as it's not in the form
        // The backend service will handle this during save
      });
    }
  }

  /**
   * Auto-assigns merchantId and currency for new payment transactions when there's only one merchant available.
   * This method should be called after setSharedCollections to ensure the collections are populated.
   *
   * Usage example:
   * 1. Call setSharedCollections(clients, merchants) first
   * 2. Create or reset the form
   * 3. Call autoAssignMerchantAndCurrency(form) to auto-populate fields
   *
   * @param form The payment transaction form to update
   */
  autoAssignMerchantAndCurrency(form: PaymentTransactionFormGroup): void {
    // Only auto-assign for new transactions (when id is null or empty)
    const currentId = form.get('id')?.value;
    if (currentId === null || currentId === '') {
      // Check if there's only one merchant available
      if (this.merchantsSharedCollection.length === 1) {
        const singleMerchant = this.merchantsSharedCollection[0];

        // Auto-assign merchantId
        form.patchValue({
          merchantId: singleMerchant.id,
        });

        // Auto-assign currency if merchant has a valid currency
        if (singleMerchant.currency && this.isValidCurrency(singleMerchant.currency)) {
          form.patchValue({
            currency: singleMerchant.currency as keyof typeof import('app/entities/enumerations/currency.model').Currency,
          });
        }
      }
    }
  }

  onMerchantChange(form: PaymentTransactionFormGroup): void {
    const selectedMerchantId = form.get('merchantId')?.value;
    const selectedMerchant = this.merchantsSharedCollection.find(merchant => merchant.id === selectedMerchantId);

    if (selectedMerchant) {
      // Update the form with merchant-related fields if needed
      // For now, we don't set any additional fields as they're not in the form
    }
  }

  getPaymentTransaction(form: PaymentTransactionFormGroup): IPaymentTransaction | NewPaymentTransaction {
    const formValue = form.getRawValue() as PaymentTransactionFormRawValue | NewPaymentTransactionFormRawValue;
    const convertedValue = this.convertPaymentTransactionRawValueToPaymentTransaction(formValue);

    // If editing an existing transaction, preserve all fields not in the form
    if (this.originalPaymentTransaction && convertedValue.id !== null) {
      const result: IPaymentTransaction = {
        ...this.originalPaymentTransaction,
        ...convertedValue,
        // Ensure the form values override the original values
        orderId: convertedValue.orderId,
        gatewayTransactionId: convertedValue.gatewayTransactionId,
        status: convertedValue.status,
        statusDescription: convertedValue.statusDescription,
        paymentBrand: convertedValue.paymentBrand,
        amount: convertedValue.amount,
        currency: convertedValue.currency,
        replyUrl: convertedValue.replyUrl,
        backofficeUrl: convertedValue.backofficeUrl,
        echo: convertedValue.echo,
        paymentFlow: convertedValue.paymentFlow,
        requestTimestamp: convertedValue.requestTimestamp,
        clientId: convertedValue.clientId,
        merchantId: convertedValue.merchantId,
      };

      // Handle client and merchant enrichment based on current form values
      if (convertedValue.clientId) {
        const selectedClient = this.clientsSharedCollection.find(client => client.id === convertedValue.clientId);
        if (selectedClient) {
          result.merchantClientId = selectedClient.merchantClientId;
          result.clientName = selectedClient.name;
        }
      }

      if (convertedValue.merchantId) {
        const selectedMerchant = this.merchantsSharedCollection.find(merchant => merchant.id === convertedValue.merchantId);
        if (selectedMerchant) {
          result.merchantName = selectedMerchant.name;
        }
      }

      return result;
    }

    // For new transactions, ensure all required fields are set
    const newTransaction: NewPaymentTransaction = {
      ...convertedValue,
      id: null,
      version: null,
    };

    return newTransaction;
  }

  resetForm(form: PaymentTransactionFormGroup, paymentTransaction: PaymentTransactionFormGroupInput): void {
    // Store the original payment transaction for later use when preserving fields
    if ('id' in paymentTransaction && paymentTransaction.id !== null) {
      this.originalPaymentTransaction = paymentTransaction;
    } else {
      this.originalPaymentTransaction = null;
    }

    const paymentTransactionRawValue = this.convertPaymentTransactionToPaymentTransactionRawValue({
      ...this.getFormDefaults(),
      ...paymentTransaction,
    });
    form.reset(
      {
        ...paymentTransactionRawValue,
        id: { value: paymentTransactionRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/currency */,
    );

    // Auto-assign merchant and currency for new transactions after form reset
    if (this.originalPaymentTransaction === null) {
      this.autoAssignMerchantAndCurrency(form);
    }
  }

  private getFormDefaults(): PaymentTransactionFormDefaults {
    return {
      id: null,
      paymentFlow: 'EMAIL',
      currency: 'USD',
    };
  }

  private convertPaymentTransactionRawValueToPaymentTransaction(
    rawPaymentTransaction: PaymentTransactionFormRawValue | NewPaymentTransactionFormRawValue,
  ): IPaymentTransaction | NewPaymentTransaction {
    return {
      ...rawPaymentTransaction,
      requestTimestamp: dayjs(rawPaymentTransaction.requestTimestamp, DATE_TIME_FORMAT),
    } as IPaymentTransaction | NewPaymentTransaction;
  }

  private convertPaymentTransactionToPaymentTransactionRawValue(
    paymentTransaction: IPaymentTransaction | (Partial<NewPaymentTransaction> & PaymentTransactionFormDefaults),
  ): PaymentTransactionFormRawValue | PartialWithRequiredKeyOf<NewPaymentTransactionFormRawValue> {
    return {
      ...paymentTransaction,
      requestTimestamp: paymentTransaction.requestTimestamp ? paymentTransaction.requestTimestamp.format(DATE_TIME_FORMAT) : undefined,
    };
  }

  /**
   * Triggers auto-assignment for all active forms when collections are updated.
   * This handles timing issues where collections are loaded after form creation.
   */
  private triggerAutoAssignment(): void {
    // If we have exactly one merchant and no active form is set,
    // we'll store this information for when a form is created
    if (this.merchantsSharedCollection.length === 1) {
      this.pendingAutoAssignment = {
        merchantId: this.merchantsSharedCollection[0].id,
        currency: this.merchantsSharedCollection[0].currency,
      };
    } else {
      this.pendingAutoAssignment = null;
    }
  }

  /**
   * Applies pending auto-assignment when a form is created.
   * This ensures auto-assignment works even when form is created before collections.
   */
  private applyPendingAutoAssignment(form: PaymentTransactionFormGroup): void {
    if (this.pendingAutoAssignment && this.originalPaymentTransaction === null) {
      const currentId = form.get('id')?.value;
      if (currentId === null || currentId === '') {
        // Auto-assign merchantId
        form.patchValue({
          merchantId: this.pendingAutoAssignment.merchantId,
        });

        // Auto-assign currency if merchant has a valid currency
        if (this.pendingAutoAssignment.currency && this.isValidCurrency(this.pendingAutoAssignment.currency)) {
          form.patchValue({
            currency: this.pendingAutoAssignment.currency as keyof typeof import('app/entities/enumerations/currency.model').Currency,
          });
        }
      }
    }
  }

  /**
   * Checks if the given currency string is a valid currency enum value.
   * @param currency The currency string to validate
   * @returns true if the currency is valid, false otherwise
   */
  private isValidCurrency(currency: string): boolean {
    const validCurrencies = ['AUD', 'USD', 'HKD', 'CAD', 'CHF', 'CNY', 'EUR', 'GBP', 'JPY', 'KRW', 'MOP', 'RUB', 'SGD', 'TWD'];
    return validCurrencies.includes(currency);
  }
}
