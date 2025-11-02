# Swagger Documentation Hosting Guide

Complete guide for hosting Swagger/OpenAPI documentation via GitHub Pages.

---

## Overview

Your IRCTC backend has Swagger documentation that can be hosted on GitHub Pages automatically through GitHub Actions.

**Current Swagger Endpoints:**
- Swagger UI: `http://localhost:8082/swagger-ui.html`
- API Docs: `http://localhost:8082/api-docs` or `/v3/api-docs`
- API Docs JSON: `http://localhost:8082/v3/api-docs`

---

## Automated Hosting via GitHub Pages

### Workflow: `.github/workflows/swagger-docs.yml`

This workflow:
1. ✅ Builds your application
2. ✅ Extracts OpenAPI/Swagger specification
3. ✅ Generates Swagger UI HTML
4. ✅ Deploys to GitHub Pages
5. ✅ Auto-updates on code changes

### Setup GitHub Pages

1. **Enable GitHub Pages:**
   - Go to: Settings → Pages
   - Source: GitHub Actions
   - Save

2. **Workflow Runs Automatically:**
   - On push to `main` or `develop`
   - On release creation
   - Manual trigger available

3. **Access Swagger Docs:**
   - URL: `https://[USERNAME].github.io/[REPO]/`
   - Example: `https://yashTariyal.github.io/irctc/`

---

## Manual Setup

### Option 1: Extract Swagger JSON Locally

```bash
# Start your application
./mvnw spring-boot:run

# In another terminal, download Swagger JSON
curl http://localhost:8082/v3/api-docs > openapi.json

# Or use Swagger UI
open http://localhost:8082/swagger-ui.html
```

### Option 2: Generate Static Swagger UI

```bash
# Download Swagger UI
mkdir swagger-docs
cd swagger-docs

# Download OpenAPI spec
curl http://localhost:8082/v3/api-docs > openapi.json

# Create index.html (see workflow file for template)
# Host on any static hosting service
```

---

## Workflow Details

### Triggers
- **Push** to `main` or `develop` branches
- **Release** creation
- **Manual** trigger (workflow_dispatch)

### Process
1. Checkout code
2. Setup JDK 21
3. Build application
4. Start application temporarily
5. Extract OpenAPI spec from running app
6. Generate Swagger UI HTML
7. Deploy to GitHub Pages

### Generated Files
- `swagger-docs/openapi.json` - OpenAPI 3.0 specification
- `swagger-docs/index.html` - Swagger UI interface

---

## Access Your Hosted Swagger

After deployment:

1. **Check Actions Tab:**
   - Go to Actions
   - Find "Generate and Deploy Swagger Docs" workflow
   - Check for "deploy" job completion

2. **Access Pages:**
   - Go to: Settings → Pages
   - View published site URL

3. **Direct Access:**
   ```
   https://[USERNAME].github.io/[REPO-NAME]/
   ```

---

## Customization

### Update Swagger Config

Edit `src/main/java/com/irctc_backend/irctc/config/SwaggerConfig.java`:

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Your Custom Title")
            .version("2.0.0")
            .description("Your description"));
}
```

### Update Application Properties

Edit `src/main/resources/application.properties`:

```properties
# Swagger UI Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
```

Changes will be reflected in next deployment.

---

## Troubleshooting

### Swagger Docs Not Updating

1. Check workflow ran successfully
2. Verify GitHub Pages is enabled
3. Wait a few minutes for deployment
4. Clear browser cache

### Application Fails to Start in Workflow

1. Check application.properties configuration
2. Verify database/Kafka not required for startup
3. Review workflow logs
4. May need to mock external dependencies

### OpenAPI Spec Not Found

The workflow tries multiple endpoints:
- `/v3/api-docs` (OpenAPI 3.0)
- `/api-docs` (Alternative path)
- `/v2/api-docs` (Swagger 2.0)

If all fail, check:
- Swagger dependency in pom.xml
- SwaggerConfig bean exists
- Application port configuration

---

## Alternative: SwaggerHub

For external hosting:

1. **Sign up**: https://swagger.io/tools/swaggerhub/
2. **Upload**: OpenAPI JSON
3. **Host**: Public or private documentation
4. **Features**: Versioning, team collaboration

---

## Current Configuration

**Swagger Dependency:**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.1.0</version>
</dependency>
```

**Swagger Config:**
- Location: `src/main/java/com/irctc_backend/irctc/config/SwaggerConfig.java`
- Groups: User, Train, Booking, Passenger, System APIs
- Security: JWT Bearer token support

**Endpoints:**
- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`
- API Docs: `/api-docs`

---

## Next Steps

1. ✅ Enable GitHub Pages in repository settings
2. ✅ Push workflow file to trigger first deployment
3. ✅ Access hosted Swagger docs
4. ✅ Share URL with team/stakeholders

---

**Status**: ✅ Ready to Deploy  
**Last Updated**: 2024-12-28

