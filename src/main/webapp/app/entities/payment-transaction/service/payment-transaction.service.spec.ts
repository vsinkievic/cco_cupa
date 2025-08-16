import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IPaymentTransaction } from '../payment-transaction.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../payment-transaction.test-samples';

import { PaymentTransactionService, RestPaymentTransaction } from './payment-transaction.service';

const requireRestSample: RestPaymentTransaction = {
  ...sampleWithRequiredData,
  requestTimestamp: sampleWithRequiredData.requestTimestamp?.toJSON(),
  callbackTimestamp: sampleWithRequiredData.callbackTimestamp?.toJSON(),
  createdDate: sampleWithRequiredData.createdDate?.toJSON() ?? null,
  lastModifiedDate: sampleWithRequiredData.lastModifiedDate?.toJSON() ?? null,
};

describe('PaymentTransaction Service', () => {
  let service: PaymentTransactionService;
  let httpMock: HttpTestingController;
  let expectedResult: IPaymentTransaction | IPaymentTransaction[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(PaymentTransactionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find('123').subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a PaymentTransaction', () => {
      const paymentTransaction = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(paymentTransaction).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a PaymentTransaction', () => {
      const paymentTransaction = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(paymentTransaction).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a PaymentTransaction', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of PaymentTransaction', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    describe('addPaymentTransactionToCollectionIfMissing', () => {
      it('should add a PaymentTransaction to an empty array', () => {
        const paymentTransaction: IPaymentTransaction = sampleWithRequiredData;
        expectedResult = service.addPaymentTransactionToCollectionIfMissing([], paymentTransaction);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(paymentTransaction);
      });

      it('should not add a PaymentTransaction to an array that contains it', () => {
        const paymentTransaction: IPaymentTransaction = sampleWithRequiredData;
        const paymentTransactionCollection: IPaymentTransaction[] = [
          {
            ...paymentTransaction,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addPaymentTransactionToCollectionIfMissing(paymentTransactionCollection, paymentTransaction);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a PaymentTransaction to an array that doesn't contain it", () => {
        const paymentTransaction: IPaymentTransaction = sampleWithRequiredData;
        const paymentTransactionCollection: IPaymentTransaction[] = [sampleWithPartialData];
        expectedResult = service.addPaymentTransactionToCollectionIfMissing(paymentTransactionCollection, paymentTransaction);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(paymentTransaction);
      });

      it('should add only unique PaymentTransaction to an array', () => {
        const paymentTransactionArray: IPaymentTransaction[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const paymentTransactionCollection: IPaymentTransaction[] = [sampleWithRequiredData];
        expectedResult = service.addPaymentTransactionToCollectionIfMissing(paymentTransactionCollection, ...paymentTransactionArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const paymentTransaction: IPaymentTransaction = sampleWithRequiredData;
        const paymentTransaction2: IPaymentTransaction = sampleWithPartialData;
        expectedResult = service.addPaymentTransactionToCollectionIfMissing([], paymentTransaction, paymentTransaction2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(paymentTransaction);
        expect(expectedResult).toContain(paymentTransaction2);
      });

      it('should accept null and undefined values', () => {
        const paymentTransaction: IPaymentTransaction = sampleWithRequiredData;
        expectedResult = service.addPaymentTransactionToCollectionIfMissing([], null, paymentTransaction, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(paymentTransaction);
      });

      it('should return initial array if no PaymentTransaction is added', () => {
        const paymentTransactionCollection: IPaymentTransaction[] = [sampleWithRequiredData];
        expectedResult = service.addPaymentTransactionToCollectionIfMissing(paymentTransactionCollection, undefined, null);
        expect(expectedResult).toEqual(paymentTransactionCollection);
      });
    });

    describe('comparePaymentTransaction', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.comparePaymentTransaction(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: '10661' };
        const entity2 = null;

        const compareResult1 = service.comparePaymentTransaction(entity1, entity2);
        const compareResult2 = service.comparePaymentTransaction(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: '10661' };
        const entity2 = { id: '1571' };

        const compareResult1 = service.comparePaymentTransaction(entity1, entity2);
        const compareResult2 = service.comparePaymentTransaction(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: '10661' };
        const entity2 = { id: '10661' };

        const compareResult1 = service.comparePaymentTransaction(entity1, entity2);
        const compareResult2 = service.comparePaymentTransaction(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
