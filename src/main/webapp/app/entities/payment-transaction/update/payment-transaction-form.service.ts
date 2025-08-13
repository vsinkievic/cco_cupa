import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IPaymentTransaction, NewPaymentTransaction } from '../payment-transaction.model';

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
type FormValueOf<T extends IPaymentTransaction | NewPaymentTransaction> = Omit<T, 'requestTimestamp' | 'callbackTimestamp'> & {
  requestTimestamp?: string | null;
  callbackTimestamp?: string | null;
};

type PaymentTransactionFormRawValue = FormValueOf<IPaymentTransaction>;

type NewPaymentTransactionFormRawValue = FormValueOf<NewPaymentTransaction>;

type PaymentTransactionFormDefaults = Pick<
  NewPaymentTransaction,
  'id' | 'paymentFlow' | 'requestTimestamp' | 'callbackTimestamp' | 'version'
>;

type PaymentTransactionFormGroupContent = {
  id: FormControl<PaymentTransactionFormRawValue['id'] | NewPaymentTransaction['id']>;
  orderId: FormControl<PaymentTransactionFormRawValue['orderId']>;
  gatewayTransactionId: FormControl<PaymentTransactionFormRawValue['gatewayTransactionId']>;
  status: FormControl<PaymentTransactionFormRawValue['status']>;
  statusDescription: FormControl<PaymentTransactionFormRawValue['statusDescription']>;
  paymentBrand: FormControl<PaymentTransactionFormRawValue['paymentBrand']>;
  amount: FormControl<PaymentTransactionFormRawValue['amount']>;
  balance: FormControl<PaymentTransactionFormRawValue['balance']>;
  currency: FormControl<PaymentTransactionFormRawValue['currency']>;
  replyUrl: FormControl<PaymentTransactionFormRawValue['replyUrl']>;
  backofficeUrl: FormControl<PaymentTransactionFormRawValue['backofficeUrl']>;
  echo: FormControl<PaymentTransactionFormRawValue['echo']>;
  paymentFlow: FormControl<PaymentTransactionFormRawValue['paymentFlow']>;
  signature: FormControl<PaymentTransactionFormRawValue['signature']>;
  signatureVersion: FormControl<PaymentTransactionFormRawValue['signatureVersion']>;
  requestTimestamp: FormControl<PaymentTransactionFormRawValue['requestTimestamp']>;
  requestData: FormControl<PaymentTransactionFormRawValue['requestData']>;
  initialResponseData: FormControl<PaymentTransactionFormRawValue['initialResponseData']>;
  callbackTimestamp: FormControl<PaymentTransactionFormRawValue['callbackTimestamp']>;
  callbackData: FormControl<PaymentTransactionFormRawValue['callbackData']>;
  lastQueryData: FormControl<PaymentTransactionFormRawValue['lastQueryData']>;
  client: FormControl<PaymentTransactionFormRawValue['client']>;
  merchant: FormControl<PaymentTransactionFormRawValue['merchant']>;
  version: FormControl<PaymentTransactionFormRawValue['version']>;
};

export type PaymentTransactionFormGroup = FormGroup<PaymentTransactionFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class PaymentTransactionFormService {
  createPaymentTransactionFormGroup(paymentTransaction: PaymentTransactionFormGroupInput = { id: '' }): PaymentTransactionFormGroup {
    const paymentTransactionRawValue = this.convertPaymentTransactionToPaymentTransactionRawValue({
      ...this.getFormDefaults(),
      ...paymentTransaction,
    });
    return new FormGroup<PaymentTransactionFormGroupContent>({
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
      status: new FormControl(paymentTransactionRawValue.status, {
        validators: [Validators.required],
      }),
      statusDescription: new FormControl(paymentTransactionRawValue.statusDescription),
      paymentBrand: new FormControl(paymentTransactionRawValue.paymentBrand, {
        validators: [Validators.required],
      }),
      amount: new FormControl(paymentTransactionRawValue.amount, {
        validators: [Validators.required],
      }),
      balance: new FormControl(paymentTransactionRawValue.balance),
      currency: new FormControl(paymentTransactionRawValue.currency, {
        validators: [Validators.required],
      }),
      replyUrl: new FormControl(paymentTransactionRawValue.replyUrl),
      backofficeUrl: new FormControl(paymentTransactionRawValue.backofficeUrl),
      echo: new FormControl(paymentTransactionRawValue.echo),
      paymentFlow: new FormControl(paymentTransactionRawValue.paymentFlow),
      signature: new FormControl(paymentTransactionRawValue.signature),
      signatureVersion: new FormControl(paymentTransactionRawValue.signatureVersion),
      requestTimestamp: new FormControl(paymentTransactionRawValue.requestTimestamp, {
        validators: [Validators.required],
      }),
      requestData: new FormControl(paymentTransactionRawValue.requestData),
      initialResponseData: new FormControl(paymentTransactionRawValue.initialResponseData),
      callbackTimestamp: new FormControl(paymentTransactionRawValue.callbackTimestamp),
      callbackData: new FormControl(paymentTransactionRawValue.callbackData),
      lastQueryData: new FormControl(paymentTransactionRawValue.lastQueryData),
      client: new FormControl(paymentTransactionRawValue.client),
      merchant: new FormControl(paymentTransactionRawValue.merchant, {
        validators: [Validators.required],
      }),
      version: new FormControl(paymentTransactionRawValue.version),
    });
  }

  getPaymentTransaction(form: PaymentTransactionFormGroup): IPaymentTransaction | NewPaymentTransaction {
    return this.convertPaymentTransactionRawValueToPaymentTransaction(
      form.getRawValue() as PaymentTransactionFormRawValue | NewPaymentTransactionFormRawValue,
    );
  }

  resetForm(form: PaymentTransactionFormGroup, paymentTransaction: PaymentTransactionFormGroupInput): void {
    const paymentTransactionRawValue = this.convertPaymentTransactionToPaymentTransactionRawValue({
      ...this.getFormDefaults(),
      ...paymentTransaction,
    });
    form.reset(
      {
        ...paymentTransactionRawValue,
        id: { value: paymentTransactionRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): PaymentTransactionFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      paymentFlow: 'EMAIL',
      requestTimestamp: currentTime,
      callbackTimestamp: currentTime,
      version: null,
    };
  }

  private convertPaymentTransactionRawValueToPaymentTransaction(
    rawPaymentTransaction: PaymentTransactionFormRawValue | NewPaymentTransactionFormRawValue,
  ): IPaymentTransaction | NewPaymentTransaction {
    return {
      ...rawPaymentTransaction,
      requestTimestamp: dayjs(rawPaymentTransaction.requestTimestamp, DATE_TIME_FORMAT),
      callbackTimestamp: dayjs(rawPaymentTransaction.callbackTimestamp, DATE_TIME_FORMAT),
    };
  }

  private convertPaymentTransactionToPaymentTransactionRawValue(
    paymentTransaction: IPaymentTransaction | (Partial<NewPaymentTransaction> & PaymentTransactionFormDefaults),
  ): PaymentTransactionFormRawValue | PartialWithRequiredKeyOf<NewPaymentTransactionFormRawValue> {
    return {
      ...paymentTransaction,
      requestTimestamp: paymentTransaction.requestTimestamp ? paymentTransaction.requestTimestamp.format(DATE_TIME_FORMAT) : undefined,
      callbackTimestamp: paymentTransaction.callbackTimestamp ? paymentTransaction.callbackTimestamp.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
