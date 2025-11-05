# Microservices Pipeline Examples

## 📋 Overview

This document provides concrete examples of pipeline implementations for specific services, demonstrating how to use the templates.

---

## 🎯 Example Services

### 1. Eureka Server (Infrastructure Service)

**Service Details:**
- **Name:** Eureka Server
- **Directory:** `irctc-eureka-server`
- **Port:** 8761
- **Type:** Infrastructure (Service Discovery)

#### GitHub Actions Pipeline

**File:** `.github/workflows/eureka-server-pipeline.yml`

**Key Features:**
- Path-based triggers (only builds when Eureka changes)
- Simple build (no database dependencies)
- Fast deployment (infrastructure service)

#### Jenkins Pipeline

**File:** `Jenkinsfile-eureka-server`

**Key Features:**
- Cross-platform Java detection
- Shared dependency handling
- Artifact archiving

---

### 2. User Service (Business Service)

**Service Details:**
- **Name:** User Service
- **Directory:** `irctc-user-service`
- **Port:** 8091
- **Type:** Business Service (User Management)

#### GitHub Actions Pipeline

**File:** `.github/workflows/user-service-pipeline.yml`

**Key Features:**
- Path-based triggers
- Database migration support (if needed)
- Security scanning (critical for user data)
- Test coverage reporting

#### Jenkins Pipeline

**File:** `Jenkinsfile-user-service`

**Key Features:**
- Comprehensive testing
- Security validation
- Artifact management

---

## 🔍 Pipeline File Locations

### GitHub Actions

```
.github/workflows/
├── .microservice-template.yml      # Template (DO NOT EDIT)
├── eureka-server-pipeline.yml      # Example: Eureka Server
└── user-service-pipeline.yml       # Example: User Service
```

### Jenkins

```
Repository Root/
├── Jenkinsfile-microservice-template  # Template (DO NOT EDIT)
├── Jenkinsfile-eureka-server          # Example: Eureka Server
└── Jenkinsfile-user-service           # Example: User Service
```

---

## 📝 How to Use These Examples

### Step 1: Review the Examples

**Examine the generated files:**
```bash
# GitHub Actions
cat .github/workflows/eureka-server-pipeline.yml
cat .github/workflows/user-service-pipeline.yml

# Jenkins
cat Jenkinsfile-eureka-server
cat Jenkinsfile-user-service
```

### Step 2: Understand the Pattern

**Notice the structure:**
- Service name and directory are replaced
- All other logic remains the same
- Path triggers are service-specific

### Step 3: Create Your Service Pipeline

**For a new service (e.g., Booking Service):**

```bash
# GitHub Actions
cp .github/workflows/.microservice-template.yml \
   .github/workflows/booking-service-pipeline.yml

# Replace placeholders
sed -i '' 's/{{SERVICE_NAME}}/Booking Service/g' \
   .github/workflows/booking-service-pipeline.yml
sed -i '' 's/{{SERVICE_DIR}}/irctc-booking-service/g' \
   .github/workflows/booking-service-pipeline.yml

# Jenkins
cp Jenkinsfile-microservice-template \
   Jenkinsfile-booking-service

# Replace placeholders
sed -i '' 's/{{SERVICE_NAME}}/Booking Service/g' \
   Jenkinsfile-booking-service
sed -i '' 's/{{SERVICE_DIR}}/irctc-booking-service/g' \
   Jenkinsfile-booking-service
```

---

## 🎨 Customization Examples

### Example 1: Add Database Migrations

**For services with database (e.g., User Service):**

```yaml
# Add to GitHub Actions workflow
- name: Run database migrations
  working-directory: ./${{ env.SERVICE_DIR }}
  run: ./mvnw flyway:migrate
  continue-on-error: true
```

### Example 2: Add Integration Tests

**For services requiring integration testing:**

```yaml
# Add to GitHub Actions workflow
- name: Run integration tests
  working-directory: ./${{ env.SERVICE_DIR }}
  run: ./mvnw verify -P integration-test
  env:
    DATABASE_URL: ${{ secrets.TEST_DATABASE_URL }}
```

### Example 3: Add Docker Build

**For containerized deployments:**

```yaml
# Add to GitHub Actions workflow
- name: Build Docker image
  uses: docker/build-push-action@v5
  with:
    context: ./${{ env.SERVICE_DIR }}
    push: false
    tags: |
      ${{ env.SERVICE_NAME }}:latest
      ${{ env.SERVICE_NAME }}:${{ github.sha }}
```

### Example 4: Add Deployment Steps

**For automated deployment:**

```yaml
# Add to GitHub Actions workflow
- name: Deploy to staging
  if: github.ref == 'refs/heads/develop'
  run: |
    ./deploy.sh staging ${{ env.SERVICE_NAME }}
  env:
    DEPLOY_TOKEN: ${{ secrets.DEPLOY_TOKEN }}
```

---

## 🔄 Service-Specific Considerations

### Infrastructure Services

**Eureka Server:**
- ✅ Simple build (no database)
- ✅ Fast deployment
- ✅ High availability critical
- ⚠️ No complex dependencies

**API Gateway:**
- ✅ Route configuration validation
- ✅ Performance testing
- ✅ Integration with all services
- ⚠️ Complex routing logic

**Config Server:**
- ✅ Configuration validation
- ✅ Version management
- ✅ Security considerations
- ⚠️ Configuration dependencies

### Business Services

**User Service:**
- ✅ Security scanning critical
- ✅ Authentication testing
- ✅ Database migrations
- ⚠️ Sensitive data handling

**Train Service:**
- ✅ Data validation
- ✅ Search performance testing
- ✅ Cache invalidation
- ⚠️ Large dataset handling

**Booking Service:**
- ✅ Transaction testing
- ✅ Complex business logic
- ✅ Integration with payment
- ⚠️ Concurrency testing

**Payment Service:**
- ✅ Security paramount
- ✅ PCI compliance checks
- ✅ Transaction integrity
- ⚠️ Financial regulations

**Notification Service:**
- ✅ External service integration
- ✅ Message queue testing
- ✅ Delivery verification
- ⚠️ Third-party dependencies

---

## 📊 Testing the Examples

### Test GitHub Actions

1. **Make a change to Eureka Server:**
   ```bash
   echo "# Test" >> irctc-eureka-server/README.md
   git add .
   git commit -m "Test Eureka pipeline"
   git push
   ```

2. **Check GitHub Actions:**
   - Go to Actions tab
   - Find "Eureka Server Pipeline"
   - Verify it triggers

### Test Jenkins

1. **Create Jenkins job:**
   - New Item → Pipeline
   - Name: `irctc-eureka-server`
   - Pipeline script from SCM
   - Script path: `Jenkinsfile-eureka-server`

2. **Trigger build:**
   - Build Now
   - Check console output
   - Verify stages execute

---

## ✅ Verification Checklist

For each service pipeline:

- [ ] Template copied correctly
- [ ] Placeholders replaced
- [ ] Path triggers configured
- [ ] Service directory matches
- [ ] Build steps execute
- [ ] Tests run successfully
- [ ] Artifacts generated
- [ ] Security scan runs
- [ ] Documentation updated

---

## 🚀 Next Steps

1. **Create pipelines for remaining services:**
   - Train Service
   - Booking Service
   - Payment Service
   - Notification Service
   - API Gateway
   - Config Server
   - External APIs
   - Swagger Hub

2. **Set up integration testing:**
   - Create integration test pipeline
   - Coordinate service builds
   - Test service interactions

3. **Configure deployment:**
   - Set up staging environment
   - Configure production deployment
   - Add rollback procedures

---

*Last Updated: November 2025*

