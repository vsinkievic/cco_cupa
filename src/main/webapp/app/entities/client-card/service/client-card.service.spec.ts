import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IClientCard } from '../client-card.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../client-card.test-samples';

import { ClientCardService } from './client-card.service';

const requireRestSample: IClientCard = {
  ...sampleWithRequiredData,
};

describe('ClientCard Service', () => {
  let service: ClientCardService;
  let httpMock: HttpTestingController;
  let expectedResult: IClientCard | IClientCard[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(ClientCardService);
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

    it('should create a ClientCard', () => {
      const clientCard = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(clientCard).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a ClientCard', () => {
      const clientCard = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(clientCard).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a ClientCard', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of ClientCard', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a ClientCard', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addClientCardToCollectionIfMissing', () => {
      it('should add a ClientCard to an empty array', () => {
        const clientCard: IClientCard = sampleWithRequiredData;
        expectedResult = service.addClientCardToCollectionIfMissing([], clientCard);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(clientCard);
      });

      it('should not add a ClientCard to an array that contains it', () => {
        const clientCard: IClientCard = sampleWithRequiredData;
        const clientCardCollection: IClientCard[] = [
          {
            ...clientCard,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addClientCardToCollectionIfMissing(clientCardCollection, clientCard);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a ClientCard to an array that doesn't contain it", () => {
        const clientCard: IClientCard = sampleWithRequiredData;
        const clientCardCollection: IClientCard[] = [sampleWithPartialData];
        expectedResult = service.addClientCardToCollectionIfMissing(clientCardCollection, clientCard);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(clientCard);
      });

      it('should add only unique ClientCard to an array', () => {
        const clientCardArray: IClientCard[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const clientCardCollection: IClientCard[] = [sampleWithRequiredData];
        expectedResult = service.addClientCardToCollectionIfMissing(clientCardCollection, ...clientCardArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const clientCard: IClientCard = sampleWithRequiredData;
        const clientCard2: IClientCard = sampleWithPartialData;
        expectedResult = service.addClientCardToCollectionIfMissing([], clientCard, clientCard2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(clientCard);
        expect(expectedResult).toContain(clientCard2);
      });

      it('should accept null and undefined values', () => {
        const clientCard: IClientCard = sampleWithRequiredData;
        expectedResult = service.addClientCardToCollectionIfMissing([], null, clientCard, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(clientCard);
      });

      it('should return initial array if no ClientCard is added', () => {
        const clientCardCollection: IClientCard[] = [sampleWithRequiredData];
        expectedResult = service.addClientCardToCollectionIfMissing(clientCardCollection, undefined, null);
        expect(expectedResult).toEqual(clientCardCollection);
      });
    });

    describe('compareClientCard', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareClientCard(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 15916 };
        const entity2 = null;

        const compareResult1 = service.compareClientCard(entity1, entity2);
        const compareResult2 = service.compareClientCard(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 15916 };
        const entity2 = { id: 26157 };

        const compareResult1 = service.compareClientCard(entity1, entity2);
        const compareResult2 = service.compareClientCard(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 15916 };
        const entity2 = { id: 15916 };

        const compareResult1 = service.compareClientCard(entity1, entity2);
        const compareResult2 = service.compareClientCard(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
