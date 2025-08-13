import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../payment-transaction.test-samples';

import { PaymentTransactionFormService } from './payment-transaction-form.service';

describe('PaymentTransaction Form Service', () => {
  let service: PaymentTransactionFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PaymentTransactionFormService);
  });

  describe('Service methods', () => {
    describe('createPaymentTransactionFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createPaymentTransactionFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            orderId: expect.any(Object),
            gatewayTransactionId: expect.any(Object),
            status: expect.any(Object),
            statusDescription: expect.any(Object),
            paymentBrand: expect.any(Object),
            amount: expect.any(Object),
            balance: expect.any(Object),
            currency: expect.any(Object),
            replyUrl: expect.any(Object),
            backofficeUrl: expect.any(Object),
            echo: expect.any(Object),
            sendEmail: expect.any(Object),
            signature: expect.any(Object),
            signatureVersion: expect.any(Object),
            requestTimestamp: expect.any(Object),
            requestData: expect.any(Object),
            initialResponseData: expect.any(Object),
            callbackTimestamp: expect.any(Object),
            callbackData: expect.any(Object),
            lastQueryData: expect.any(Object),
            client: expect.any(Object),
            merchant: expect.any(Object),
          }),
        );
      });

      it('passing IPaymentTransaction should create a new form with FormGroup', () => {
        const formGroup = service.createPaymentTransactionFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            orderId: expect.any(Object),
            gatewayTransactionId: expect.any(Object),
            status: expect.any(Object),
            statusDescription: expect.any(Object),
            paymentBrand: expect.any(Object),
            amount: expect.any(Object),
            balance: expect.any(Object),
            currency: expect.any(Object),
            replyUrl: expect.any(Object),
            backofficeUrl: expect.any(Object),
            echo: expect.any(Object),
            sendEmail: expect.any(Object),
            signature: expect.any(Object),
            signatureVersion: expect.any(Object),
            requestTimestamp: expect.any(Object),
            requestData: expect.any(Object),
            initialResponseData: expect.any(Object),
            callbackTimestamp: expect.any(Object),
            callbackData: expect.any(Object),
            lastQueryData: expect.any(Object),
            client: expect.any(Object),
            merchant: expect.any(Object),
          }),
        );
      });
    });

    describe('getPaymentTransaction', () => {
      it('should return NewPaymentTransaction for default PaymentTransaction initial value', () => {
        const formGroup = service.createPaymentTransactionFormGroup(sampleWithNewData);

        const paymentTransaction = service.getPaymentTransaction(formGroup) as any;

        expect(paymentTransaction).toMatchObject(sampleWithNewData);
      });

      it('should return NewPaymentTransaction for empty PaymentTransaction initial value', () => {
        const formGroup = service.createPaymentTransactionFormGroup();

        const paymentTransaction = service.getPaymentTransaction(formGroup) as any;

        expect(paymentTransaction).toMatchObject({});
      });

      it('should return IPaymentTransaction', () => {
        const formGroup = service.createPaymentTransactionFormGroup(sampleWithRequiredData);

        const paymentTransaction = service.getPaymentTransaction(formGroup) as any;

        expect(paymentTransaction).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IPaymentTransaction should not enable id FormControl', () => {
        const formGroup = service.createPaymentTransactionFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewPaymentTransaction should disable id FormControl', () => {
        const formGroup = service.createPaymentTransactionFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: '' });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
