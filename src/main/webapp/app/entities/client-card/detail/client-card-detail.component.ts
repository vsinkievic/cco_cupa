import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IClientCard } from '../client-card.model';

@Component({
  selector: 'jhi-client-card-detail',
  templateUrl: './client-card-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class ClientCardDetailComponent {
  clientCard = input<IClientCard | null>(null);

  previousState(): void {
    window.history.back();
  }
}
