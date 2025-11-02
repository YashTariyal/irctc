# Gradle Jenkins Pipeline Guide

Complete guide for using Gradle in Jenkins pipelines.

---

## Quick Start

### Option 1: Use Jenkinsfile-gradle (Recommended)

The `Jenkinsfile-gradle` file is ready to use in Jenkins:

```groovy
// Copy Jenkinsfile-gradle to Jenkinsfile
cp Jenkinsfile-gradle Jenkinsfile
```

**Features:**
- ✅ Complete Gradle pipeline
- ✅ Build, test, package stages
- ✅ Artifact archiving
- ✅ GitHub Packages publishing
- ✅ Build summaries

### Option 2: Use Gradle Script in Pipeline

**Direct Gradle commands in Jenkins pipeline:**

```groovy
stage('Build') {
    steps {
        sh './gradlew clean build -x test'
    }
}

stage('Test') {
    steps {
        sh './gradlew test'
    }
}

stage('Package') {
    steps {
        sh './gradlew bootJar'
    }
}
```

---

## Jenkins Configuration

### Step 1: Install Gradle Plugin (if needed)

1. Jenkins → Manage Jenkins → Plugins
2. Search: "Gradle Plugin"
3. Install: "Gradle Plugin"

### Step 2: Configure Gradle Tool

1. Jenkins → Manage Jenkins → Tools
2. Click "Add Gradle"
3. Name: `Gradle-8.10`
4. Version: `8.10` (or latest)
5. Click "Save"

### Step 3: Create/Update Jenkins Job

1. **New Item** → **Pipeline**
2. **Name:** `irctc-monolith-gradle`
3. **Pipeline** → **Definition:** Pipeline script from SCM
4. **SCM:** Git
5. **Repository:** Your GitHub repo
6. **Branch:** `*/main`
7. **Script Path:** `Jenkinsfile-gradle`
8. **Save**

---

## Gradle Commands for Jenkins

### Build Commands

```bash
# Clean and build (skip tests)
./gradlew clean build -x test

# Full build with tests
./gradlew clean build

# Only compile
./gradlew clean compileJava

# Create JAR
./gradlew bootJar

# Run tests
./gradlew test

# Run specific test
./gradlew test --tests "com.irctc_backend.irctc.service.*Test"
```

### Publish Commands

```bash
# Publish to GitHub Packages
./gradlew publish \
  -Pgithub.owner=YOUR_USERNAME \
  -Pgithub.repo=irctc \
  -Pgpr.user=YOUR_USERNAME \
  -Pgpr.token=YOUR_TOKEN
```

---

## Complete Jenkins Pipeline Script

Use this in Jenkins "Pipeline script" directly:

```groovy
pipeline {
    agent any
    
    tools {
        gradle 'Gradle-8.10'
        jdk 'JDK-21'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh './gradlew clean compileJava'
            }
        }
        
        stage('Test') {
            steps {
                sh './gradlew test'
            }
            post {
                always {
                    junit 'build/test-results/test/**/*.xml'
                }
            }
        }
        
        stage('Package') {
            steps {
                sh './gradlew bootJar'
            }
            post {
                success {
                    archiveArtifacts 'build/libs/*.jar'
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
    }
}
```

---

## Standalone Gradle Script

**For direct execution in Jenkins:**

```groovy
// Save as: build.gradle.kts (Kotlin DSL) or build.gradle (Groovy)
// This is already created in your project root

// To use in Jenkins pipeline:
pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh './gradlew clean build'
            }
        }
    }
}
```

---

## Gradle vs Maven

### Current Project Status

- **Maven**: `pom.xml` - Already configured ✅
- **Gradle**: `build.gradle` - Now available ✅

**You can use either:**

### Use Maven (Current):
```groovy
sh './mvnw clean package'
```

### Use Gradle (New):
```groovy
sh './gradlew clean build'
```

---

## Jenkins Job Setup

### For Gradle Pipeline:

1. **Job Name:** `irctc-monolith-gradle`
2. **Type:** Pipeline
3. **Script:** From SCM
4. **File:** `Jenkinsfile-gradle`

**Or inline script:**

```groovy
pipeline {
    agent any
    tools {
        gradle 'Gradle-8.10'
        jdk 'JDK-21'
    }
    stages {
        stage('Build') {
            steps {
                checkout scm
                sh './gradlew clean build'
            }
        }
    }
}
```

---

## Testing Locally

### Before using in Jenkins:

```bash
# Make wrapper executable
chmod +x ./gradlew

# Test build
./gradlew clean build

# Test package
./gradlew bootJar

# Check generated JAR
ls -lh build/libs/*.jar
```

---

## GitHub Actions with Gradle

The workflow `.github/workflows/gradle-pipeline.yml` is ready:

- ✅ Uses Gradle wrapper
- ✅ Caches dependencies
- ✅ Builds and tests
- ✅ Uploads artifacts
- ✅ Generates reports

**Trigger:** Push to main/develop or manual dispatch

---

## Files Created

1. **build.gradle** - Main Gradle build file
2. **gradle.properties** - Gradle configuration
3. **gradlew** - Gradle wrapper script
4. **gradle/wrapper/** - Gradle wrapper files
5. **Jenkinsfile-gradle** - Jenkins pipeline for Gradle
6. **.github/workflows/gradle-pipeline.yml** - GitHub Actions workflow

---

## Quick Reference

### Build:
```bash
./gradlew clean build
```

### Test:
```bash
./gradlew test
```

### Package:
```bash
./gradlew bootJar
```

### Publish:
```bash
./gradlew publish
```

### All in one:
```bash
./gradlew clean build test bootJar
```

---

**Status**: ✅ Ready to Use  
**Last Updated**: 2024-12-28

