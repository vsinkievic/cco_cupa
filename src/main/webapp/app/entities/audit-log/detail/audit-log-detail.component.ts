import { Component, inject, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { DataUtils } from 'app/core/util/data-util.service';
import { JsonHighlightDirective } from 'app/shared/directive/json-highlight.directive';
import { IAuditLog } from '../audit-log.model';

@Component({
  selector: 'jhi-audit-log-detail',
  templateUrl: './audit-log-detail.component.html',
  styleUrls: ['./audit-log-detail.component.scss'],
  imports: [SharedModule, RouterModule, FormatMediumDatetimePipe, JsonHighlightDirective],
})
export class AuditLogDetailComponent {
  auditLog = input<IAuditLog | null>(null);

  protected dataUtils = inject(DataUtils);

  byteSize(base64String: string): string {
    return this.dataUtils.byteSize(base64String);
  }

  openFile(base64String: string, contentType: string | null | undefined): void {
    this.dataUtils.openFile(base64String, contentType);
  }

  previousState(): void {
    window.history.back();
  }
}
