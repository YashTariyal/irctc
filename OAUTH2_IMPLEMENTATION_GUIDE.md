# OAuth2 / OpenID Connect Implementation Guide

## 📋 Overview

This guide documents the OAuth2/OpenID Connect (OIDC) integration implemented for the IRCTC microservices architecture. The implementation provides industry-standard authentication and authorization while maintaining backward compatibility with existing JWT tokens.

---

## 🎯 Implementation Summary

### Components Implemented

1. **OAuth2 Authorization Server** (User Service)
   - Issues JWT tokens
   - Supports Authorization Code, Client Credentials, and Refresh Token flows
   - OpenID Connect (OIDC) support

2. **OAuth2 Resource Server** (User Service & API Gateway)
   - Validates JWT tokens
   - Extracts user information
   - Protects endpoints

3. **API Gateway Integration**
   - Validates tokens at gateway level
   - Forwards user information to downstream services
   - Centralized authentication

---

## 📁 Files Created/Modified

### User Service

**New Files:**
- `irctc-user-service/src/main/java/com/irctc/user/config/OAuth2ResourceServerConfig.java`
- `irctc-user-service/src/main/java/com/irctc/user/config/OAuth2AuthorizationServerConfig.java`

**Modified Files:**
- `irctc-user-service/pom.xml` - Added OAuth2 dependencies
- `irctc-user-service/src/main/resources/application.yml` - Added OAuth2 configuration
- `irctc-user-service/src/main/java/com/irctc/user/config/SimpleSecurityConfig.java` - Made conditional

### API Gateway

**New Files:**
- `irctc-api-gateway/src/main/java/com/irctc/gateway/filter/OAuth2AuthenticationFilter.java`
- `irctc-api-gateway/src/main/java/com/irctc/gateway/config/OAuth2GatewayConfig.java`

**Modified Files:**
- `irctc-api-gateway/pom.xml` - Added OAuth2 Resource Server dependency
- `irctc-api-gateway/src/main/resources/application.yml` - Added OAuth2 configuration

---

## 🔧 Configuration

### User Service Configuration

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          # Option 1: Use JWK Set URI (for external OAuth2 server)
          # jwk-set-uri: https://auth-server/.well-known/jwks.json
          
          # Option 2: Use Issuer URI (auto-discovers JWK Set URI)
          issuer-uri: http://localhost:8091
          
          # Token validation settings
          time-to-live: 3600 # 1 hour
          
      # OAuth2 Client Configuration (for SSO)
      client:
        registration:
          irctc:
            provider: irctc
            client-id: irctc-client
            client-secret: irctc-secret
            authorization-grant-type: authorization_code
            redirect-uri: http://localhost:8090/login/oauth2/code/irctc
            scope: openid, profile, read, write
        provider:
          irctc:
            issuer-uri: http://localhost:8091
```

### API Gateway Configuration

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          # Use Issuer URI from User Service (Authorization Server)
          issuer-uri: http://localhost:8091
          # Token validation settings
          time-to-live: 3600 # 1 hour
```

---

## 🚀 Usage

### 1. Get Access Token (Authorization Code Flow)

**Step 1: Redirect to Authorization Endpoint**
```
GET http://localhost:8091/oauth2/authorize?client_id=irctc-client&response_type=code&redirect_uri=http://localhost:8090/login/oauth2/code/irctc&scope=openid profile read write
```

**Step 2: Exchange Authorization Code for Token**
```bash
curl -X POST http://localhost:8091/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u irctc-client:irctc-secret \
  -d "grant_type=authorization_code" \
  -d "code={authorization_code}" \
  -d "redirect_uri=http://localhost:8090/login/oauth2/code/irctc"
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "scope": "openid profile read write"
}
```

### 2. Get Access Token (Client Credentials Flow)

```bash
curl -X POST http://localhost:8091/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u irctc-client:irctc-secret \
  -d "grant_type=client_credentials" \
  -d "scope=read write"
```

### 3. Use Access Token

**Make Authenticated Request:**
```bash
curl -X GET http://localhost:8090/api/v1/users/me \
  -H "Authorization: Bearer {access_token}"
```

**API Gateway will:**
1. Validate the token
2. Extract user information
3. Forward to downstream service with headers:
   - `X-User-Id`: User ID
   - `X-User-Email`: User email
   - `X-User-Authorities`: User authorities
   - `X-Authenticated`: true

### 4. Refresh Token

```bash
curl -X POST http://localhost:8091/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u irctc-client:irctc-secret \
  -d "grant_type=refresh_token" \
  -d "refresh_token={refresh_token}"
```

---

## 🔐 OAuth2 Endpoints

### Authorization Server Endpoints (User Service: 8091)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/oauth2/authorize` | GET | Authorization endpoint |
| `/oauth2/token` | POST | Token endpoint |
| `/oauth2/introspect` | POST | Token introspection |
| `/oauth2/revoke` | POST | Token revocation |
| `/.well-known/openid-configuration` | GET | OIDC discovery |
| `/.well-known/oauth-authorization-server` | GET | OAuth2 metadata |
| `/.well-known/jwks.json` | GET | JSON Web Key Set |

