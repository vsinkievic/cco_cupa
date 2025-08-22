# Webhook Implementation

## Overview

A webhook controller has been implemented to process payment gateway notifications. The endpoint is publicly accessible and designed to handle GET requests only, as specified in the gateway documentation. The implementation includes comprehensive signature verification to ensure the authenticity of webhook notifications and asynchronous balance updates when needed.

## Endpoint Details

- **URL**: `/public/webhook`
- **Methods**: GET only (POST not supported according to documentation)
- **Authentication**: None required (publicly accessible)
- **Content Type**: Query parameters
- **Parameter Case**: Case insensitive for specific parameters (merchantID/merchantId, orderID/orderId, clientID/clientId)

## Security Configuration

The webhook endpoint is configured as publicly accessible in `SecurityConfiguration.java`:

```java
.requestMatchers(mvc.pattern("/public/webhook")).permitAll()
```

## Required Parameters

The webhook requires three mandatory parameters to process the request:

- `merchantID` - Merchant identifier
- `orderID` - Order identifier
- `signature` - MD5 signature for verification

### Optional Parameters

- `currency` - Payment currency (e.g., AUD, USD)
- `success` - Transaction success status (Y/N)
- `clientID` - Client identifier
- `amount` - Transaction amount
- `detail` - Additional transaction details

## Signature Verification

The webhook implements signature verification based on the gateway documentation:

### Signature Algorithm

```
MD5(success + clientID + orderID.toLowerCase() + MD5(merchantKey) + amount + currency + merchantID)
```

### Verification Process

1. **Merchant Key Retrieval**: Gets the appropriate merchant key based on merchant mode (TEST/LIVE)
2. **Signature Calculation**: Calculates the expected signature using the same algorithm
3. **Verification**: Compares the calculated signature with the provided signature
4. **Result**: Returns `false` if signatures don't match, preventing webhook processing

## Balance Update System

When a webhook is processed but the transaction balance is null, the system automatically triggers an asynchronous balance update:

### Event-Driven Architecture

- **Event**: `BalanceUpdateEvent` fired when balance is null after webhook processing
- **Listener**: `BalanceUpdateEventListener` handles the event asynchronously
- **Processing**: Queries the remote gateway to fetch updated transaction details

### Implementation Details

1. **Event Firing**: `PaymentTransactionService.processWebhook()` fires `BalanceUpdateEvent` when balance is null
2. **Asynchronous Processing**: Event listener runs outside the webhook transaction using `@Async`
3. **Context Creation**: Creates proper merchant context for remote gateway queries
4. **Remote Query**: Calls `queryPaymentFromGateway()` to fetch updated balance
5. **Error Handling**: Comprehensive logging and error handling for failed balance updates

### Benefits

- **Non-blocking**: Webhook response is not delayed by balance updates
- **Reliable**: Uses existing gateway query infrastructure
- **Transactional**: Balance updates run in separate transactions
- **Auditable**: Full logging of balance update attempts and results

## Processing Flow

1. **Webhook Reception**: Controller receives GET request with query parameters
2. **Parameter Validation**: Validates required fields (merchantID, orderID, signature)
3. **Silent Exit**: Returns 200 OK immediately if required fields are missing
4. **Signature Verification**: Verifies webhook signature using merchant key
5. **Transaction Lookup**: Finds corresponding payment transaction
6. **Webhook Processing**: Updates transaction with webhook data
7. **Balance Check**: Checks if balance is null after processing
8. **Event Firing**: Fires `BalanceUpdateEvent` if balance is null
9. **Response**: Returns 200 OK for successful processing, 400 Bad Request for failures

## Testing

Comprehensive tests have been implemented to verify:

- **Successful Processing**: Webhooks with all required fields are processed correctly
- **Case Insensitive Parameters**: Both original case and camelCase parameters work
- **Parameter Preference**: Original case parameters are preferred when both are provided
- **Silent Exit**: Missing required fields result in silent exit without service calls
- **Error Handling**: Invalid data formats are handled gracefully
- **Public Access**: Endpoint is accessible without authentication
- **Value Verification**: All URL parameter values are correctly mapped and passed to the service using ArgumentCaptor
- **Signature Verification**: Invalid signatures are rejected appropriately
- **Balance Update Events**: Events are fired when balance is null after webhook processing
- **Event Listener**: Balance update events are processed asynchronously

### Test Verification Details

The tests use `ArgumentCaptor<PaymentReply>` to capture and verify that all URL parameters are correctly mapped to the corresponding fields in the `PaymentReply` object:

- **String Fields**: `currency`, `success`, `merchantId`, `orderId`, `clientId`, `detail`
- **Numeric Fields**: `amount` (parsed as BigDecimal)
- **Null Handling**: Missing parameters are properly set to null
- **Case Sensitivity**: Both `merchantID`/`merchantId`, `orderID`/`orderId`, `clientID`/`clientId` are supported

## Error Handling

- **Missing Required Fields**: Silent exit with 200 OK response
- **Invalid Signature**: Returns 400 Bad Request
- **Transaction Not Found**: Returns 400 Bad Request
- **Invalid Data Formats**: Graceful handling with appropriate logging
- **Balance Update Failures**: Logged but don't affect webhook response

## Future Enhancements

The implementation is designed to support future enhancements:

- **IP Whitelisting**: Easy to add IP address validation for additional security
- **Rate Limiting**: Can be extended with rate limiting for webhook endpoints
- **Retry Logic**: Balance update events can be enhanced with retry mechanisms
- **Monitoring**: Comprehensive logging enables monitoring and alerting

## Files Modified

- `src/main/java/lt/creditco/cupa/web/rest/WebhookController.java` - Main webhook controller
- `src/main/java/lt/creditco/cupa/service/PaymentTransactionService.java` - Webhook processing and event firing
- `src/main/java/lt/creditco/cupa/remote/SignatureVerifier.java` - Signature verification utility
- `src/main/java/lt/creditco/cupa/event/BalanceUpdateEvent.java` - Balance update event
- `src/main/java/lt/creditco/cupa/event/BalanceUpdateEventListener.java` - Event listener for balance updates
- `src/main/java/lt/creditco/cupa/config/SecurityConfiguration.java` - Security configuration
- `src/main/java/lt/creditco/cupa/web/filter/SpaWebFilter.java` - SPA filter configuration
- `src/test/java/lt/creditco/cupa/web/rest/WebhookControllerTest.java` - Webhook controller tests
- `src/test/java/lt/creditco/cupa/event/BalanceUpdateEventListenerTest.java` - Event listener tests
- `src/test/java/lt/creditco/cupa/security/SecurityConfigurationIT.java` - Security integration tests
