# Jenkins Tools Configuration Guide

## Problem

Jenkins error: `Tool type "maven" does not have an install of "Maven-3.9" configured`

This happens when Jenkins doesn't have the tools configured that the pipeline expects.

---

## Solution Options

### Option 1: Use Maven/Gradle Wrapper (Recommended - No Setup Needed)

The updated Jenkinsfiles now use Maven/Gradle wrappers, so **no tool configuration is needed**!

**Just use the updated Jenkinsfile** - it will work automatically.

---

### Option 2: Configure Tools in Jenkins

If you want to use Jenkins tools instead of wrappers:

#### Configure Maven

1. **Jenkins** → **Manage Jenkins** → **Tools**
2. Click **Add Maven**
3. **Name:** `Maven-3.9`
4. **Version:** Select version (e.g., `3.9.9` or latest)
5. Click **Save**

#### Configure JDK

1. **Jenkins** → **Manage Jenkins** → **Tools**
2. Click **Add JDK**
3. **Name:** `JDK-21`
4. **Installation:** 
   - Option A: Auto-install (select version like `21`)
   - Option B: Specify path: `/usr/lib/jvm/java-21-openjdk`
5. Click **Save**

#### Configure Gradle

1. **Jenkins** → **Manage Jenkins** → **Tools**
2. Click **Add Gradle**
3. **Name:** `Gradle-8.10`
4. **Version:** `8.10` (or latest)
5. Click **Save**

---

### Option 3: Use Updated Jenkinsfile (No Tools Needed)

The updated `Jenkinsfile` and `Jenkinsfile-gradle` now:
- ✅ Use Maven wrapper (`./mvnw`) - no Maven installation needed
- ✅ Use Gradle wrapper (`./gradlew`) - no Gradle installation needed
- ✅ Use system Java - no JDK configuration needed
- ✅ Work out of the box!

**Just use the updated files** - they handle tool configuration automatically.

---

## Quick Fix: Update Your Jenkins Job

### For Maven Pipeline:

1. **Open your Jenkins job**
2. **Configure** → **Pipeline** section
3. **Replace** with updated `Jenkinsfile` content
4. Or **Script Path:** `Jenkinsfile` (uses wrapper automatically)
5. **Save**

### For Gradle Pipeline:

1. **Open your Jenkins job**
2. **Configure** → **Pipeline** section
3. **Replace** with updated `Jenkinsfile-gradle` content
4. Or **Script Path:** `Jenkinsfile-gradle` (uses wrapper automatically)
5. **Save**

---

## Minimal Jenkinsfile (No Tools)

If you just want a simple pipeline that works:

```groovy
pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build') {
            steps {
                sh '''
                    chmod +x ./mvnw
                    ./mvnw clean package -DskipTests
                '''
            }
        }
    }
    post {
        success {
            archiveArtifacts 'target/*.jar'
        }
    }
}
```

Or for Gradle:

```groovy
pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build') {
            steps {
                sh '''
                    chmod +x ./gradlew
                    ./gradlew clean build
                '''
            }
        }
    }
    post {
        success {
            archiveArtifacts 'build/libs/*.jar'
        }
    }
}
```

---

## Verification

After updating:

1. **Save** Jenkins job configuration
2. **Build Now**
3. Should work without tool configuration errors!

---

**Status**: ✅ Fixed - No tool configuration needed!

