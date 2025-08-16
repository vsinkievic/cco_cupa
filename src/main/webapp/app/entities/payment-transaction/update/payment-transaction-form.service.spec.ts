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
            currency: expect.any(Object),
            replyUrl: expect.any(Object),
            backofficeUrl: expect.any(Object),
            echo: expect.any(Object),
            paymentFlow: expect.any(Object),
            requestTimestamp: expect.any(Object),
            clientId: expect.any(Object),
            merchantId: expect.any(Object),
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
            currency: expect.any(Object),
            replyUrl: expect.any(Object),
            backofficeUrl: expect.any(Object),
            echo: expect.any(Object),
            paymentFlow: expect.any(Object),
            requestTimestamp: expect.any(Object),
            clientId: expect.any(Object),
            merchantId: expect.any(Object),
          }),
        );
      });
    });

    describe('getPaymentTransaction', () => {
      it('should return NewPaymentTransaction for default PaymentTransaction initial value', () => {
        const formGroup = service.createPaymentTransactionFormGroup(sampleWithNewData);

        const paymentTransaction = service.getPaymentTransaction(formGroup) as any;

        // The form service should return the version field even though it's not in the form
        expect(paymentTransaction).toMatchObject({
          ...sampleWithNewData,
          version: null,
        });
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

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
