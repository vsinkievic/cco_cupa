import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AlertError } from 'app/shared/alert/alert-error.model';
import { EventManager, EventWithContent } from 'app/core/util/event-manager.service';
import { DataUtils, FileLoadError } from 'app/core/util/data-util.service';
import { IMerchant } from 'app/entities/merchant/merchant.model';
import { MerchantService } from 'app/entities/merchant/service/merchant.service';
import { AuditLogService } from '../service/audit-log.service';
import { IAuditLog } from '../audit-log.model';
import { AuditLogFormGroup, AuditLogFormService } from './audit-log-form.service';

@Component({
  selector: 'jhi-audit-log-update',
  templateUrl: './audit-log-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class AuditLogUpdateComponent implements OnInit {
  isSaving = false;
  auditLog: IAuditLog | null = null;

  merchantsSharedCollection: IMerchant[] = [];

  protected dataUtils = inject(DataUtils);
  protected eventManager = inject(EventManager);
  protected auditLogService = inject(AuditLogService);
  protected auditLogFormService = inject(AuditLogFormService);
  protected merchantService = inject(MerchantService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: AuditLogFormGroup = this.auditLogFormService.createAuditLogFormGroup();

  compareMerchant = (o1: IMerchant | null, o2: IMerchant | null): boolean => this.merchantService.compareMerchant(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ auditLog }) => {
      this.auditLog = auditLog;
      if (auditLog) {
        this.updateForm(auditLog);
      }

      this.loadRelationshipsOptions();
    });
  }

  byteSize(base64String: string): string {
    return this.dataUtils.byteSize(base64String);
  }

  openFile(base64String: string, contentType: string | null | undefined): void {
    this.dataUtils.openFile(base64String, contentType);
  }

  setFileData(event: Event, field: string, isImage: boolean): void {
    this.dataUtils.loadFileToForm(event, this.editForm, field, isImage).subscribe({
      error: (err: FileLoadError) =>
        this.eventManager.broadcast(new EventWithContent<AlertError>('cupaApp.error', { message: err.message })),
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const auditLog = this.auditLogFormService.getAuditLog(this.editForm);
    if (auditLog.id !== null) {
      this.subscribeToSaveResponse(this.auditLogService.update(auditLog));
    } else {
      this.subscribeToSaveResponse(this.auditLogService.create(auditLog));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IAuditLog>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(auditLog: IAuditLog): void {
    this.auditLog = auditLog;
    this.auditLogFormService.resetForm(this.editForm, auditLog);

    this.merchantsSharedCollection = this.merchantService.addMerchantToCollectionIfMissing<IMerchant>(
      this.merchantsSharedCollection,
      auditLog.merchant,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.merchantService
      .query()
      .pipe(map((res: HttpResponse<IMerchant[]>) => res.body ?? []))
      .pipe(
        map((merchants: IMerchant[]) =>
          this.merchantService.addMerchantToCollectionIfMissing<IMerchant>(merchants, this.auditLog?.merchant),
        ),
      )
      .subscribe((merchants: IMerchant[]) => (this.merchantsSharedCollection = merchants));
  }
}
