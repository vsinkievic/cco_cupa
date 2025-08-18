# CUPA API Audit Logging Implementation

## Overview

This document describes the implementation of comprehensive audit logging for all `CupaApiResource` calls. The solution ensures that business logic for determining fields like `environment` and `merchantId` runs only once per request and is shared between audit logging and controller methods.

## Architecture

The implementation follows a **Context-Aware Interceptor** pattern with the following components:

### 1. CupaApiContext

**File**: `src/main/java/lt/creditco/cupa/web/context/CupaApiContext.java`

- Thread-local context holder for request-scoped data
- Stores business context extracted once per request
- Provides access to merchant, environment, API key, and other request data

### 2. CupaApiBusinessLogicService

**File**: `src/main/java/lt/creditco/cupa/service/CupaApiBusinessLogicService.java`

- Centralized service for extracting business context
- Determines merchant, environment, and API key based on authentication
- Extracts orderId, clientId, and request data
- **Runs only once per request** - ensures efficiency

### 3. CupaApiAuditInterceptor

**File**: `src/main/java/lt/creditco/cupa/web/interceptor/CupaApiAuditInterceptor.java`

- Spring interceptor that captures all `/api/v1/**` requests
- Creates initial audit log entry in `preHandle()`
- Updates with response data in `postHandle()`
- Updates with exception information in `afterCompletion()`
- Cleans up context after request completion

### 4. WebMvcConfig

**File**: `src/main/java/lt/creditco/cupa/config/WebMvcConfig.java`

- Registers the audit interceptor for `/api/v1/**` endpoints
- Ensures automatic capture of all CUPA API calls

## Key Features

### Single Execution of Business Logic

- Business context extraction happens once in `preHandle()`
- Results are stored in `CupaApiContext` for use by controller methods
- No duplicate database queries or business logic execution

### Comprehensive Data Capture

- **Request Data**: Timestamp, endpoint, method, IP address, request body
- **Business Context**: Merchant ID, environment, API key, order ID, client ID
- **Response Data**: Status code, response description
- **Exception Handling**: Captures and logs exceptions with full details

### Thread Safety

- Uses `ThreadLocal` for request-scoped data
- Automatic cleanup in `afterCompletion()`
- Safe for concurrent requests

### Minimal Controller Changes

- Controllers only need to access `CupaApiContext.getContext()` when needed
- No changes required to existing business logic
- Optional enhancement for logging and debugging

## Usage Example

### Controller Method

```java
@GetMapping("/payments/{orderId}")
public ResponseEntity<Payment> getPayment(@PathVariable String orderId, Principal principal) {
  // Business context is already available from interceptor
  CupaApiContext.CupaApiContextData context = CupaApiContext.getContext();

  log.info(
    "getPayment({}), executed by {}, merchant: {}, environment: {}",
    orderId,
    principal.getName(),
    context.getMerchantId(),
    context.getEnvironment()
  );

  // Your existing business logic...
  return ResponseEntity.ok(payment);
}

```

### Audit Log Entry

Each request creates an audit log entry with:

- `requestTimestamp`: When the request was received
- `apiEndpoint`: Full request URI
- `httpMethod`: GET, POST, etc.
- `requesterIpAddress`: Client IP (handles proxies)
- `requestData`: JSON request body or query parameters
- `orderId`: Extracted from path or request body
- `merchantId`: Determined from authentication
- `environment`: TEST or LIVE based on merchant configuration
- `cupaApiKey`: API key for the merchant/environment
- `httpStatusCode`: Response status code
- `responseDescription`: Human-readable status description
- `responseData`: Exception details if applicable

## Configuration

The interceptor is automatically registered for all `/api/v1/**` endpoints. No additional configuration is required.

## Testing

Comprehensive test coverage includes:

- **CupaApiAuditInterceptorTest**: Tests interceptor functionality
- **CupaApiBusinessLogicServiceTest**: Tests business logic extraction
- Exception handling and edge cases
- Thread safety verification

## Benefits

1. **Centralized Logging**: All API calls are automatically logged
2. **Performance**: Business logic runs only once per request
3. **Comprehensive**: Captures all required fields for audit purposes
4. **Maintainable**: Clean separation of concerns
5. **Extensible**: Easy to add new fields or modify business logic
6. **Thread-Safe**: Handles concurrent requests properly

## Future Enhancements

1. **Response Body Capture**: Implement response wrapper for full response body logging
2. **Async Support**: Add support for async request handling
3. **Performance Optimization**: Consider async logging for high-traffic scenarios
4. **Data Retention**: Implement log retention and archiving policies
5. **Security**: Add data sanitization for sensitive information

## Files Created/Modified

### New Files

- `src/main/java/lt/creditco/cupa/web/context/CupaApiContext.java`
- `src/main/java/lt/creditco/cupa/service/CupaApiBusinessLogicService.java`
- `src/main/java/lt/creditco/cupa/web/interceptor/CupaApiAuditInterceptor.java`
- `src/main/java/lt/creditco/cupa/config/WebMvcConfig.java`
- `src/test/java/lt/creditco/cupa/web/interceptor/CupaApiAuditInterceptorTest.java`
- `src/test/java/lt/creditco/cupa/service/CupaApiBusinessLogicServiceTest.java`

### Modified Files

- `src/main/java/lt/creditco/cupa/web/rest/CupaApiResource.java` - Updated to use context

## Conclusion

The audit logging implementation provides a robust, efficient, and maintainable solution for capturing comprehensive audit data for all CUPA API calls. The design ensures that business logic runs only once while providing rich context for both audit logging and controller methods.