### Public Endpoints (No Authentication Required)

- `/actuator/**`
- `/api/v1/users/register`
- `/api/v1/users/login`
- `/swagger-ui/**`
- `/api-docs/**`
- `/oauth2/**`
- `/.well-known/**`

### Protected Endpoints (Require Authentication)

- All other endpoints require a valid OAuth2 token

---

## 📊 Token Structure

### Access Token (JWT)

```json
{
  "sub": "admin",
  "aud": ["irctc-client"],
  "nbf": 1234567890,
  "scope": ["openid", "profile", "read", "write"],
  "iss": "http://localhost:8091",
  "exp": 1234571490,
  "iat": 1234567890,
  "jti": "token-id",
  "authorities": ["ROLE_ADMIN", "ROLE_USER"]
}
```

---

## 🔄 Integration with Existing JWT

The implementation maintains backward compatibility with existing JWT tokens. You can:

1. **Use OAuth2 tokens** (recommended for new integrations)
2. **Continue using existing JWT tokens** (for backward compatibility)

To enable simple JWT mode (disable OAuth2):
```yaml
security:
  simple:
    enabled: true
```

---

## 🧪 Testing

### Test OAuth2 Flow

1. **Start Services:**
   ```bash
   # Start Eureka
   cd irctc-eureka-server && ./mvnw spring-boot:run
   
   # Start User Service (Authorization Server)
   cd irctc-user-service && ./mvnw spring-boot:run
   
   # Start API Gateway
   cd irctc-api-gateway && ./mvnw spring-boot:run
   ```

2. **Get Token:**
   ```bash
   curl -X POST http://localhost:8091/oauth2/token \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -u irctc-client:irctc-secret \
     -d "grant_type=client_credentials" \
     -d "scope=read write"
   ```

3. **Use Token:**
   ```bash
   curl -X GET http://localhost:8090/api/v1/users/me \
     -H "Authorization: Bearer {access_token}"
   ```

---

## 📝 Client Registration

### Default Client

- **Client ID:** `irctc-client`
- **Client Secret:** `irctc-secret`
- **Grant Types:** Authorization Code, Refresh Token, Client Credentials
- **Scopes:** openid, profile, read, write
- **Redirect URIs:** 
  - `http://localhost:8090/login/oauth2/code/irctc`
  - `http://localhost:8090/authorized`

### Register New Client

Update `OAuth2AuthorizationServerConfig.java`:

```java
RegisteredClient newClient = RegisteredClient.withId(UUID.randomUUID().toString())
    .clientId("new-client-id")
    .clientSecret("{noop}new-client-secret")
    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
    .redirectUri("http://localhost:8090/login/oauth2/code/new-client")
    .scope(OidcScopes.OPENID)
    .scope("read")
    .scope("write")
    .build();
```

---

## 🔒 Security Best Practices

1. **Use HTTPS in Production**
   - OAuth2 endpoints should only be accessible via HTTPS
   - Update `issuer-uri` to use HTTPS

2. **Secure Client Secrets**
   - Store client secrets securely (use environment variables)
   - Use password encoding: `{bcrypt}...` or `{noop}...` for development

3. **Token Expiration**
   - Access tokens: 1 hour (configurable)
   - Refresh tokens: 7 days (configurable)

4. **Scope Limitation**
   - Grant minimum required scopes
   - Use principle of least privilege

5. **Token Storage**
   - Never store tokens in localStorage (XSS risk)
   - Use httpOnly cookies for web applications
   - Use secure storage for mobile apps

---

## 🐛 Troubleshooting

### Issue: "OAuth2 configuration missing"

**Solution:** Ensure `spring.security.oauth2.resourceserver.jwt.issuer-uri` is configured in `application.yml`

### Issue: "Invalid token"

**Solution:** 
- Check token expiration
- Verify issuer URI matches token issuer
- Ensure token is properly formatted

### Issue: "CORS errors"

**Solution:** Add CORS configuration:
```yaml
spring:
  web:
    cors:
      allowed-origins: "*"
      allowed-methods: "*"
      allowed-headers: "*"
```

---

## 📚 References

- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [OAuth2 Authorization Server](https://docs.spring.io/spring-authorization-server/docs/current/reference/html/)
- [OpenID Connect](https://openid.net/specs/openid-connect-core-1_0.html)

---

## ✅ Next Steps

1. **Database-backed Client Registration** - Store clients in database instead of memory
2. **Token Introspection** - Implement token introspection endpoint
3. **Token Revocation** - Implement token revocation
4. **User Info Endpoint** - Implement OIDC UserInfo endpoint
5. **PKCE Support** - Add PKCE for mobile apps

---

*Last Updated: November 2025*

