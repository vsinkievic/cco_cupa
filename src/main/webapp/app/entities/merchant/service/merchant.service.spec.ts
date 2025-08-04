import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IMerchant } from '../merchant.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../merchant.test-samples';

import { MerchantService } from './merchant.service';

const requireRestSample: IMerchant = {
  ...sampleWithRequiredData,
};

describe('Merchant Service', () => {
  let service: MerchantService;
  let httpMock: HttpTestingController;
  let expectedResult: IMerchant | IMerchant[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(MerchantService);
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

    it('should create a Merchant', () => {
      const merchant = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(merchant).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Merchant', () => {
      const merchant = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(merchant).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Merchant', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Merchant', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Merchant', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addMerchantToCollectionIfMissing', () => {
      it('should add a Merchant to an empty array', () => {
        const merchant: IMerchant = sampleWithRequiredData;
        expectedResult = service.addMerchantToCollectionIfMissing([], merchant);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(merchant);
      });

      it('should not add a Merchant to an array that contains it', () => {
        const merchant: IMerchant = sampleWithRequiredData;
        const merchantCollection: IMerchant[] = [
          {
            ...merchant,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addMerchantToCollectionIfMissing(merchantCollection, merchant);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Merchant to an array that doesn't contain it", () => {
        const merchant: IMerchant = sampleWithRequiredData;
        const merchantCollection: IMerchant[] = [sampleWithPartialData];
        expectedResult = service.addMerchantToCollectionIfMissing(merchantCollection, merchant);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(merchant);
      });

      it('should add only unique Merchant to an array', () => {
        const merchantArray: IMerchant[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const merchantCollection: IMerchant[] = [sampleWithRequiredData];
        expectedResult = service.addMerchantToCollectionIfMissing(merchantCollection, ...merchantArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const merchant: IMerchant = sampleWithRequiredData;
        const merchant2: IMerchant = sampleWithPartialData;
        expectedResult = service.addMerchantToCollectionIfMissing([], merchant, merchant2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(merchant);
        expect(expectedResult).toContain(merchant2);
      });

      it('should accept null and undefined values', () => {
        const merchant: IMerchant = sampleWithRequiredData;
        expectedResult = service.addMerchantToCollectionIfMissing([], null, merchant, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(merchant);
      });

      it('should return initial array if no Merchant is added', () => {
        const merchantCollection: IMerchant[] = [sampleWithRequiredData];
        expectedResult = service.addMerchantToCollectionIfMissing(merchantCollection, undefined, null);
        expect(expectedResult).toEqual(merchantCollection);
      });
    });

    describe('compareMerchant', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareMerchant(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 23082 };
        const entity2 = null;

        const compareResult1 = service.compareMerchant(entity1, entity2);
        const compareResult2 = service.compareMerchant(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 23082 };
        const entity2 = { id: 16734 };

        const compareResult1 = service.compareMerchant(entity1, entity2);
        const compareResult2 = service.compareMerchant(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 23082 };
        const entity2 = { id: 23082 };

        const compareResult1 = service.compareMerchant(entity1, entity2);
        const compareResult2 = service.compareMerchant(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
