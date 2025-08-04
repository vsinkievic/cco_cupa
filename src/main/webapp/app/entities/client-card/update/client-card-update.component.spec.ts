import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IClient } from 'app/entities/client/client.model';
import { ClientService } from 'app/entities/client/service/client.service';
import { ClientCardService } from '../service/client-card.service';
import { IClientCard } from '../client-card.model';
import { ClientCardFormService } from './client-card-form.service';

import { ClientCardUpdateComponent } from './client-card-update.component';

describe('ClientCard Management Update Component', () => {
  let comp: ClientCardUpdateComponent;
  let fixture: ComponentFixture<ClientCardUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let clientCardFormService: ClientCardFormService;
  let clientCardService: ClientCardService;
  let clientService: ClientService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ClientCardUpdateComponent],
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
      .overrideTemplate(ClientCardUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(ClientCardUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    clientCardFormService = TestBed.inject(ClientCardFormService);
    clientCardService = TestBed.inject(ClientCardService);
    clientService = TestBed.inject(ClientService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Client query and add missing value', () => {
      const clientCard: IClientCard = { id: 26157 };
      const client: IClient = { id: 26282 };
      clientCard.client = client;

      const clientCollection: IClient[] = [{ id: 26282 }];
      jest.spyOn(clientService, 'query').mockReturnValue(of(new HttpResponse({ body: clientCollection })));
      const additionalClients = [client];
      const expectedCollection: IClient[] = [...additionalClients, ...clientCollection];
      jest.spyOn(clientService, 'addClientToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ clientCard });
      comp.ngOnInit();

      expect(clientService.query).toHaveBeenCalled();
      expect(clientService.addClientToCollectionIfMissing).toHaveBeenCalledWith(
        clientCollection,
        ...additionalClients.map(expect.objectContaining),
      );
      expect(comp.clientsSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const clientCard: IClientCard = { id: 26157 };
      const client: IClient = { id: 26282 };
      clientCard.client = client;

      activatedRoute.data = of({ clientCard });
      comp.ngOnInit();

      expect(comp.clientsSharedCollection).toContainEqual(client);
      expect(comp.clientCard).toEqual(clientCard);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IClientCard>>();
      const clientCard = { id: 15916 };
      jest.spyOn(clientCardFormService, 'getClientCard').mockReturnValue(clientCard);
      jest.spyOn(clientCardService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ clientCard });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: clientCard }));
      saveSubject.complete();

      // THEN
      expect(clientCardFormService.getClientCard).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(clientCardService.update).toHaveBeenCalledWith(expect.objectContaining(clientCard));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IClientCard>>();
      const clientCard = { id: 15916 };
      jest.spyOn(clientCardFormService, 'getClientCard').mockReturnValue({ id: null });
      jest.spyOn(clientCardService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ clientCard: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: clientCard }));
      saveSubject.complete();

      // THEN
      expect(clientCardFormService.getClientCard).toHaveBeenCalled();
      expect(clientCardService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IClientCard>>();
      const clientCard = { id: 15916 };
      jest.spyOn(clientCardService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ clientCard });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(clientCardService.update).toHaveBeenCalled();
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
  });
});
