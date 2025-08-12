import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { MerchantService } from '../service/merchant.service';
import { IMerchant } from '../merchant.model';
import { MerchantFormService } from './merchant-form.service';

import { MerchantUpdateComponent } from './merchant-update.component';

describe('Merchant Management Update Component', () => {
  let comp: MerchantUpdateComponent;
  let fixture: ComponentFixture<MerchantUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let merchantFormService: MerchantFormService;
  let merchantService: MerchantService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [MerchantUpdateComponent],
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
      .overrideTemplate(MerchantUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(MerchantUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    merchantFormService = TestBed.inject(MerchantFormService);
    merchantService = TestBed.inject(MerchantService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should update editForm', () => {
      const merchant: IMerchant = { id: '16734' };

      activatedRoute.data = of({ merchant });
      comp.ngOnInit();

      expect(comp.merchant).toEqual(merchant);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IMerchant>>();
      const merchant = { id: '23082', version: 1 };
      jest.spyOn(merchantFormService, 'getMerchant').mockReturnValue(merchant);
      jest.spyOn(merchantService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ merchant });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: merchant }));
      saveSubject.complete();

      // THEN
      expect(merchantFormService.getMerchant).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(merchantService.update).toHaveBeenCalledWith(expect.objectContaining(merchant));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IMerchant>>();
      const merchant = { id: '23082' };
      jest.spyOn(merchantFormService, 'getMerchant').mockReturnValue({ id: null, version: null });
      jest.spyOn(merchantService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ merchant: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: merchant }));
      saveSubject.complete();

      // THEN
      expect(merchantFormService.getMerchant).toHaveBeenCalled();
      expect(merchantService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IMerchant>>();
      const merchant = { id: '23082', version: 1 };
      jest.spyOn(merchantService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ merchant });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(merchantService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
