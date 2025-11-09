# API Versioning Implementation Guide

## Overview

Comprehensive API versioning strategy supporting multiple version negotiation methods, deprecation management, and version-aware routing across all microservices.

## Features

### 1. **Multiple Version Negotiation Methods**
- ✅ URL Path: `/api/v1/bookings`
- ✅ Accept Header: `Accept: application/vnd.irctc.v1+json`
- ✅ Custom Header: `X-API-Version: v1`
- ✅ Query Parameter: `?version=v1`

**Priority Order:**
1. URL Path (highest priority)
2. Accept Header
3. Custom Header (`X-API-Version`)
4. Query Parameter (lowest priority)

### 2. **Version-Aware Routing**
- ✅ API Gateway routes based on version
- ✅ Automatic path rewriting
- ✅ Version headers in responses
- ✅ Version metadata in request attributes

### 3. **Deprecation Management**
- ✅ Deprecation warnings in response headers
- ✅ Sunset date tracking
- ✅ Replacement version guidance
- ✅ Configurable deprecation status

### 4. **Version Management API**
- ✅ List all versions: `GET /api/versions`
- ✅ Get version info: `GET /api/versions/{version}`
- ✅ Version status: `GET /api/versions/status`

## Architecture

```
┌─────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Client    │───▶│  API Gateway     │───▶│  Microservice    │
│             │    │  (Version Resolver)│    │  (Version Aware) │
└─────────────┘    └──────────────────┘    └─────────────────┘
      │                    │                         │
      │                    ▼                         │
      │            ┌─────────────────┐              │
      │            │ Version Manager │              │
      │            │ (Registry)      │              │
      │            └─────────────────┘              │
      │                                              │
      └──────────────────────────────────────────────┘
              Version Headers in Response
```

## Version Negotiation Methods

### 1. URL Path (Recommended)

**Request:**
```http
GET /api/v1/bookings/123
```

**Response Headers:**
```
X-API-Version: v1
X-API-Version-Status: active
```

### 2. Accept Header

**Request:**
```http
GET /api/bookings/123
Accept: application/vnd.irctc.v1+json
```

**Response Headers:**
```
X-API-Version: v1
X-API-Version-Status: active
Content-Type: application/vnd.irctc.v1+json
```

### 3. Custom Header

**Request:**
```http
GET /api/bookings/123
X-API-Version: v1
```

**Response Headers:**
```
X-API-Version: v1
X-API-Version-Status: active
```

### 4. Query Parameter

**Request:**
```http
GET /api/bookings/123?version=v1
```

**Response Headers:**
```
X-API-Version: v1
X-API-Version-Status: active
```

## Deprecation Warnings

### Deprecated Version Request

**Request:**
```http
GET /api/v1/bookings/123
```

**Response Headers:**
```
X-API-Version: v1
X-API-Version-Status: deprecated
X-API-Deprecated: true
Warning: 299 - "API version v1 is deprecated. Sunset date: 2025-12-31. Please migrate to v2"
X-API-Sunset: 2025-12-31
X-API-Replacement: v2
```

## Version Management API

### Get All Versions

```http
GET /api/versions
```

**Response:**
```json
{
  "versions": {
    "v1": {
      "version": "v1",
      "deprecated": false,
      "sunsetDate": null,
      "replacementVersion": null,
      "releaseNotes": "Initial API version",
      "releaseDate": "2024-01-01"
    },
    "v2": {
      "version": "v2",
      "deprecated": false,
      "sunsetDate": null,
      "replacementVersion": null,
      "releaseNotes": "Enhanced API version with improved responses",
      "releaseDate": "2024-06-01"
    }
  },
  "defaultVersion": "v1"
}
```

### Get Version Information

```http
GET /api/versions/v1
```

**Response:**
```json
{
  "version": "v1",
  "deprecated": false,
  "sunsetDate": null,
  "replacementVersion": null,
  "releaseNotes": "Initial API version",
  "releaseDate": "2024-01-01"
}
```

### Get Version Status

```http
GET /api/versions/status
```

**Response:**
```json
{
  "defaultVersion": "v1",
  "supportedVersions": ["v1", "v2"],
  "deprecationWarningsEnabled": true
}
```

## Implementation Details

### API Gateway Components

1. **ApiVersionResolver**
   - Resolves version from multiple sources
   - Priority-based resolution
   - Version normalization

2. **ApiVersionGatewayFilter**
   - Adds version headers to responses
   - Handles deprecation warnings
   - Validates version support

3. **ApiVersionManager**
   - Manages version registry
   - Tracks deprecation status
   - Provides version metadata

4. **ApiVersionController**
   - REST API for version information
   - Version status endpoints

### Microservice Components

1. **ApiVersionInterceptor**
   - Adds version headers to responses
   - Extracts version from path
   - Handles version metadata

2. **ApiVersion Annotation**
   - Marks versioned endpoints
   - Supports deprecation metadata

