package lt.creditco.cupa.service.mapper;

import lt.creditco.cupa.api.Payment;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between {@link PaymentTransactionDTO} and {@link Payment}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface PaymentMapper {
    /**
     * Convert PaymentTransactionDTO to Payment.
     *
     * @param paymentTransactionDTO the source DTO
     * @return the Payment object
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "clientId", source = "merchantClientId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(
        target = "currency",
        expression = "java(paymentTransactionDTO.getCurrency() != null ? paymentTransactionDTO.getCurrency().name() : null)"
    )
    @Mapping(
        target = "status",
        expression = "java(paymentTransactionDTO.getStatus() != null ? paymentTransactionDTO.getStatus().name() : null)"
    )
    @Mapping(target = "statusDescription", source = "statusDescription")
    @Mapping(target = "createdAt", source = "requestTimestamp")
    Payment toPayment(PaymentTransactionDTO paymentTransactionDTO);

    /**
     * Convert Payment to PaymentTransactionDTO.
     * Note: This is a partial mapping as Payment has fewer fields than PaymentTransactionDTO.
     *
     * @param payment the source Payment
     * @return the PaymentTransactionDTO object
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "merchantClientId", source = "clientId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "requestTimestamp", source = "createdAt")
    @Mapping(target = "gatewayTransactionId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "statusDescription", ignore = true)
    @Mapping(target = "paymentBrand", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "replyUrl", ignore = true)
    @Mapping(target = "backofficeUrl", ignore = true)
    @Mapping(target = "echo", ignore = true)
    @Mapping(target = "paymentFlow", ignore = true)
    @Mapping(target = "signature", ignore = true)
    @Mapping(target = "signatureVersion", ignore = true)
    @Mapping(target = "requestData", ignore = true)
    @Mapping(target = "initialResponseData", ignore = true)
    @Mapping(target = "callbackTimestamp", ignore = true)
    @Mapping(target = "callbackData", ignore = true)
    @Mapping(target = "lastQueryData", ignore = true)
    @Mapping(target = "clientId", ignore = true)
    @Mapping(target = "merchantId", ignore = true)
    @Mapping(target = "clientName", ignore = true)
    @Mapping(target = "merchantName", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    PaymentTransactionDTO toPaymentTransactionDTO(Payment payment);
}
