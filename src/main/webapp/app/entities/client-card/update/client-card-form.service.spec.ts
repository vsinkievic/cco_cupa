import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../client-card.test-samples';

import { ClientCardFormService } from './client-card-form.service';

describe('ClientCard Form Service', () => {
  let service: ClientCardFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ClientCardFormService);
  });

  describe('Service methods', () => {
    describe('createClientCardFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createClientCardFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            maskedPan: expect.any(Object),
            expiryDate: expect.any(Object),
            cardholderName: expect.any(Object),
            isDefault: expect.any(Object),
            isValid: expect.any(Object),
            client: expect.any(Object),
          }),
        );
      });

      it('passing IClientCard should create a new form with FormGroup', () => {
        const formGroup = service.createClientCardFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            maskedPan: expect.any(Object),
            expiryDate: expect.any(Object),
            cardholderName: expect.any(Object),
            isDefault: expect.any(Object),
            isValid: expect.any(Object),
            client: expect.any(Object),
          }),
        );
      });
    });

    describe('getClientCard', () => {
      it('should return NewClientCard for default ClientCard initial value', () => {
        const formGroup = service.createClientCardFormGroup(sampleWithNewData);

        const clientCard = service.getClientCard(formGroup) as any;

        expect(clientCard).toMatchObject(sampleWithNewData);
      });

      it('should return NewClientCard for empty ClientCard initial value', () => {
        const formGroup = service.createClientCardFormGroup();

        const clientCard = service.getClientCard(formGroup) as any;

        expect(clientCard).toMatchObject({});
      });

      it('should return IClientCard', () => {
        const formGroup = service.createClientCardFormGroup(sampleWithRequiredData);

        const clientCard = service.getClientCard(formGroup) as any;

        expect(clientCard).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IClientCard should not enable id FormControl', () => {
        const formGroup = service.createClientCardFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewClientCard should disable id FormControl', () => {
        const formGroup = service.createClientCardFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
