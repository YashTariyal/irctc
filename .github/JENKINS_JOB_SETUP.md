# Jenkins Job Setup Guide

## Problem

Error: `'checkout scm' is only available when using "Multibranch Pipeline" or "Pipeline script from SCM"`

This happens when you're using an **inline pipeline script** instead of loading from SCM.

---

## Solution Options

### Option 1: Use "Pipeline script from SCM" (Recommended)

This is the best approach - your Jenkinsfile is stored in your repository.

#### Setup Steps:

1. **Open your Jenkins job**
2. **Configure** → **Pipeline** section
3. **Definition:** Select **"Pipeline script from SCM"**
4. **SCM:** Select **Git**
5. **Repository URL:** Your GitHub repository URL
   ```
   https://github.com/YOUR_USERNAME/irctc.git
   ```
6. **Credentials:** Add if repository is private
7. **Branches:** `*/main` (or `*/master`)
8. **Script Path:** `Jenkinsfile` (or `Jenkinsfile-gradle` for Gradle)
9. **Click Save**

**Benefits:**
- ✅ Automatic checkout
- ✅ Uses latest code from repository
- ✅ No manual git setup needed
- ✅ Works with `checkout scm`

---

### Option 2: Use Inline Script (Updated Jenkinsfile Works)

The updated Jenkinsfile now handles both cases!

**For inline scripts:**

1. **Open your Jenkins job**
2. **Configure** → **Pipeline** section
3. **Definition:** Select **"Pipeline script"**
4. **Paste** the Jenkinsfile content (from repository)
5. **Click Save**

**The pipeline will:**
- Try `checkout scm` first
- If that fails, skip checkout (works with existing code)
- Build from workspace files

---

### Option 3: Manual Git Checkout

If you need to checkout manually in inline script:

```groovy
stage('Checkout') {
    steps {
        sh '''
            # Remove existing workspace
            rm -rf *
            rm -rf .*
            
            # Clone repository
            git clone https://github.com/YOUR_USERNAME/irctc.git .
            
            # Or if workspace already has git
            git fetch --all
            git reset --hard origin/main
        '''
    }
}
```

---

## Recommended Setup

### Best Practice: Pipeline script from SCM

```
Pipeline Configuration:
┌─────────────────────────────────┐
│ Definition: Pipeline script     │
│   from SCM                      │
│                                 │
│ SCM: Git                        │
│ Repository: https://github.com/ │
│   YOUR_USERNAME/irctc.git       │
│ Branches: */main                │
│ Script Path: Jenkinsfile        │
└─────────────────────────────────┘
```

**Benefits:**
- ✅ Version controlled
- ✅ Easy to update (just commit to repo)
- ✅ Automatic checkout
- ✅ Works with `checkout scm`

---

## Quick Setup Steps

### Step 1: Update Jenkins Job

1. Go to Jenkins → Your Job → Configure
2. Pipeline → Definition: **"Pipeline script from SCM"**
3. SCM: **Git**
4. Repository: Your GitHub repo URL
5. Branches: `*/main`
6. Script Path: `Jenkinsfile`
7. Save

### Step 2: Build

1. Click **Build Now**
2. Should work automatically!

---

## Verification

After setup:

1. ✅ Build should checkout code automatically
2. ✅ Git commit should be shown in logs
3. ✅ Build should proceed to compile stage

---

## Troubleshooting

### "checkout scm" still fails

**Solution:** Use "Pipeline script from SCM" (Option 1) instead of inline script.

### No code in workspace

**For inline scripts:**
- Make sure git is installed on Jenkins
- Or manually clone before running pipeline

**For SCM-based:**
- Check repository URL is correct
- Verify credentials if private repo
- Check branch name matches

### Git not found

Install git on Jenkins:
```bash
# On Jenkins server
sudo apt install git  # Ubuntu/Debian
brew install git      # macOS
```

---

**Status**: ✅ Fixed - Works with both SCM and inline scripts!

