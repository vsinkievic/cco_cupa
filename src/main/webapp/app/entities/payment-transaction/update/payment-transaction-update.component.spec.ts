import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IClient } from 'app/entities/client/client.model';
import { ClientService } from 'app/entities/client/service/client.service';
import { IMerchant } from 'app/entities/merchant/merchant.model';
import { MerchantService } from 'app/entities/merchant/service/merchant.service';
import { IPaymentTransaction } from '../payment-transaction.model';
import { PaymentTransactionService } from '../service/payment-transaction.service';
import { PaymentTransactionFormService } from './payment-transaction-form.service';

import { PaymentTransactionUpdateComponent } from './payment-transaction-update.component';

describe('PaymentTransaction Management Update Component', () => {
  let comp: PaymentTransactionUpdateComponent;
  let fixture: ComponentFixture<PaymentTransactionUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let paymentTransactionFormService: PaymentTransactionFormService;
  let paymentTransactionService: PaymentTransactionService;
  let clientService: ClientService;
  let merchantService: MerchantService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [PaymentTransactionUpdateComponent],
      providers: [
        provideHttpClient(),
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(PaymentTransactionUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(PaymentTransactionUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    paymentTransactionFormService = TestBed.inject(PaymentTransactionFormService);
    paymentTransactionService = TestBed.inject(PaymentTransactionService);
    clientService = TestBed.inject(ClientService);
    merchantService = TestBed.inject(MerchantService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Client query and load clients', () => {
      const paymentTransaction: IPaymentTransaction = { id: '1571', clientId: '26282' };

      const clientCollection: IClient[] = [{ id: '26282' }];
      jest.spyOn(clientService, 'query').mockReturnValue(of(new HttpResponse({ body: clientCollection })));

      activatedRoute.data = of({ paymentTransaction });
      comp.ngOnInit();

      expect(clientService.query).toHaveBeenCalled();
      expect(comp.clientsSharedCollection).toEqual(clientCollection);
    });

    it('should call Merchant query and load merchants', () => {
      const paymentTransaction: IPaymentTransaction = { id: '1571', merchantId: '23082' };

      const merchantCollection: IMerchant[] = [{ id: '23082' }];
      jest.spyOn(merchantService, 'query').mockReturnValue(of(new HttpResponse({ body: merchantCollection })));

      activatedRoute.data = of({ paymentTransaction });
      comp.ngOnInit();

      expect(merchantService.query).toHaveBeenCalled();
      expect(comp.merchantsSharedCollection).toEqual(merchantCollection);
    });

    it('should update editForm', () => {
      const paymentTransaction: IPaymentTransaction = { id: '1571', clientId: '26282', merchantId: '23082' };

      activatedRoute.data = of({ paymentTransaction });
      comp.ngOnInit();

      expect(comp.paymentTransaction).toEqual(paymentTransaction);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPaymentTransaction>>();
      const paymentTransaction = { id: '10661' };
      jest.spyOn(paymentTransactionFormService, 'getPaymentTransaction').mockReturnValue(paymentTransaction);
      jest.spyOn(paymentTransactionService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ paymentTransaction });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: paymentTransaction }));
      saveSubject.complete();

      // THEN
      expect(paymentTransactionFormService.getPaymentTransaction).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(paymentTransactionService.update).toHaveBeenCalledWith(expect.objectContaining(paymentTransaction));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPaymentTransaction>>();
      const paymentTransaction = { id: '10661' };
      jest.spyOn(paymentTransactionFormService, 'getPaymentTransaction').mockReturnValue({ id: null, version: null });
      jest.spyOn(paymentTransactionService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ paymentTransaction: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: paymentTransaction }));
      saveSubject.complete();

      // THEN
      expect(paymentTransactionFormService.getPaymentTransaction).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(paymentTransactionService.create).toHaveBeenCalledWith(expect.objectContaining({ id: null, version: null }));
      expect(comp.isSaving).toEqual(false);
    });
  });

  describe('onClientChange', () => {
    it('should not update form fields when client is selected', () => {
      // GIVEN
      const client: IClient = { id: '123', merchantClientId: 'merchant-client-123', name: 'Test Client' };
      comp.clientsSharedCollection = [client];
      comp.editForm.patchValue({ clientId: '123' });

      // WHEN
      comp.onClientChange();

      // THEN
      // The form service now handles client enrichment during save, not in the form
      // These fields are not in the form anymore
      expect(comp.editForm.get('merchantClientId')?.value).toBeUndefined();
      expect(comp.editForm.get('clientName')?.value).toBeUndefined();
    });

    it('should not update fields when no client is selected', () => {
      // GIVEN
      comp.editForm.patchValue({ clientId: null });

      // WHEN
      comp.onClientChange();

      // THEN
      expect(comp.editForm.get('merchantClientId')?.value).toBeUndefined();
      expect(comp.editForm.get('clientName')?.value).toBeUndefined();
    });
  });

  describe('onMerchantChange', () => {
    it('should not update form fields when merchant is selected', () => {
      // GIVEN
      const merchant: IMerchant = { id: '456', name: 'Test Merchant' };
      comp.merchantsSharedCollection = [merchant];
      comp.editForm.patchValue({ merchantId: '456' });

      // WHEN
      comp.onMerchantChange();

      // THEN
      // The form service now handles merchant enrichment during save, not in the form
      // These fields are not in the form anymore
      expect(comp.editForm.get('merchantName')?.value).toBeUndefined();
    });

    it('should not update field when no merchant is selected', () => {
      // GIVEN
      comp.editForm.patchValue({ merchantId: null });

      // WHEN
      comp.onMerchantChange();

      // THEN
      expect(comp.editForm.get('merchantName')?.value).toBeUndefined();
    });
  });
});
