import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IAuditLog } from '../audit-log.model';
import { AuditLogService } from '../service/audit-log.service';

@Component({
  templateUrl: './audit-log-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class AuditLogDeleteDialogComponent {
  auditLog?: IAuditLog;

  protected auditLogService = inject(AuditLogService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.auditLogService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
