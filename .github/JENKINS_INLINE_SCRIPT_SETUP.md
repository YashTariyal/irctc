# Jenkins Inline Script Setup Guide

## Problem

When using an **inline pipeline script** (not "Pipeline script from SCM"), the workspace is empty because:
- Code isn't automatically checked out
- `./mvnw` file doesn't exist
- Build fails with "No such file or directory"

---

## Quick Fix: Use "Pipeline script from SCM" (Recommended)

**This is the easiest solution - no code changes needed!**

### Setup Steps:

1. **Jenkins Job** → **Configure**
2. **Pipeline** → **Definition:** Select **"Pipeline script from SCM"**
3. **SCM:** Git
4. **Repository URL:** Your GitHub repo
   ```
   https://github.com/YOUR_USERNAME/irctc.git
   ```
5. **Branches:** `*/main`
6. **Script Path:** `Jenkinsfile`
7. **Save**

**Done!** Code will be checked out automatically.

---

## Alternative: Configure Inline Script

If you must use inline script, you need to provide repository information.

### Option 1: Set Environment Variables

Add environment variables in Jenkins job:

1. **Jenkins Job** → **Configure** → **Pipeline**
2. **Scroll down** to find environment variables section
3. Add:
   - `GIT_URL` = `https://github.com/YOUR_USERNAME/irctc.git`
   - `GIT_BRANCH` = `main`
4. **Save**

### Option 2: Update Jenkinsfile Directly

Edit the Jenkinsfile and replace the default URL:

```groovy
def repoUrl = 'https://github.com/YOUR_USERNAME/irctc.git'
def branch = 'main'
```

Then use it in the checkout stage.

---

## Manual Clone (Last Resort)

If you need to manually clone, add this to your inline script:

```groovy
stage('Checkout') {
    steps {
        sh '''
            # Remove existing workspace
            rm -rf *
            rm -rf .*
            
            # Clone your repository
            git clone https://github.com/YOUR_USERNAME/irctc.git .
            cd .
            git checkout main
        '''
    }
}
```

**Replace `YOUR_USERNAME` with your actual GitHub username.**

---

## Recommended: Pipeline script from SCM

**Why this is better:**
- ✅ Automatic checkout
- ✅ Always uses latest code
- ✅ No manual configuration
- ✅ Works with all Jenkins features
- ✅ Version controlled

**Setup takes 2 minutes and solves all checkout issues!**

---

## Verification

After switching to "Pipeline script from SCM":

1. ✅ Build should checkout code automatically
2. ✅ `./mvnw` file should exist
3. ✅ Build should proceed to compile stage
4. ✅ Git commit info should be shown

---

**Status**: ✅ Use "Pipeline script from SCM" for best results!

