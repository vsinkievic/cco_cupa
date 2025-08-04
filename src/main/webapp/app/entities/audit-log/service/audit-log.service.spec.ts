import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IAuditLog } from '../audit-log.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../audit-log.test-samples';

import { AuditLogService, RestAuditLog } from './audit-log.service';

const requireRestSample: RestAuditLog = {
  ...sampleWithRequiredData,
  requestTimestamp: sampleWithRequiredData.requestTimestamp?.toJSON(),
};

describe('AuditLog Service', () => {
  let service: AuditLogService;
  let httpMock: HttpTestingController;
  let expectedResult: IAuditLog | IAuditLog[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(AuditLogService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a AuditLog', () => {
      const auditLog = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(auditLog).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a AuditLog', () => {
      const auditLog = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(auditLog).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a AuditLog', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of AuditLog', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a AuditLog', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addAuditLogToCollectionIfMissing', () => {
      it('should add a AuditLog to an empty array', () => {
        const auditLog: IAuditLog = sampleWithRequiredData;
        expectedResult = service.addAuditLogToCollectionIfMissing([], auditLog);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(auditLog);
      });

      it('should not add a AuditLog to an array that contains it', () => {
        const auditLog: IAuditLog = sampleWithRequiredData;
        const auditLogCollection: IAuditLog[] = [
          {
            ...auditLog,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addAuditLogToCollectionIfMissing(auditLogCollection, auditLog);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a AuditLog to an array that doesn't contain it", () => {
        const auditLog: IAuditLog = sampleWithRequiredData;
        const auditLogCollection: IAuditLog[] = [sampleWithPartialData];
        expectedResult = service.addAuditLogToCollectionIfMissing(auditLogCollection, auditLog);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(auditLog);
      });

      it('should add only unique AuditLog to an array', () => {
        const auditLogArray: IAuditLog[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const auditLogCollection: IAuditLog[] = [sampleWithRequiredData];
        expectedResult = service.addAuditLogToCollectionIfMissing(auditLogCollection, ...auditLogArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const auditLog: IAuditLog = sampleWithRequiredData;
        const auditLog2: IAuditLog = sampleWithPartialData;
        expectedResult = service.addAuditLogToCollectionIfMissing([], auditLog, auditLog2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(auditLog);
        expect(expectedResult).toContain(auditLog2);
      });

      it('should accept null and undefined values', () => {
        const auditLog: IAuditLog = sampleWithRequiredData;
        expectedResult = service.addAuditLogToCollectionIfMissing([], null, auditLog, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(auditLog);
      });

      it('should return initial array if no AuditLog is added', () => {
        const auditLogCollection: IAuditLog[] = [sampleWithRequiredData];
        expectedResult = service.addAuditLogToCollectionIfMissing(auditLogCollection, undefined, null);
        expect(expectedResult).toEqual(auditLogCollection);
      });
    });

    describe('compareAuditLog', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareAuditLog(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 25090 };
        const entity2 = null;

        const compareResult1 = service.compareAuditLog(entity1, entity2);
        const compareResult2 = service.compareAuditLog(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 25090 };
        const entity2 = { id: 25691 };

        const compareResult1 = service.compareAuditLog(entity1, entity2);
        const compareResult2 = service.compareAuditLog(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 25090 };
        const entity2 = { id: 25090 };

        const compareResult1 = service.compareAuditLog(entity1, entity2);
        const compareResult2 = service.compareAuditLog(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
