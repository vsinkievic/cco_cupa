import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IMerchant } from '../merchant.model';
import { MerchantService } from '../service/merchant.service';

@Component({
  templateUrl: './merchant-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class MerchantDeleteDialogComponent {
  merchant?: IMerchant;

  protected merchantService = inject(MerchantService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.merchantService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
