import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IAuditLog } from '../audit-log.model';
import { sampleWithFullData, sampleWithPartialData, sampleWithRequiredData } from '../audit-log.test-samples';

import { AuditLogService, RestAuditLog } from './audit-log.service';

const requireRestSample: RestAuditLog = {
  ...sampleWithRequiredData,
  requestTimestamp: sampleWithRequiredData.requestTimestamp?.toJSON(),
};

describe('AuditLog Service', () => {
  let service: AuditLogService;
  let httpMock: HttpTestingController;
  let expectedResult: IAuditLog | IAuditLog[] | boolean | null;
  let expectedResultArray: IAuditLog[];

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

    it('should return a list of AuditLog', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    describe('addAuditLogToCollectionIfMissing', () => {
      it('should add a AuditLog to an empty array', () => {
        const auditLog: IAuditLog = sampleWithRequiredData;
        expectedResultArray = service.addAuditLogToCollectionIfMissing([], auditLog);
        expect(expectedResultArray).toHaveLength(1);
        expect(expectedResultArray).toContain(auditLog);
      });

      it('should not add a AuditLog to an array that contains it', () => {
        const auditLog: IAuditLog = sampleWithRequiredData;
        const auditLogCollection: IAuditLog[] = [
          {
            ...auditLog,
          },
          sampleWithPartialData,
        ];
        expectedResultArray = service.addAuditLogToCollectionIfMissing(auditLogCollection, auditLog);
        expect(expectedResultArray).toHaveLength(2);
      });

      it("should add a AuditLog to an array that doesn't contain it", () => {
        const auditLog: IAuditLog = sampleWithRequiredData;
        const auditLogCollection: IAuditLog[] = [sampleWithPartialData];
        expectedResultArray = service.addAuditLogToCollectionIfMissing(auditLogCollection, auditLog);
        expect(expectedResultArray).toHaveLength(2);
        expect(expectedResultArray).toContain(auditLog);
      });

      it('should add only unique AuditLog to an array', () => {
        const auditLogArray: IAuditLog[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const auditLogCollection: IAuditLog[] = [sampleWithRequiredData];
        expectedResultArray = service.addAuditLogToCollectionIfMissing(auditLogCollection, ...auditLogArray);
        expect(expectedResultArray).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const auditLog: IAuditLog = sampleWithRequiredData;
        const auditLog2: IAuditLog = sampleWithPartialData;
        expectedResultArray = service.addAuditLogToCollectionIfMissing([], auditLog, auditLog2);
        expect(expectedResultArray).toHaveLength(2);
        expect(expectedResultArray).toContain(auditLog);
        expect(expectedResultArray).toContain(auditLog2);
      });

      it('should accept null and undefined values', () => {
        const auditLog: IAuditLog = sampleWithRequiredData;
        expectedResultArray = service.addAuditLogToCollectionIfMissing([], null, auditLog, undefined);
        expect(expectedResultArray).toHaveLength(1);
        expect(expectedResultArray).toContain(auditLog);
      });

      it('should return initial array if no AuditLog is added', () => {
        const auditLogCollection: IAuditLog[] = [sampleWithRequiredData];
        expectedResultArray = service.addAuditLogToCollectionIfMissing(auditLogCollection, undefined, null);
        expect(expectedResultArray).toEqual(auditLogCollection);
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
