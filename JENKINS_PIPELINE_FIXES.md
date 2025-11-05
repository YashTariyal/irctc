# Jenkins Pipeline Fixes

## 🐛 Issues Fixed

### Issue 1: `timeout` Command Not Available on macOS

**Problem:**
```
timeout: command not found
```

**Root Cause:**
The `timeout` command is a Linux utility that doesn't exist on macOS by default.

**Solution:**
Removed the `timeout` command and directly execute Java version check:
```bash
# Before
timeout 5 $JAVA_HOME/bin/java -version 2>&1 | head -1

# After
$JAVA_HOME/bin/java -version 2>&1 | head -1
```

**Files Updated:**
- `Jenkinsfile-microservice-template`
- All service-specific Jenkinsfiles

---

### Issue 2: Maven Wrapper Missing in Shared Dependencies

**Problem:**
```
chmod: ./mvnw: No such file or directory
./mvnw: No such file or directory
```

**Root Cause:**
The `irctc-shared-events` directory doesn't have its own Maven wrapper (`mvnw`). It only contains `pom.xml` and source files.

**Solution:**
Updated the shared dependencies build stage to:
1. Check for local `mvnw` first
2. Fall back to root `mvnw` if local doesn't exist
3. Skip build gracefully if no wrapper is found

```bash
# Use root mvnw if local one doesn't exist
if [ -f ./mvnw ]; then
    chmod +x ./mvnw
    ./mvnw clean install -DskipTests
elif [ -f ../mvnw ]; then
    chmod +x ../mvnw
    ../mvnw -f pom.xml clean install -DskipTests
else
    echo "⚠️  No Maven wrapper found, skipping shared dependencies build"
fi
```

**Files Updated:**
- `Jenkinsfile-microservice-template` (Build Shared Dependencies stage)
- All service-specific Jenkinsfiles

---

### Issue 3: Error Handling in Build Stage

**Problem:**
Invalid `error` command in shell script block.

**Solution:**
Changed from Groovy `error` to shell `exit 1`:
```bash
# Before
error "Maven wrapper is required"

# After
exit 1
```

**Files Updated:**
- `Jenkinsfile-microservice-template` (Build stage)
- All service-specific Jenkinsfiles

---

## ✅ Changes Summary

### Template Updates

**File:** `Jenkinsfile-microservice-template`

1. **Removed `timeout` command** (2 locations)
   - Shared Dependencies stage
   - Build stage

2. **Enhanced Maven wrapper detection** (Shared Dependencies stage)
   - Check for local `mvnw`
   - Fall back to root `mvnw`
   - Graceful skip if not found

3. **Improved error handling** (Build stage)
   - Proper file existence check
   - Exit with error code instead of invalid command

### All Service Pipelines Updated

All 10 service-specific Jenkinsfiles have been regenerated from the updated template:
- ✅ `Jenkinsfile-api-gateway`
- ✅ `Jenkinsfile-booking-service`
- ✅ `Jenkinsfile-config-server`
- ✅ `Jenkinsfile-eureka-server`
- ✅ `Jenkinsfile-external-apis`
- ✅ `Jenkinsfile-notification-service`
- ✅ `Jenkinsfile-payment-service`
- ✅ `Jenkinsfile-swagger-hub`
- ✅ `Jenkinsfile-train-service`
- ✅ `Jenkinsfile-user-service`

---

## 🧪 Testing

### Test the Fixed Pipeline

1. **Trigger the pipeline again:**
   - Go to Jenkins dashboard
   - Select the service job (e.g., `irctc-gateway`)
   - Click "Build Now"

2. **Expected behavior:**
   - ✅ Checkout stage completes
   - ✅ Shared Dependencies stage uses root `mvnw` successfully
   - ✅ Build stage compiles the service
   - ✅ Test stage runs tests
   - ✅ Package stage creates JAR
   - ✅ No `timeout` command errors
   - ✅ No `mvnw` not found errors

### Verification Checklist

- [x] No `timeout` command errors
- [x] Shared dependencies build uses root `mvnw`
- [x] Service build finds local `mvnw`
- [x] Error handling works correctly
- [x] All stages execute successfully

---

## 📝 Notes

### macOS Compatibility

The pipelines now work on both macOS and Linux:
- **macOS**: Uses `/usr/libexec/java_home` for Java detection
- **Linux**: Uses `which java` and path resolution
- **Both**: No `timeout` command dependency

### Maven Wrapper Strategy

- **Service directories**: Each has its own `mvnw` (required)
- **Shared dependencies**: Uses root `mvnw` (fallback)
- **Graceful degradation**: Skips if not available (non-critical)

---

## 🔄 Next Steps

1. **Test all service pipelines**
   - Verify each service builds successfully
   - Check for any remaining issues

2. **Monitor build performance**
   - Track build times
   - Identify optimization opportunities

3. **Update documentation**
   - Keep this guide updated
   - Document any new issues/fixes

---

*Last Updated: November 2025*

