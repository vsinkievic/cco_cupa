import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { MerchantDetailComponent } from './merchant-detail.component';

describe('Merchant Management Detail Component', () => {
  let comp: MerchantDetailComponent;
  let fixture: ComponentFixture<MerchantDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MerchantDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./merchant-detail.component').then(m => m.MerchantDetailComponent),
              resolve: { merchant: () => of({ id: 23082 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(MerchantDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MerchantDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load merchant on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', MerchantDetailComponent);

      // THEN
      expect(instance.merchant()).toEqual(expect.objectContaining({ id: 23082 }));
    });
  });

  describe('PreviousState', () => {
    it('should navigate to previous state', () => {
      jest.spyOn(window.history, 'back');
      comp.previousState();
      expect(window.history.back).toHaveBeenCalled();
    });
  });
});
