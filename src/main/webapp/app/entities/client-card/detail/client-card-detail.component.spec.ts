import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { ClientCardDetailComponent } from './client-card-detail.component';

describe('ClientCard Management Detail Component', () => {
  let comp: ClientCardDetailComponent;
  let fixture: ComponentFixture<ClientCardDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClientCardDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./client-card-detail.component').then(m => m.ClientCardDetailComponent),
              resolve: { clientCard: () => of({ id: 15916 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(ClientCardDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ClientCardDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load clientCard on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', ClientCardDetailComponent);

      // THEN
      expect(instance.clientCard()).toEqual(expect.objectContaining({ id: 15916 }));
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
