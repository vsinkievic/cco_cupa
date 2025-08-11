import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IClientCard, NewClientCard } from '../client-card.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IClientCard for edit and NewClientCardFormGroupInput for create.
 */
type ClientCardFormGroupInput = IClientCard | PartialWithRequiredKeyOf<NewClientCard>;

type ClientCardFormDefaults = Pick<NewClientCard, 'id' | 'isDefault' | 'isValid'>;

type ClientCardFormGroupContent = {
  id: FormControl<IClientCard['id'] | NewClientCard['id']>;
  version: FormControl<IClientCard['version']>;
  maskedPan: FormControl<IClientCard['maskedPan']>;
  expiryDate: FormControl<IClientCard['expiryDate']>;
  cardholderName: FormControl<IClientCard['cardholderName']>;
  isDefault: FormControl<IClientCard['isDefault']>;
  isValid: FormControl<IClientCard['isValid']>;
  client: FormControl<IClientCard['client']>;
};

export type ClientCardFormGroup = FormGroup<ClientCardFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class ClientCardFormService {
  createClientCardFormGroup(clientCard: ClientCardFormGroupInput = { id: null, version: null }): ClientCardFormGroup {
    const clientCardRawValue = {
      ...this.getFormDefaults(),
      ...clientCard,
    };
    return new FormGroup<ClientCardFormGroupContent>({
      id: new FormControl(
        { value: clientCardRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      version: new FormControl(clientCardRawValue.version),
      maskedPan: new FormControl(clientCardRawValue.maskedPan, {
        validators: [Validators.required],
      }),
      expiryDate: new FormControl(clientCardRawValue.expiryDate),
      cardholderName: new FormControl(clientCardRawValue.cardholderName),
      isDefault: new FormControl(clientCardRawValue.isDefault),
      isValid: new FormControl(clientCardRawValue.isValid),
      client: new FormControl(clientCardRawValue.client, {
        validators: [Validators.required],
      }),
    });
  }

  getClientCard(form: ClientCardFormGroup): IClientCard | NewClientCard {
    return form.getRawValue() as IClientCard | NewClientCard;
  }

  resetForm(form: ClientCardFormGroup, clientCard: ClientCardFormGroupInput): void {
    const clientCardRawValue = { ...this.getFormDefaults(), ...clientCard };
    form.reset(
      {
        ...clientCardRawValue,
        id: { value: clientCardRawValue.id, disabled: true },
        version: { value: clientCardRawValue.version },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): ClientCardFormDefaults {
    return {
      id: null,
      isDefault: false,
      isValid: false,
    };
  }
}
