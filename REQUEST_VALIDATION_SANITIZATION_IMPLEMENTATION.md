# Request Validation & Sanitization Implementation Guide

## Overview

Comprehensive request validation and sanitization implementation to prevent security vulnerabilities including XSS, SQL injection, path traversal, and ensure data integrity through Bean Validation.

## Features

### 1. **Bean Validation (JSR-303)**
- ✅ Standard validation annotations (`@NotNull`, `@NotBlank`, `@Size`, `@Pattern`, `@Email`)
- ✅ Custom validation annotations (`@ValidPnr`, `@ValidIndianPhone`)
- ✅ Validation error handling
- ✅ Automatic validation on `@Valid` annotated parameters

### 2. **Input Sanitization**
- ✅ XSS prevention (script tags, event handlers, javascript:)
- ✅ SQL injection prevention (SQL keywords, special characters)
- ✅ Path traversal prevention (`../`, `..\\`)
- ✅ HTML tag removal
- ✅ Control character removal

### 3. **Custom Validators**
- ✅ PNR number validator (10 alphanumeric characters)
- ✅ Indian phone number validator (10 digits starting with 6-9)
- ✅ Extensible validator framework

### 4. **Security Filters**
- ✅ Input sanitization filter
- ✅ Request blocking on violations
- ✅ Security violation logging
- ✅ Configurable behavior

## Architecture

```
┌─────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Client    │───▶│  Sanitization    │───▶│   Controller    │
│             │    │     Filter       │    │   (@Valid)      │
└─────────────┘    └──────────────────┘    └─────────────────┘
      │                    │                         │
      │                    ▼                         │
      │            ┌─────────────────┐              │
      │            │  Validation     │              │
      │            │  Annotations    │              │
      │            └─────────────────┘              │
      │                                              │
      └──────────────────────────────────────────────┘
              Validation Errors (if any)
```

## Validation Annotations

### Standard Annotations

```java
@NotNull(message = "User ID is required")
@Positive(message = "User ID must be positive")
private Long userId;

@NotBlank(message = "PNR number is required")
@ValidPnr
private String pnrNumber;

@Email(message = "Email must be valid")
private String email;

@Pattern(regexp = "CONFIRMED|CANCELLED|PENDING", 
         message = "Status must be one of: CONFIRMED, CANCELLED, PENDING")
private String status;

@DecimalMin(value = "0.0", inclusive = false, 
           message = "Total fare must be greater than 0")
@Digits(integer = 8, fraction = 2)
private BigDecimal totalFare;
```

### Custom Annotations

```java
@ValidPnr
private String pnrNumber;

@ValidIndianPhone
private String phoneNumber;
```

## Sanitization Patterns

### XSS Prevention

**Detected Patterns:**
- `<script>` tags
- `<iframe>` tags
- `javascript:` protocol
- Event handlers (`onclick`, `onerror`, etc.)
- `eval()` calls
- `expression()` CSS
- `vbscript:` protocol
- `<style>` tags

**Example:**
```java
// Input: <script>alert('XSS')</script>
// Result: Blocked or sanitized
```

### SQL Injection Prevention

**Detected Patterns:**
- SQL keywords (`SELECT`, `INSERT`, `UPDATE`, `DELETE`, `DROP`, etc.)
- SQL comment patterns (`--`, `/* */`)
- Union-based attacks
- Boolean-based attacks (`OR 1=1`, `AND 1=1`)

**Example:**
```java
// Input: ' OR '1'='1
// Result: Blocked or sanitized
```

### Path Traversal Prevention

**Detected Patterns:**
- `../` (Unix)
- `..\\` (Windows)
- URL-encoded variants (`%2e%2e%2f`)

**Example:**
```java
// Input: ../../../etc/passwd
// Result: Blocked or sanitized
```

## Sanitization Utilities

### HTML Sanitization

```java
String sanitized = SanitizationUtils.sanitizeHtml(input);
// Removes: <script>, <style>, <iframe>, and all HTML tags
```

### SQL Sanitization

```java
String sanitized = SanitizationUtils.sanitizeSql(input);
// Escapes: single quotes, removes SQL comments
```

### Path Sanitization

```java
String sanitized = SanitizationUtils.sanitizePath(input);
// Removes: ../, ..\\, null bytes
```

### Email Sanitization

```java
String sanitized = SanitizationUtils.sanitizeEmail(input);
// Removes: HTML tags, normalizes to lowercase
```

### Phone Sanitization

```java
String sanitized = SanitizationUtils.sanitizePhone(input);
// Removes: all non-digit characters except +
```

## Configuration

### Application Properties

```yaml
validation:
  sanitization:
    enabled: true  # Enable input sanitization
    block-on-violation: true  # Block requests with malicious input
    log-violations: true  # Log security violations
```

### Maven Dependency

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

## Usage Examples

### Controller with Validation

