import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IMerchant, NewMerchant } from '../merchant.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IMerchant for edit and NewMerchantFormGroupInput for create.
 */
type MerchantFormGroupInput = IMerchant | PartialWithRequiredKeyOf<NewMerchant>;

type MerchantFormDefaults = Pick<NewMerchant, 'id'>;

type MerchantFormGroupContent = {
  id: FormControl<IMerchant['id'] | NewMerchant['id']>;
  name: FormControl<IMerchant['name']>;
  mode: FormControl<IMerchant['mode']>;
  status: FormControl<IMerchant['status']>;
  balance: FormControl<IMerchant['balance']>;
  cupaTestApiKey: FormControl<IMerchant['cupaTestApiKey']>;
  cupaProdApiKey: FormControl<IMerchant['cupaProdApiKey']>;
  remoteTestUrl: FormControl<IMerchant['remoteTestUrl']>;
  remoteTestMerchantId: FormControl<IMerchant['remoteTestMerchantId']>;
  remoteTestMerchantKey: FormControl<IMerchant['remoteTestMerchantKey']>;
  remoteTestApiKey: FormControl<IMerchant['remoteTestApiKey']>;
  remoteProdUrl: FormControl<IMerchant['remoteProdUrl']>;
  remoteProdMerchantId: FormControl<IMerchant['remoteProdMerchantId']>;
  remoteProdMerchantKey: FormControl<IMerchant['remoteProdMerchantKey']>;
  remoteProdApiKey: FormControl<IMerchant['remoteProdApiKey']>;
};

export type MerchantFormGroup = FormGroup<MerchantFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class MerchantFormService {
  createMerchantFormGroup(merchant: MerchantFormGroupInput = { id: null }): MerchantFormGroup {
    const merchantRawValue = {
      ...this.getFormDefaults(),
      ...merchant,
    };
    return new FormGroup<MerchantFormGroupContent>({
      id: new FormControl(
        { value: merchantRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(merchantRawValue.name, {
        validators: [Validators.required],
      }),
      mode: new FormControl(merchantRawValue.mode, {
        validators: [Validators.required],
      }),
      status: new FormControl(merchantRawValue.status, {
        validators: [Validators.required],
      }),
      balance: new FormControl(merchantRawValue.balance),
      cupaTestApiKey: new FormControl(merchantRawValue.cupaTestApiKey),
      cupaProdApiKey: new FormControl(merchantRawValue.cupaProdApiKey),
      remoteTestUrl: new FormControl(merchantRawValue.remoteTestUrl),
      remoteTestMerchantId: new FormControl(merchantRawValue.remoteTestMerchantId),
      remoteTestMerchantKey: new FormControl(merchantRawValue.remoteTestMerchantKey),
      remoteTestApiKey: new FormControl(merchantRawValue.remoteTestApiKey),
      remoteProdUrl: new FormControl(merchantRawValue.remoteProdUrl),
      remoteProdMerchantId: new FormControl(merchantRawValue.remoteProdMerchantId),
      remoteProdMerchantKey: new FormControl(merchantRawValue.remoteProdMerchantKey),
      remoteProdApiKey: new FormControl(merchantRawValue.remoteProdApiKey),
    });
  }

  getMerchant(form: MerchantFormGroup): IMerchant | NewMerchant {
    return form.getRawValue() as IMerchant | NewMerchant;
  }

  resetForm(form: MerchantFormGroup, merchant: MerchantFormGroupInput): void {
    const merchantRawValue = { ...this.getFormDefaults(), ...merchant };
    form.reset(
      {
        ...merchantRawValue,
        id: { value: merchantRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): MerchantFormDefaults {
    return {
      id: null,
    };
  }
}
