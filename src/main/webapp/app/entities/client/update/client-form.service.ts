import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IClient, NewClient } from '../client.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IClient for edit and NewClientFormGroupInput for create.
 */
type ClientFormGroupInput = IClient | PartialWithRequiredKeyOf<NewClient>;

type ClientFormDefaults = Pick<NewClient, 'id' | 'valid' | 'isBlacklisted' | 'isCorrelatedBlacklisted' | 'version'>;

type ClientFormGroupContent = {
  id: FormControl<IClient['id'] | NewClient['id']>;
  version: FormControl<IClient['version']>;
  merchantClientId: FormControl<IClient['merchantClientId']>;
  merchantId: FormControl<IClient['merchantId']>;
  name: FormControl<IClient['name']>;
  emailAddress: FormControl<IClient['emailAddress']>;
  mobileNumber: FormControl<IClient['mobileNumber']>;
  clientPhone: FormControl<IClient['clientPhone']>;
  valid: FormControl<IClient['valid']>;
  streetNumber: FormControl<IClient['streetNumber']>;
  streetName: FormControl<IClient['streetName']>;
  streetSuffix: FormControl<IClient['streetSuffix']>;
  city: FormControl<IClient['city']>;
  state: FormControl<IClient['state']>;
  postCode: FormControl<IClient['postCode']>;
  country: FormControl<IClient['country']>;
  isBlacklisted: FormControl<IClient['isBlacklisted']>;
  isCorrelatedBlacklisted: FormControl<IClient['isCorrelatedBlacklisted']>;
};

export type ClientFormGroup = FormGroup<ClientFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class ClientFormService {
  createClientFormGroup(client: ClientFormGroupInput = { id: null, version: null }): ClientFormGroup {
    const clientRawValue = {
      ...this.getFormDefaults(),
      ...client,
    };

    // Determine if this is an existing client with createdInGateway
    const isExistingClient = Boolean(clientRawValue.id && clientRawValue.createdInGateway);

    return new FormGroup<ClientFormGroupContent>({
      id: new FormControl(
        { value: clientRawValue.id, disabled: Boolean(clientRawValue.id) },
        {
          nonNullable: false,
          validators: [],
        },
      ),
      version: new FormControl(clientRawValue.version),
      merchantClientId: new FormControl(
        { value: clientRawValue.merchantClientId, disabled: Boolean(clientRawValue.createdInGateway) },
        {
          validators: [Validators.required],
        },
      ),
      merchantId: new FormControl(
        { value: clientRawValue.merchantId, disabled: isExistingClient },
        {
          validators: [Validators.required],
        },
      ),
      name: new FormControl(clientRawValue.name),
      emailAddress: new FormControl(clientRawValue.emailAddress, {
        validators: [Validators.pattern('^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$')],
      }),
      mobileNumber: new FormControl(clientRawValue.mobileNumber),
      clientPhone: new FormControl(clientRawValue.clientPhone),
      valid: new FormControl({ value: clientRawValue.valid, disabled: true }),
      streetNumber: new FormControl(clientRawValue.streetNumber),
      streetName: new FormControl(clientRawValue.streetName),
      streetSuffix: new FormControl(clientRawValue.streetSuffix),
      city: new FormControl(clientRawValue.city),
      state: new FormControl(clientRawValue.state),
      postCode: new FormControl(clientRawValue.postCode),
      country: new FormControl(clientRawValue.country),
      isBlacklisted: new FormControl({ value: clientRawValue.isBlacklisted, disabled: true }),
      isCorrelatedBlacklisted: new FormControl({ value: clientRawValue.isCorrelatedBlacklisted, disabled: true }),
    });
  }

  getClient(form: ClientFormGroup): IClient | NewClient {
    return form.getRawValue() as IClient | NewClient;
  }

  resetForm(form: ClientFormGroup, client: ClientFormGroupInput): void {
    const clientRawValue = { ...this.getFormDefaults(), ...client };

    // Determine if this is an existing client with createdInGateway
    const isExistingClient = Boolean(clientRawValue.id && clientRawValue.createdInGateway);

    form.reset(
      {
        ...clientRawValue,
        id: { value: clientRawValue.id, disabled: Boolean(clientRawValue.id) },
        version: clientRawValue.version,
        merchantClientId: { value: clientRawValue.merchantClientId, disabled: Boolean(clientRawValue.createdInGateway) },
        merchantId: { value: clientRawValue.merchantId, disabled: isExistingClient },
        valid: { value: clientRawValue.valid, disabled: true },
        isBlacklisted: { value: clientRawValue.isBlacklisted, disabled: true },
        isCorrelatedBlacklisted: { value: clientRawValue.isCorrelatedBlacklisted, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): ClientFormDefaults {
    return {
      id: null,
      valid: false,
      isBlacklisted: false,
      isCorrelatedBlacklisted: false,
      version: null,
    };
  }
}