```java
@RestController
@RequestMapping("/api/bookings")
public class SimpleBookingController {
    
    @PostMapping
    public ResponseEntity<SimpleBooking> createBooking(
            @Valid @RequestBody SimpleBooking booking) {
        // Validation happens automatically
        // If validation fails, MethodArgumentNotValidException is thrown
        return ResponseEntity.ok(bookingService.createBooking(booking));
    }
}
```

### Entity with Validation

```java
@Entity
public class SimpleBooking {
    
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;
    
    @NotBlank(message = "PNR number is required")
    @ValidPnr
    private String pnrNumber;
    
    @NotNull(message = "Total fare is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal totalFare;
}
```

### Custom Validator

```java
@ValidPnr
private String pnrNumber;

// Validator implementation
public class PnrValidator implements ConstraintValidator<ValidPnr, String> {
    private static final Pattern PNR_PATTERN = Pattern.compile("^[A-Z0-9]{10}$");
    
    @Override
    public boolean isValid(String pnr, ConstraintValidatorContext context) {
        if (pnr == null || pnr.isEmpty()) {
            return false;
        }
        return PNR_PATTERN.matcher(pnr.toUpperCase()).matches();
    }
}
```

## Validation Error Response

### Standard Error Format

```json
{
  "timestamp": "2024-12-28T10:30:00",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Validation failed for the request",
  "path": "/api/bookings",
  "errors": {
    "fieldErrors": {
      "userId": "User ID is required",
      "pnrNumber": "Invalid PNR number format. PNR must be 10 alphanumeric characters.",
      "totalFare": "Total fare must be greater than 0"
    }
  },
  "detail": "Validation failed for 3 field(s)"
}
```

## Security Violation Handling

### Blocked Request Response

```json
{
  "error": "VALIDATION_ERROR",
  "message": "Invalid input detected. Request blocked for security reasons.",
  "status": 400
}
```

### Logged Violations

```
⚠️  XSS pattern detected: <script[^>]*>.*?</script> in input: <script>alert('XSS')</script>
🚫 Security violation blocked: Malicious input detected in query parameters - Path: /api/bookings?q=<script>
```

## Best Practices

### 1. **Validation Strategy**
- Validate at the controller level with `@Valid`
- Use appropriate validation annotations
- Provide clear error messages
- Validate both required and optional fields

### 2. **Sanitization Strategy**
- Sanitize before validation
- Use whitelist approach (allow known good patterns)
- Log all security violations
- Block malicious requests by default

### 3. **Custom Validators**
- Create validators for business-specific rules
- Reuse validators across entities
- Provide clear validation messages
- Test validators thoroughly

### 4. **Error Handling**
- Return user-friendly error messages
- Don't expose internal validation logic
- Log validation failures for monitoring
- Provide field-level error details

### 5. **Performance**
- Sanitization filter runs early in the filter chain
- Validation happens at controller level
- Cache compiled patterns
- Minimize regex complexity

## Security Considerations

### 1. **XSS Prevention**
- Always sanitize user input
- Use Content Security Policy (CSP)
- Encode output in templates
- Validate and sanitize URLs

### 2. **SQL Injection Prevention**
- Use parameterized queries (JPA/Hibernate)
- Never concatenate user input in SQL
- Validate input types
- Use ORM frameworks

### 3. **Path Traversal Prevention**
- Validate file paths
- Use whitelist for allowed paths
- Normalize paths
- Check for directory traversal patterns

### 4. **Input Length Limits**
- Set maximum length for all inputs
- Validate array/list sizes
- Limit request body size
- Monitor for oversized requests

## Files Created

### Core Components
- `InputSanitizationFilter.java` - Security filter
- `SanitizationUtils.java` - Sanitization utilities
- `ValidPnr.java` - Custom PNR validator annotation
- `PnrValidator.java` - PNR validator implementation
- `ValidIndianPhone.java` - Custom phone validator annotation
- `IndianPhoneValidator.java` - Phone validator implementation

### Configuration
- `application.yml` - Validation configuration
- `pom.xml` - Validation dependency

## Benefits

1. **Security**
   - Prevents XSS attacks
   - Prevents SQL injection
   - Prevents path traversal
   - Blocks malicious input

2. **Data Integrity**
   - Ensures required fields are present
   - Validates data formats
   - Enforces business rules
   - Prevents invalid data

3. **User Experience**
   - Clear error messages
   - Field-level validation feedback
   - Early validation (before processing)
   - Consistent error format

4. **Maintainability**
   - Declarative validation (annotations)
   - Reusable validators
   - Centralized error handling
   - Easy to extend

5. **Compliance**
   - Security best practices
   - Input validation requirements
   - Data protection standards
   - Audit trail (logging)

## Conclusion

The Request Validation & Sanitization implementation provides comprehensive protection against security vulnerabilities while ensuring data integrity through Bean Validation. The combination of automatic validation, custom validators, and input sanitization creates a robust security layer for the microservices.

