# Microservices Pipeline Template Guide

## 📋 Overview

This guide explains how to use the reusable pipeline templates to create CI/CD pipelines for each microservice.

---

## 🎯 Template Files

### 1. GitHub Actions Template
**Location:** `.github/workflows/.microservice-template.yml`

### 2. Jenkins Template
**Location:** `Jenkinsfile-microservice-template`

---

## 🚀 Quick Start

### Step 1: Identify Your Service

**Service Information:**
- **Service Name**: e.g., `user-service`, `booking-service`
- **Service Directory**: e.g., `irctc-user-service`, `irctc-booking-service`

### Step 2: Create GitHub Actions Pipeline

**For GitHub Actions:**

1. **Copy the template:**
   ```bash
   cp .github/workflows/.microservice-template.yml \
      .github/workflows/user-service-pipeline.yml
   ```

2. **Replace placeholders:**
   - `{{SERVICE_NAME}}` → `User Service` (or `user-service`)
   - `{{SERVICE_DIR}}` → `irctc-user-service`

3. **Example replacements:**
   ```yaml
   # Before
   name: {{SERVICE_NAME}} Service Pipeline
   SERVICE_DIR: {{SERVICE_DIR}}
   
   # After
   name: User Service Pipeline
   SERVICE_DIR: irctc-user-service
   ```

### Step 3: Create Jenkins Pipeline

**For Jenkins:**

1. **Copy the template:**
   ```bash
   cp Jenkinsfile-microservice-template \
      Jenkinsfile-user-service
   ```

2. **Replace placeholders:**
   - `{{SERVICE_NAME}}` → `User Service`
   - `{{SERVICE_DIR}}` → `irctc-user-service`

3. **Configure Jenkins job:**
   - Create new Pipeline job
   - Set "Pipeline script from SCM"
   - Script path: `Jenkinsfile-user-service`

---

## 📝 Service Configuration Matrix

| Service | SERVICE_NAME | SERVICE_DIR | Port |
|---------|-------------|-------------|------|
| Eureka Server | `Eureka Server` | `irctc-eureka-server` | 8761 |
| API Gateway | `API Gateway` | `irctc-api-gateway` | 8090 |
| Config Server | `Config Server` | `irctc-config-server` | - |
| User Service | `User Service` | `irctc-user-service` | 8091 |
| Train Service | `Train Service` | `irctc-train-service` | 8092 |
| Booking Service | `Booking Service` | `irctc-booking-service` | 8093 |
| Payment Service | `Payment Service` | `irctc-payment-service` | 8094 |
| Notification Service | `Notification Service` | `irctc-notification-service` | 8095 |
| External APIs | `External APIs` | `irctc-external-apis` | - |
| Swagger Hub | `Swagger Hub` | `irctc-swagger-hub` | - |

---

## 🔧 Template Customization

### Common Customizations

#### 1. Add Service-Specific Tests

**In GitHub Actions:**
```yaml
- name: Run integration tests
  working-directory: ./${{ env.SERVICE_DIR }}
  run: ./mvnw integration-test
```

#### 2. Add Database Migrations

**In GitHub Actions:**
```yaml
- name: Run database migrations
  working-directory: ./${{ env.SERVICE_DIR }}
  run: ./mvnw flyway:migrate
```

#### 3. Add Docker Build

**In GitHub Actions:**
```yaml
- name: Build Docker image
  uses: docker/build-push-action@v5
  with:
    context: ./${{ env.SERVICE_DIR }}
    push: false
    tags: ${{ env.SERVICE_NAME }}:latest
```

#### 4. Add Deployment Steps

**In GitHub Actions:**
```yaml
- name: Deploy to staging
  if: github.ref == 'refs/heads/develop'
  run: |
    # Your deployment script
    ./deploy.sh staging
```

---

## 📋 Template Features

### GitHub Actions Template Features

✅ **Automatic Triggers:**
- Push to `main` or `develop`
- Pull requests
- Manual workflow dispatch
- Path-based triggers (only builds when service changes)

✅ **Build Steps:**
- Java 21 setup
- Maven dependency caching
- Shared dependencies build
- Service compilation
- Test execution
- Package creation

✅ **Quality Checks:**
- Test coverage reporting
- Security scanning (OWASP)
- Test result archiving

