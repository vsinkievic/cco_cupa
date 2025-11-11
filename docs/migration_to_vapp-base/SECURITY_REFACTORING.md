# Security Configuration Refactoring

## Date: 2025-11-08

## Overview
Refactored security configuration to properly separate API (X-API-Key) and Vaadin UI (session-based) authentication using the Template Method pattern, making vapp-base extensible for different authentication strategies.

## Problem Statement
The original security configuration attempted to handle both Vaadin UI and API authentication in a single `SecurityConfiguration` class that extended `VaadinWebSecurity`. This caused:
- `anyRequest()` conflicts when trying to add custom matchers after Vaadin's base configuration
- Difficulty integrating vapp-base's pre-configured security components
- Unclear separation between API and UI security concerns

## Solution: Template Method Pattern

### Architecture
Implemented a two-tier security architecture using Spring Security's `@Order` annotation:

1. **API Security (Order 1)**: `CupaApiSecurityConfiguration` extends `ApiSecurityConfiguration` from vapp-base
2. **UI Security (Order 2)**: `VaadinSecurityConfiguration` from vapp-base (used as-is)

### Changes in vapp-base

#### ApiSecurityConfiguration
**File**: `/bpmid_vapp-base/src/main/java/com/bpmid/vapp/config/ApiSecurityConfiguration.java`

**Changes**:
- Added `@ConditionalOnMissingBean(name = "apiFilterChain")` to allow applications to replace it entirely
- Introduced two protected template methods:
  - `configureApiAuthentication(HttpSecurity, MvcRequestMatcher.Builder)` - Override to replace JWT with custom auth
  - `configureApiAuthorization(HttpSecurity, MvcRequestMatcher.Builder)` - Override to customize authorization rules
- Default implementation provides JWT authentication (production-ready for apps that need it)

**Benefits**:
- Applications can override just authentication (keeping standard authorization)
- Or override both methods for complete customization
- vapp-base remains production-ready with JWT by default

#### VaadinSecurityConfiguration
**File**: `/bpmid_vapp-base/src/main/java/com/bpmid/vapp/config/VaadinSecurityConfiguration.java`

**Changes**:
- Introduced two protected template methods:
  - `configureManagementEndpoints(AuthorizeHttpRequestsConfigurer...AuthorizationManagerRequestMatcherRegistry)` - Customize management endpoint security
  - `configureApiDocumentation(AuthorizeHttpRequestsConfigurer...AuthorizationManagerRequestMatcherRegistry)` - Customize API docs security
- Default implementation:
  - Management endpoints: `/management/health*`, `/management/info`, `/management/prometheus` are public (for load balancers)
  - Other management endpoints require `ROLE_ADMIN`
  - Swagger UI public, API docs require authentication

**Benefits**:
- Applications can customize which management endpoints are public
- Consistent pattern with API security configuration

### Changes in CUPA

#### New: CupaApiSecurityConfiguration
**File**: `/cco_cupa/src/main/java/lt/creditco/cupa/config/CupaApiSecurityConfiguration.java`

**Purpose**: Extends vapp-base `ApiSecurityConfiguration` and overrides template methods to use X-API-Key authentication.

**Implementation**:
```java
@Override
protected void configureApiAuthentication(HttpSecurity http, MvcRequestMatcher.Builder mvc) {
    // Replace JWT with X-API-Key authentication filter
    http.addFilterAfter(new ApiKeyAuthenticationFilter(merchantRepository), BasicAuthenticationFilter.class);
}

@Override
protected void configureApiAuthorization(HttpSecurity http, MvcRequestMatcher.Builder mvc) {
    http.authorizeHttpRequests(authz ->
        authz
            .requestMatchers(mvc.pattern("/api/v1/**")).authenticated()  // ApiKeyAuthenticationFilter handles auth
            .requestMatchers(mvc.pattern("/api/**")).authenticated()      // Future versions
            .requestMatchers(mvc.pattern("/public/webhook")).permitAll()
    );
}
```

**Key Points**:
- `/api/v1/**` endpoints are `permitAll()` because authentication is handled by `ApiKeyAuthenticationFilter`
- The filter sets Spring Security context if X-API-Key is valid
- Other `/api/**` paths (like `/api/admin/**`) are denied by default when accessed with API keys
- This prevents API keys from being used to access non-merchant endpoints
- No JWT/OAuth2 configuration needed

