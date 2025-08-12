import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IMerchant } from 'app/entities/merchant/merchant.model';
import { MerchantService } from 'app/entities/merchant/service/merchant.service';
import { IClient, NewClient } from '../client.model';
import { ClientService } from '../service/client.service';
import { ClientFormGroup, ClientFormService } from './client-form.service';

@Component({
  selector: 'jhi-client-update',
  templateUrl: './client-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class ClientUpdateComponent implements OnInit {
  isSaving = false;
  client: IClient | null = null;

  merchantsSharedCollection: IMerchant[] = [];

  protected clientService = inject(ClientService);
  protected clientFormService = inject(ClientFormService);
  protected merchantService = inject(MerchantService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: ClientFormGroup = this.clientFormService.createClientFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ client }) => {
      this.client = client;
      if (client) {
        this.updateForm(client);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const client = this.clientFormService.getClient(this.editForm);
    console.warn('client before saving:', client);
    if (client.version !== null) {
      this.subscribeToSaveResponse(this.clientService.update(client));
    } else {
      const newClient = { ...client, version: null } as NewClient;
      this.subscribeToSaveResponse(this.clientService.create(newClient));
    }
  }

  onMerchantChange(event: Event): void {
    const target = event.target as HTMLSelectElement;
    const merchantId = target.value;

    if (merchantId) {
      const merchant = this.merchantsSharedCollection.find(m => m.id === merchantId);
      if (merchant) {
        this.editForm.patchValue({ merchantId: merchant.id });
      }
    } else {
      this.editForm.patchValue({ merchantId: null });
    }
  }

  trackMerchant(index: number, item: IMerchant): string {
    return item.id;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IClient>>): void {
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

  protected updateForm(client: IClient): void {
    this.client = client;
    this.clientFormService.resetForm(this.editForm, client);
  }

  protected loadRelationshipsOptions(): void {
    this.merchantService
      .query()
      .pipe(map((res: HttpResponse<IMerchant[]>) => res.body ?? []))
      .subscribe((merchants: IMerchant[]) => (this.merchantsSharedCollection = merchants));
  }
}