## Configuration

### API Gateway Configuration

```yaml
api:
  versioning:
    default-version: v1
    enable-deprecation-warnings: true
    supported-versions: v1,v2
```

### Gateway Routes

```yaml
spring:
  cloud:
    gateway:
      default-filters:
        - name: ApiVersion
          args:
            enabled: true
      routes:
        - id: booking-service-v1
          uri: lb://IRCTC-BOOKING-SERVICE
          predicates:
            - Path=/api/v1/bookings/**
          filters:
            - RewritePath=/api/v1/bookings/(?<segment>.*), /api/bookings/${segment}
        - id: booking-service-v2
          uri: lb://IRCTC-BOOKING-SERVICE
          predicates:
            - Path=/api/v2/bookings/**
          filters:
            - RewritePath=/api/v2/bookings/(?<segment>.*), /api/bookings/${segment}
```

## Version Migration Strategy

### Phase 1: Introduce New Version
1. Deploy new version alongside existing version
2. Both versions are active
3. Monitor usage of both versions

### Phase 2: Deprecate Old Version
1. Mark old version as deprecated
2. Add deprecation warnings
3. Set sunset date (e.g., 6 months)
4. Provide migration guide

### Phase 3: Sunset Old Version
1. Remove old version after sunset date
2. Return 410 Gone for old version requests
3. Redirect to new version if possible

## Best Practices

### 1. **Version Naming**
- Use semantic versioning: `v1`, `v2`, `v3`
- Avoid breaking changes in same major version
- Document version differences

### 2. **Deprecation Timeline**
- Announce deprecation 6+ months in advance
- Provide clear migration path
- Set realistic sunset dates

### 3. **Version Support**
- Support at least 2 versions simultaneously
- Maintain backward compatibility when possible
- Clear communication about breaking changes

### 4. **Version Headers**
- Always include `X-API-Version` in responses
- Use `Warning` header for deprecation
- Provide replacement version in headers

### 5. **Documentation**
- Document all supported versions
- Provide migration guides
- Include version in API documentation

## Example Usage

### Client Request (URL Path)

```bash
curl -X GET "http://localhost:8090/api/v1/bookings/123" \
  -H "Accept: application/json"
```

**Response:**
```http
HTTP/1.1 200 OK
X-API-Version: v1
X-API-Version-Status: active
Content-Type: application/json

{
  "id": 123,
  "pnrNumber": "PNR123456",
  "status": "CONFIRMED"
}
```

### Client Request (Accept Header)

```bash
curl -X GET "http://localhost:8090/api/bookings/123" \
  -H "Accept: application/vnd.irctc.v1+json"
```

**Response:**
```http
HTTP/1.1 200 OK
X-API-Version: v1
X-API-Version-Status: active
Content-Type: application/vnd.irctc.v1+json
```

### Client Request (Custom Header)

```bash
curl -X GET "http://localhost:8090/api/bookings/123" \
  -H "X-API-Version: v1"
```

### Client Request (Query Parameter)

```bash
curl -X GET "http://localhost:8090/api/bookings/123?version=v1"
```

## Deprecation Example

### Deprecate v1

```java
versionManager.deprecateVersion("v1", 
    LocalDate.of(2025, 12, 31), 
    "v2");
```

### Request to Deprecated Version

```bash
curl -X GET "http://localhost:8090/api/v1/bookings/123"
```

**Response:**
```http
HTTP/1.1 200 OK
X-API-Version: v1
X-API-Version-Status: deprecated
X-API-Deprecated: true
Warning: 299 - "API version v1 is deprecated. Sunset date: 2025-12-31. Please migrate to v2"
X-API-Sunset: 2025-12-31
X-API-Replacement: v2
```

## Files Created

### API Gateway
- `ApiVersionResolver.java` - Version resolution logic
- `ApiVersionGatewayFilter.java` - Gateway filter for version handling
- `ApiVersionManager.java` - Version registry and management
- `ApiVersionInfo.java` - Version metadata model
- `ApiVersionController.java` - Version management API

### Microservices
- `ApiVersionInterceptor.java` - Response header interceptor
- `ApiVersion.java` - Annotation for versioning
- `WebMvcConfig.java` - Interceptor registration

## Benefits

1. **Flexible Version Negotiation**
   - Multiple methods for client convenience
   - Priority-based resolution
   - Backward compatibility

2. **Clear Deprecation Management**
   - Warnings in headers
   - Sunset date tracking
   - Migration guidance

3. **Version Awareness**
   - Automatic version detection
   - Version headers in responses
   - Version metadata tracking

4. **API Evolution**
   - Safe introduction of new versions
   - Gradual migration path
   - Clear communication

## Conclusion

The API versioning implementation provides a comprehensive solution for managing API versions across microservices, supporting multiple negotiation methods, deprecation management, and clear version communication.