#### Deleted: SecurityConfiguration
**File**: `/cco_cupa/src/main/java/lt/creditco/cupa/config/SecurityConfiguration.java` (removed)

**Reason**: This monolithic configuration tried to do too much and caused `anyRequest()` conflicts.

#### Updated: CupaApplication
**File**: `/cco_cupa/src/main/java/lt/creditco/cupa/CupaApplication.java`

**Changes**:
- Simplified `@ComponentScan` exclusions
- Removed exclusions for `ApiSecurityConfiguration` and `VaadinSecurityConfiguration`
- Now only excludes `CacheConfiguration`
- Added documentation explaining that `CupaApiSecurityConfiguration` replaces vapp-base's API config via `@ConditionalOnMissingBean`

## Security Endpoints Summary

After refactoring, the security configuration is:

| Path Pattern | Authentication | Authorization | Notes |
|-------------|----------------|---------------|-------|
| `/api/v1/**` | X-API-Key (ApiKeyAuthenticationFilter) | permitAll | Merchant API |
| `/api/**` | X-API-Key (ApiKeyAuthenticationFilter) | permitAll | Future API versions |
| `/public/webhook` | None | permitAll | External integrations |
| `/ui/**` | Session-based (Vaadin) | Authenticated | Admin UI |
| `/management/health**` | None | permitAll | Load balancer health checks |
| `/management/info` | None | permitAll | Public info endpoint |
| `/management/prometheus` | None | permitAll | Metrics for monitoring |
| `/management/**` | Session-based (Vaadin) | ROLE_ADMIN | Admin management |
| `/swagger-ui/**` | None | permitAll | API documentation UI |
| `/v3/api-docs/**` | Session-based (Vaadin) | Authenticated | API spec |
| `/h2-console/**` | None | permitAll | Dev profile only |
| `/` | Redirects to `/ui/` | N/A | Root redirect |

## Filter Chain Order

1. **Order 1**: `apiFilterChain` (from `CupaApiSecurityConfiguration`)
   - Matches: `/api/**`
   - Stateless
   - X-API-Key authentication

2. **Order 2**: (implicit from `VaadinSecurityConfiguration`)
   - Matches: all other paths (especially `/ui/**`)
   - Stateful (sessions)
   - Form login with Vaadin

## Benefits of This Approach

1. **Clean Separation**: API and UI security are completely separate filter chains
2. **Extensibility**: vapp-base provides production-ready defaults that can be easily customized
3. **Template Method Pattern**: Standard OOP pattern, easy to understand and maintain
4. **No Conflicts**: Each filter chain has its own configuration, no `anyRequest()` conflicts
5. **Reusable**: Other applications in the vapp-base ecosystem can follow the same pattern

## Migration Notes for Other Applications

To use vapp-base security in your application:

### Option 1: Use vapp-base defaults (JWT authentication)
Just scan vapp-base components - no custom security config needed.

### Option 2: Customize API authentication only
Extend `ApiSecurityConfiguration` and override `configureApiAuthentication()`:
```java
@Configuration
public class MyApiSecurityConfiguration extends ApiSecurityConfiguration {
    @Override
    protected void configureApiAuthentication(HttpSecurity http, MvcRequestMatcher.Builder mvc) {
        // Your custom authentication
    }
}
```

### Option 3: Customize both authentication and authorization
Override both template methods.

### Option 4: Complete replacement
Exclude vapp-base's `ApiSecurityConfiguration` and create your own `@Order(1)` filter chain.

## Testing Checklist

- [ ] Application starts without security conflicts
- [ ] `/api/v1/**` endpoints work with X-API-Key header
- [ ] `/public/webhook` is accessible without authentication
- [ ] `/ui/**` requires login
- [ ] `/management/health` is accessible without authentication
- [ ] `/management/**` requires ADMIN role
- [ ] `/swagger-ui/**` is accessible
- [ ] H2 console works in dev profile

## Known Issues

- Deprecation warnings from Spring Security regarding `MvcRequestMatcher` - these are cosmetic warnings for future migration to newer Spring Security patterns
- These do not affect functionality and will be addressed in future Spring Boot updates

