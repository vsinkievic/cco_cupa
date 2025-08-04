import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../audit-log.test-samples';

import { AuditLogFormService } from './audit-log-form.service';

describe('AuditLog Form Service', () => {
  let service: AuditLogFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuditLogFormService);
  });

  describe('Service methods', () => {
    describe('createAuditLogFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createAuditLogFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            requestTimestamp: expect.any(Object),
            apiEndpoint: expect.any(Object),
            httpMethod: expect.any(Object),
            httpStatusCode: expect.any(Object),
            orderId: expect.any(Object),
            responseDescription: expect.any(Object),
            cupaApiKey: expect.any(Object),
            environment: expect.any(Object),
            requestData: expect.any(Object),
            responseData: expect.any(Object),
            requesterIpAddress: expect.any(Object),
            merchant: expect.any(Object),
          }),
        );
      });

      it('passing IAuditLog should create a new form with FormGroup', () => {
        const formGroup = service.createAuditLogFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            requestTimestamp: expect.any(Object),
            apiEndpoint: expect.any(Object),
            httpMethod: expect.any(Object),
            httpStatusCode: expect.any(Object),
            orderId: expect.any(Object),
            responseDescription: expect.any(Object),
            cupaApiKey: expect.any(Object),
            environment: expect.any(Object),
            requestData: expect.any(Object),
            responseData: expect.any(Object),
            requesterIpAddress: expect.any(Object),
            merchant: expect.any(Object),
          }),
        );
      });
    });

    describe('getAuditLog', () => {
      it('should return NewAuditLog for default AuditLog initial value', () => {
        const formGroup = service.createAuditLogFormGroup(sampleWithNewData);

        const auditLog = service.getAuditLog(formGroup) as any;

        expect(auditLog).toMatchObject(sampleWithNewData);
      });

      it('should return NewAuditLog for empty AuditLog initial value', () => {
        const formGroup = service.createAuditLogFormGroup();

        const auditLog = service.getAuditLog(formGroup) as any;

        expect(auditLog).toMatchObject({});
      });

      it('should return IAuditLog', () => {
        const formGroup = service.createAuditLogFormGroup(sampleWithRequiredData);

        const auditLog = service.getAuditLog(formGroup) as any;

        expect(auditLog).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IAuditLog should not enable id FormControl', () => {
        const formGroup = service.createAuditLogFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewAuditLog should disable id FormControl', () => {
        const formGroup = service.createAuditLogFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
