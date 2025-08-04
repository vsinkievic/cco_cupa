import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IAuditLog, NewAuditLog } from '../audit-log.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IAuditLog for edit and NewAuditLogFormGroupInput for create.
 */
type AuditLogFormGroupInput = IAuditLog | PartialWithRequiredKeyOf<NewAuditLog>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IAuditLog | NewAuditLog> = Omit<T, 'requestTimestamp'> & {
  requestTimestamp?: string | null;
};

type AuditLogFormRawValue = FormValueOf<IAuditLog>;

type NewAuditLogFormRawValue = FormValueOf<NewAuditLog>;

type AuditLogFormDefaults = Pick<NewAuditLog, 'id' | 'requestTimestamp'>;

type AuditLogFormGroupContent = {
  id: FormControl<AuditLogFormRawValue['id'] | NewAuditLog['id']>;
  requestTimestamp: FormControl<AuditLogFormRawValue['requestTimestamp']>;
  apiEndpoint: FormControl<AuditLogFormRawValue['apiEndpoint']>;
  httpMethod: FormControl<AuditLogFormRawValue['httpMethod']>;
  httpStatusCode: FormControl<AuditLogFormRawValue['httpStatusCode']>;
  orderId: FormControl<AuditLogFormRawValue['orderId']>;
  responseDescription: FormControl<AuditLogFormRawValue['responseDescription']>;
  cupaApiKey: FormControl<AuditLogFormRawValue['cupaApiKey']>;
  environment: FormControl<AuditLogFormRawValue['environment']>;
  requestData: FormControl<AuditLogFormRawValue['requestData']>;
  responseData: FormControl<AuditLogFormRawValue['responseData']>;
  requesterIpAddress: FormControl<AuditLogFormRawValue['requesterIpAddress']>;
  merchant: FormControl<AuditLogFormRawValue['merchant']>;
};

export type AuditLogFormGroup = FormGroup<AuditLogFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class AuditLogFormService {
  createAuditLogFormGroup(auditLog: AuditLogFormGroupInput = { id: null }): AuditLogFormGroup {
    const auditLogRawValue = this.convertAuditLogToAuditLogRawValue({
      ...this.getFormDefaults(),
      ...auditLog,
    });
    return new FormGroup<AuditLogFormGroupContent>({
      id: new FormControl(
        { value: auditLogRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      requestTimestamp: new FormControl(auditLogRawValue.requestTimestamp, {
        validators: [Validators.required],
      }),
      apiEndpoint: new FormControl(auditLogRawValue.apiEndpoint, {
        validators: [Validators.required],
      }),
      httpMethod: new FormControl(auditLogRawValue.httpMethod, {
        validators: [Validators.required],
      }),
      httpStatusCode: new FormControl(auditLogRawValue.httpStatusCode),
      orderId: new FormControl(auditLogRawValue.orderId),
      responseDescription: new FormControl(auditLogRawValue.responseDescription),
      cupaApiKey: new FormControl(auditLogRawValue.cupaApiKey),
      environment: new FormControl(auditLogRawValue.environment),
      requestData: new FormControl(auditLogRawValue.requestData),
      responseData: new FormControl(auditLogRawValue.responseData),
      requesterIpAddress: new FormControl(auditLogRawValue.requesterIpAddress),
      merchant: new FormControl(auditLogRawValue.merchant),
    });
  }

  getAuditLog(form: AuditLogFormGroup): IAuditLog | NewAuditLog {
    return this.convertAuditLogRawValueToAuditLog(form.getRawValue() as AuditLogFormRawValue | NewAuditLogFormRawValue);
  }

  resetForm(form: AuditLogFormGroup, auditLog: AuditLogFormGroupInput): void {
    const auditLogRawValue = this.convertAuditLogToAuditLogRawValue({ ...this.getFormDefaults(), ...auditLog });
    form.reset(
      {
        ...auditLogRawValue,
        id: { value: auditLogRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): AuditLogFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      requestTimestamp: currentTime,
    };
  }

  private convertAuditLogRawValueToAuditLog(rawAuditLog: AuditLogFormRawValue | NewAuditLogFormRawValue): IAuditLog | NewAuditLog {
    return {
      ...rawAuditLog,
      requestTimestamp: dayjs(rawAuditLog.requestTimestamp, DATE_TIME_FORMAT),
    };
  }

  private convertAuditLogToAuditLogRawValue(
    auditLog: IAuditLog | (Partial<NewAuditLog> & AuditLogFormDefaults),
  ): AuditLogFormRawValue | PartialWithRequiredKeyOf<NewAuditLogFormRawValue> {
    return {
      ...auditLog,
      requestTimestamp: auditLog.requestTimestamp ? auditLog.requestTimestamp.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
