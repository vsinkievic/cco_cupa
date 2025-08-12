import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { ClientService } from '../service/client.service';
import { IClient } from '../client.model';
import { ClientFormService } from './client-form.service';

import { ClientUpdateComponent } from './client-update.component';

describe('Client Management Update Component', () => {
  let comp: ClientUpdateComponent;
  let fixture: ComponentFixture<ClientUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let clientFormService: ClientFormService;
  let clientService: ClientService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ClientUpdateComponent],
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
      .overrideTemplate(ClientUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(ClientUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    clientFormService = TestBed.inject(ClientFormService);
    clientService = TestBed.inject(ClientService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should update editForm', () => {
      const client: IClient = { id: '16836' };

      activatedRoute.data = of({ client });
      comp.ngOnInit();

      expect(comp.client).toEqual(client);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IClient>>();
      const client = { id: '26282' };
      jest.spyOn(clientFormService, 'getClient').mockReturnValue(client);
      jest.spyOn(clientService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ client });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: client }));
      saveSubject.complete();

      // THEN
      expect(clientFormService.getClient).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(clientService.update).toHaveBeenCalledWith(expect.objectContaining(client));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IClient>>();
      const client = { id: '26282' };
      jest.spyOn(clientFormService, 'getClient').mockReturnValue({ id: null });
      jest.spyOn(clientService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ client: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: client }));
      saveSubject.complete();

      // THEN
      expect(clientFormService.getClient).toHaveBeenCalled();
      expect(clientService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IClient>>();
      const client = { id: '26282' };
      jest.spyOn(clientService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ client });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(clientService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('onMerchantChange', () => {
    it('should set merchantId when merchant is selected', () => {
      // GIVEN
      const merchant = { id: '123', name: 'Test Merchant' };
      comp.merchantsSharedCollection = [merchant];
      const event = { target: { value: '123' } } as any;
      jest.spyOn(comp.editForm, 'patchValue');

      // WHEN
      comp.onMerchantChange(event);

      // THEN
      expect(comp.editForm.patchValue).toHaveBeenCalledWith({ merchantId: '123' });
    });

    it('should clear merchantId when no merchant is selected', () => {
      // GIVEN
      const event = { target: { value: '' } } as any;
      jest.spyOn(comp.editForm, 'patchValue');

      // WHEN
      comp.onMerchantChange(event);

      // THEN
      expect(comp.editForm.patchValue).toHaveBeenCalledWith({ merchantId: null });
    });
  });

  describe('trackMerchant', () => {
    it('should return merchant id', () => {
      // GIVEN
      const merchant = { id: '123', name: 'Test Merchant' };

      // WHEN
      const result = comp.trackMerchant(0, merchant);

      // THEN
      expect(result).toBe('123');
    });
  });
});
