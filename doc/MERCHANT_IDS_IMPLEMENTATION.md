# User.merchantIds Backend Implementation

## Overview

This document summarizes the backend implementation for the new `merchantIds` field in the User entity, which stores a comma-separated list of allowed Merchant IDs.

## Changes Made

### 1. User Entity (`src/main/java/lt/creditco/cupa/domain/User.java`)

- Added `merchantIds` field with `@Column(name = "merchant_ids", length = 512)`
- Added `@ValidMerchantIds` validation annotation
- Added getter and setter methods for `merchantIds`
- Updated `toString()` method to include `merchantIds`

### 2. Custom Validation (`src/main/java/lt/creditco/cupa/domain/validator/`)

- **ValidMerchantIds.java**: Custom validation annotation
- **ValidMerchantIdsValidator.java**: Validation implementation that:
  - Accepts null/empty values (optional field)
  - Splits comma-separated merchant IDs
  - Validates each merchant ID exists in the database
  - Ignores empty entries in the list

### 3. DTOs

- **AdminUserDTO.java**: Added `merchantIds` field with validation, getter/setter, and updated constructor/toString
- **UserDTO.java**: Added `merchantIds` field with getter/setter and updated constructor/toString/equals/hashCode

### 4. UserService (`src/main/java/lt/creditco/cupa/service/UserService.java`)

- Updated `createUser()` method to set `merchantIds` from DTO
- Updated `updateUser()` method to set `merchantIds` from DTO

### 5. Database Migration

- **20250101000000_added_merchant_ids_to_user.xml**: Liquibase changelog to add `merchant_ids` column
- **master.xml**: Updated to include the new changelog

### 6. Tests

- **ValidMerchantIdsValidatorTest.java**: Comprehensive test suite for the validator
- **UserServiceIT.java**: Integration test for UserService with merchantIds

## Validation Rules

- Field is optional (null/empty values are valid)
- When provided, must contain valid Merchant IDs that exist in the database
- Comma-separated format (e.g., "merchant1,merchant2,merchant3")
- Whitespace around merchant IDs is trimmed
- Empty entries in the list are ignored

## Database Schema

```sql
ALTER TABLE jhi_user ADD COLUMN merchant_ids VARCHAR(512);
```

## Usage Examples

### Creating a User with Merchant IDs

```java
AdminUserDTO userDTO = new AdminUserDTO();
userDTO.setLogin("testuser");
userDTO.setEmail("test@example.com");
userDTO.setMerchantIds("merchant1,merchant2,merchant3");
User user = userService.createUser(userDTO);
```

### Updating User Merchant IDs

```java
AdminUserDTO userDTO = new AdminUserDTO();
userDTO.setId(userId);
userDTO.setMerchantIds("merchant1,merchant2");
userService.updateUser(userDTO);
```

### Validation Examples

- `null` → Valid
- `""` → Valid
- `"merchant1"` → Valid (if merchant1 exists)
- `"merchant1,merchant2"` → Valid (if both exist)
- `"merchant1, merchant2"` → Valid (spaces are trimmed)
- `"invalidMerchant"` → Invalid (if merchant doesn't exist)
- `"merchant1,invalidMerchant"` → Invalid (if any merchant doesn't exist)

## Next Steps

The backend implementation is complete. The next step would be to update the Web UI to include the merchantIds field in forms and displays.
