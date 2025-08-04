import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IMerchant } from '../merchant.model';

@Component({
  selector: 'jhi-merchant-detail',
  templateUrl: './merchant-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class MerchantDetailComponent {
  merchant = input<IMerchant | null>(null);

  previousState(): void {
    window.history.back();
  }
}
