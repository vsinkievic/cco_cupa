import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IClient } from 'app/entities/client/client.model';
import { ClientService } from 'app/entities/client/service/client.service';
import { IClientCard } from '../client-card.model';
import { ClientCardService } from '../service/client-card.service';
import { ClientCardFormGroup, ClientCardFormService } from './client-card-form.service';

@Component({
  selector: 'jhi-client-card-update',
  templateUrl: './client-card-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class ClientCardUpdateComponent implements OnInit {
  isSaving = false;
  clientCard: IClientCard | null = null;

  clientsSharedCollection: IClient[] = [];

  protected clientCardService = inject(ClientCardService);
  protected clientCardFormService = inject(ClientCardFormService);
  protected clientService = inject(ClientService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: ClientCardFormGroup = this.clientCardFormService.createClientCardFormGroup();

  compareClient = (o1: IClient | null, o2: IClient | null): boolean => this.clientService.compareClient(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ clientCard }) => {
      this.clientCard = clientCard;
      if (clientCard) {
        this.updateForm(clientCard);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const clientCard = this.clientCardFormService.getClientCard(this.editForm);
    if (clientCard.id !== null) {
      this.subscribeToSaveResponse(this.clientCardService.update(clientCard));
    } else {
      this.subscribeToSaveResponse(this.clientCardService.create(clientCard));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IClientCard>>): void {
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

  protected updateForm(clientCard: IClientCard): void {
    this.clientCard = clientCard;
    this.clientCardFormService.resetForm(this.editForm, clientCard);

    this.clientsSharedCollection = this.clientService.addClientToCollectionIfMissing<IClient>(
      this.clientsSharedCollection,
      clientCard.client,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.clientService
      .query()
      .pipe(map((res: HttpResponse<IClient[]>) => res.body ?? []))
      .pipe(map((clients: IClient[]) => this.clientService.addClientToCollectionIfMissing<IClient>(clients, this.clientCard?.client)))
      .subscribe((clients: IClient[]) => (this.clientsSharedCollection = clients));
  }
}
