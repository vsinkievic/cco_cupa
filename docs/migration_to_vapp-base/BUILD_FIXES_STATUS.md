# Build Fixes Status

## Completed Fixes

### 1. Removed frontend-maven-plugin
- ✅ Removed `frontend-maven-plugin.version` property
- ✅ Removed plugin definition from `pluginManagement`
- ✅ Removed plugin execution from `prod` profile
- ✅ Removed plugin execution from `webapp` profile
- ✅ Removed Eclipse lifecycle-mapping for frontend-maven-plugin
- ✅ Removed `node.version` and `npm.version` properties

### 2. Fixed Package Imports for vapp-base
- ✅ Changed all `import com.bpmid.vapp.ui.MainLayout` to `import com.bpmid.vapp.base.ui.MainLayout`
- ✅ Changed `import com.bpmid.vapp.ui.menu.MenuProvider` to `import com.bpmid.vapp.base.ui.MenuProvider`
- ✅ Rewrote `CupaMenuProvider` to match correct `MenuProvider` interface signature

### 3. Added Missing Authority Constants
- ✅ Added `AuthoritiesConstants.CREDITCO = "ROLE_CREDITCO"`
- ✅ Added `AuthoritiesConstants.MERCHANT = "ROLE_MERCHANT"`

### 4. Fixed Application Entry Point
- ✅ Fixed `ApplicationWebXml.java` to reference `CupaApplication` instead of `CupaApp`

### 5. Fixed PaymentRequest
- ✅ Added `merchantId` field to `PaymentRequest` to satisfy `MerchantOwnedEntity` interface

### 6. Added Missing Lombok Annotations
- ✅ Added `@Slf4j` to `MerchantService` (was already there, just confirmed)
- ✅ Verified `CupaApiBusinessLogicService` has `@Slf4j`
- ✅ Verified `CupaApiAuditInterceptor` has `@Slf4j`

## Remaining Compilation Errors

The following compilation errors still need to be fixed:

### 1. BalanceUpdateEvent
- **Error**: `cannot find symbol: method getTransactionId()`
- **File**: `BalanceUpdateEventListener.java:119`
- **Root Cause**: Missing `@Getter` annotation on `BalanceUpdateEvent`
- **Status**: ✅ **VERIFIED** - The class already has `@Getter` annotation (line 10)
- **Next Step**: Need to investigate why Lombok is not generating getters

### 2. CupaApiContextData
- **Error**: `cannot find symbol: method getMerchantContext()`
- **File**: Multiple files (`PaymentTransactionService.java:238`, `:357`)
- **Root Cause**: `CupaApiContextData` has custom `getMerchantId()` method that might conflict with Lombok `@Builder` + `@Data`
- **Status**: Needs investigation

### 3. GatewayResponse
- **Errors**: `cannot find symbol: method getResponse()`, `getReply()`
- **Files**: `PaymentTransactionService.java` (multiple locations)
- **Root Cause**: `GatewayResponse` has `@Data` which should generate getters, but might have issue with generic type
- **Status**: Needs investigation

### 4. RestTemplateBodyInterceptor.Trace
- **Errors**: `cannot find symbol: method getRequestBody()`, `getResponseBody()`
- **File**: `PaymentTransactionService.java` (multiple locations)
- **Root Cause**: `Trace` has `@Getter` but setters are package-private, Lombok might not be generating getters
- **Status**: ✅ **VERIFIED** - The class has `@Getter` annotation (line 47)
- **Next Step**: Need to investigate why Lombok is not generating getters

### 5. remote.PaymentRequest (UnionPay Gateway API model)
- **Errors**: Multiple `cannot find symbol: method setXxx()` errors
- **File**: `PaymentTransactionService.java` (lines 326-351)
- **Root Cause**: `remote.PaymentRequest` has `@Data` which should generate setters
- **Status**: ✅ **VERIFIED** - The class has `@Data` annotation (line 11)
- **Next Step**: Need to investigate why Lombok is not generating setters

### 6. remote.ClientDetails
- **Errors**: Multiple `cannot find symbol: method setXxx()` errors  
- **File**: `PaymentTransactionService.java` (lines 347-351)
- **Root Cause**: `ClientDetails` has `@Data` which should generate setters
- **Status**: ✅ **VERIFIED** - The class has `@Data` annotation (line 10)
- **Next Step**: Need to investigate why Lombok is not generating setters

### 7. GatewayConfig
- **Error**: `cannot find symbol: method builder()`
- **File**: `PaymentTransactionService.java:361`
- **Root Cause**: `GatewayConfig` has `@Builder` but might need additional annotation
- **Status**: ✅ **VERIFIED** - The class has `@Builder` annotation (line 8)
- **Next Step**: Need to investigate why Lombok is not generating builder

## Root Cause Analysis

All the remaining errors are related to Lombok not generating expected methods (getters, setters, builders). This suggests one of the following issues:

1. **Lombok Annotation Processing Not Enabled**: Maven might not be running Lombok annotation processor correctly
2. **Lombok Dependency Issue**: Lombok might not be on the classpath or the version might be incompatible
3. **IDE vs Maven**: The Lombok issue might be specific to Maven compilation (IDE might work fine)

## Recommended Next Steps

1. **Verify Lombok is on the classpath and annotation processing is enabled**:
   ```bash
   ./mvnw dependency:tree | grep lombok
   ```

2. **Clean and rebuild with Lombok debug output**:
   ```bash
   ./mvnw clean compile -X | grep -i lombok
   ```

3. **Check if Lombok is configured correctly in pom.xml**:
   - Lombok should be in dependencies
   - Lombok annotation processor should be configured in maven-compiler-plugin

4. **Try running with explicit Lombok configuration**:
   - Ensure `maven-compiler-plugin` has annotation processor path configured
   - Ensure Lombok version is compatible with Java 17

## Files Modified in This Session

1. `/home/vsinkievic/git/cco_cupa/pom.xml` - Removed frontend-maven-plugin
2. `/home/vsinkievic/git/cco_cupa/src/main/java/lt/creditco/cupa/ui/CupaMenuProvider.java` - Fixed package imports
3. All UI view files in `src/main/java/lt/creditco/cupa/ui/**/*View.java` - Fixed MainLayout imports
4. `/home/vsinkievic/git/cco_cupa/src/main/java/lt/creditco/cupa/security/AuthoritiesConstants.java` - Added CREDITCO and MERCHANT constants
5. `/home/vsinkievic/git/cco_cupa/src/main/java/lt/creditco/cupa/ApplicationWebXml.java` - Fixed CupaApp -> CupaApplication
6. `/home/vsinkievic/git/cco_cupa/src/main/java/lt/creditco/cupa/api/PaymentRequest.java` - Added merchantId field