✅ **Artifacts:**
- JAR file upload
- Test reports
- Security reports

✅ **Deployment:**
- GitHub Packages publishing (on release)
- Conditional deployment

### Jenkins Template Features

✅ **Cross-Platform Support:**
- macOS Java detection
- Linux Java detection
- Automatic JAVA_HOME setup

✅ **Build Stages:**
- Checkout with fallback
- Shared dependencies build
- Service compilation
- Test execution
- Package creation
- Publishing (optional)

✅ **Error Handling:**
- Graceful SCM checkout fallback
- Test result archiving
- Build summary reporting

---

## 🎨 Example: User Service Pipeline

### GitHub Actions Example

**File:** `.github/workflows/user-service-pipeline.yml`

```yaml
name: User Service Pipeline

on:
  push:
    branches:
      - main
      - develop
    paths:
      - 'irctc-user-service/**'
      - '.github/workflows/user-service-pipeline.yml'
      # ... rest of configuration
```

### Jenkins Example

**File:** `Jenkinsfile-user-service`

```groovy
pipeline {
    agent any
    
    environment {
        SERVICE_NAME = 'User Service'
        SERVICE_DIR = 'irctc-user-service'
        # ... rest of configuration
    }
    # ... rest of pipeline
}
```

---

## 🔄 Path-Based Triggers

### How It Works

**GitHub Actions:**
```yaml
paths:
  - 'irctc-user-service/**'      # Only trigger on service changes
  - '.github/workflows/user-service-pipeline.yml'  # Or workflow changes
  - 'pom.xml'                    # Or root pom changes
  - 'irctc-shared-events/**'     # Or shared library changes
```

**Benefits:**
- ✅ Only builds when service changes
- ✅ Faster CI/CD cycles
- ✅ Reduced resource usage
- ✅ Parallel builds possible

---

## 🚀 Best Practices

### 1. Use Consistent Naming

**Pattern:**
- GitHub Actions: `{service-name}-pipeline.yml`
- Jenkins: `Jenkinsfile-{service-name}`

**Examples:**
- `user-service-pipeline.yml`
- `Jenkinsfile-user-service`

### 2. Keep Templates Updated

**When updating templates:**
1. Update `.microservice-template.yml`
2. Update `Jenkinsfile-microservice-template`
3. Document changes in this guide
4. Notify teams of updates

### 3. Service-Specific Customizations

**Add customizations in:**
- Service-specific workflow files
- Don't modify templates directly
- Document customizations

### 4. Test Pipeline Changes

**Before deploying:**
1. Test in feature branch
2. Verify path triggers work
3. Check build artifacts
4. Validate deployment steps

---

## 📊 Monitoring

### GitHub Actions

**View pipelines:**
- Go to **Actions** tab
- Select service pipeline
- View build history

**Metrics:**
- Build duration
- Success rate
- Test coverage
- Security scan results

### Jenkins

**View pipelines:**
- Go to Jenkins dashboard
- Select service job
- View build history

**Metrics:**
- Build duration
- Success rate
- Test results
- Artifact sizes

---

## 🔍 Troubleshooting

### Common Issues

#### 1. Pipeline Not Triggering

**Check:**
- Path filters are correct
- Service directory matches
- Branch name matches

#### 2. Build Failures

**Check:**
- Java version compatibility
- Maven wrapper permissions
- Shared dependencies built

#### 3. Tests Failing

**Check:**
- Test database configuration
- Service dependencies
- Test data setup

---

## 📚 Next Steps

1. **Create pipelines for all services**
   - Use templates
   - Customize per service
   - Test thoroughly

2. **Set up integration testing**
   - Create integration test pipeline
   - Coordinate service builds
   - Test service interactions

3. **Configure deployment**
   - Set up staging environment
   - Configure production deployment
   - Add rollback procedures

4. **Monitor and optimize**
   - Track build times
   - Optimize slow builds
   - Improve test coverage

---

## 📝 Checklist

- [ ] Template files created
- [ ] Service pipelines created
- [ ] Path triggers configured
- [ ] Test execution working
- [ ] Artifacts uploading
- [ ] Security scanning active
- [ ] Documentation complete
- [ ] Teams trained

---

*Last Updated: November 2025*

