import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IClientCard } from '../client-card.model';
import { ClientCardService } from '../service/client-card.service';

@Component({
  templateUrl: './client-card-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class ClientCardDeleteDialogComponent {
  clientCard?: IClientCard;

  protected clientCardService = inject(ClientCardService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: string): void {
    this.clientCardService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
