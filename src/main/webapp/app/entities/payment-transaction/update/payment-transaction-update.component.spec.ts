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
    it('should call Client query and add missing value', () => {
      const paymentTransaction: IPaymentTransaction = { id: 1571 };
      const client: IClient = { id: 26282 };
      paymentTransaction.client = client;

      const clientCollection: IClient[] = [{ id: 26282 }];
      jest.spyOn(clientService, 'query').mockReturnValue(of(new HttpResponse({ body: clientCollection })));
      const additionalClients = [client];
      const expectedCollection: IClient[] = [...additionalClients, ...clientCollection];
      jest.spyOn(clientService, 'addClientToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ paymentTransaction });
      comp.ngOnInit();

      expect(clientService.query).toHaveBeenCalled();
      expect(clientService.addClientToCollectionIfMissing).toHaveBeenCalledWith(
        clientCollection,
        ...additionalClients.map(expect.objectContaining),
      );
      expect(comp.clientsSharedCollection).toEqual(expectedCollection);
    });

    it('should call Merchant query and add missing value', () => {
      const paymentTransaction: IPaymentTransaction = { id: 1571 };
      const merchant: IMerchant = { id: 23082 };
      paymentTransaction.merchant = merchant;

      const merchantCollection: IMerchant[] = [{ id: 23082 }];
      jest.spyOn(merchantService, 'query').mockReturnValue(of(new HttpResponse({ body: merchantCollection })));
      const additionalMerchants = [merchant];
      const expectedCollection: IMerchant[] = [...additionalMerchants, ...merchantCollection];
      jest.spyOn(merchantService, 'addMerchantToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ paymentTransaction });
      comp.ngOnInit();

      expect(merchantService.query).toHaveBeenCalled();
      expect(merchantService.addMerchantToCollectionIfMissing).toHaveBeenCalledWith(
        merchantCollection,
        ...additionalMerchants.map(expect.objectContaining),
      );
      expect(comp.merchantsSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const paymentTransaction: IPaymentTransaction = { id: 1571 };
      const client: IClient = { id: 26282 };
      paymentTransaction.client = client;
      const merchant: IMerchant = { id: 23082 };
      paymentTransaction.merchant = merchant;

      activatedRoute.data = of({ paymentTransaction });
      comp.ngOnInit();

      expect(comp.clientsSharedCollection).toContainEqual(client);
      expect(comp.merchantsSharedCollection).toContainEqual(merchant);
      expect(comp.paymentTransaction).toEqual(paymentTransaction);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPaymentTransaction>>();
      const paymentTransaction = { id: 10661 };
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
      const paymentTransaction = { id: 10661 };
      jest.spyOn(paymentTransactionFormService, 'getPaymentTransaction').mockReturnValue({ id: null });
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
      expect(paymentTransactionService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPaymentTransaction>>();
      const paymentTransaction = { id: 10661 };
      jest.spyOn(paymentTransactionService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ paymentTransaction });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(paymentTransactionService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareClient', () => {
      it('should forward to clientService', () => {
        const entity = { id: 26282 };
        const entity2 = { id: 16836 };
        jest.spyOn(clientService, 'compareClient');
        comp.compareClient(entity, entity2);
        expect(clientService.compareClient).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareMerchant', () => {
      it('should forward to merchantService', () => {
        const entity = { id: 23082 };
        const entity2 = { id: 16734 };
        jest.spyOn(merchantService, 'compareMerchant');
        comp.compareMerchant(entity, entity2);
        expect(merchantService.compareMerchant).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
