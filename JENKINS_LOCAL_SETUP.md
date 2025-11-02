# Jenkins Local Setup Guide

Complete step-by-step guide to install and run Jenkins on your local machine.

---

## Prerequisites

- ✅ Java 11 or higher installed
- ✅ At least 512MB RAM available
- ✅ Port 8080 available (or choose different port)

---

## Step 1: Verify Java Installation

```bash
java -version
```

**Expected output:**
```
openjdk version "21.x.x"
```

**If Java is not installed:**

### macOS (using Homebrew):
```bash
brew install openjdk@21
```

### macOS (direct download):
1. Download from: https://adoptium.net/
2. Install the package
3. Verify: `java -version`

### Linux (Ubuntu/Debian):
```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

### Windows:
1. Download JDK from: https://adoptium.net/
2. Install and set JAVA_HOME environment variable

---

## Step 2: Download Jenkins

### Option A: Using Homebrew (macOS - Easiest)

```bash
brew install jenkins-lts
```

### Option B: Download WAR File (All Platforms)

```bash
# Create directory
mkdir -p ~/jenkins
cd ~/jenkins

# Download Jenkins WAR file
curl -L https://get.jenkins.io/war-stable/latest/jenkins.war -o jenkins.war
```

**Or download manually:**
- Go to: https://www.jenkins.io/download/
- Download "Generic Java package (.war)"
- Save as `jenkins.war`

---

## Step 3: Start Jenkins

### Option A: Using Homebrew (macOS)

```bash
brew services start jenkins-lts
```

Jenkins will start automatically and run in the background.

### Option B: Run WAR File Directly

```bash
cd ~/jenkins
java -jar jenkins.war
```

**Or with custom port:**
```bash
java -jar jenkins.war --httpPort=8080
```

**Keep terminal open** - Jenkins runs in this window.

---

## Step 4: Access Jenkins Web Interface

1. **Open browser**: http://localhost:8080

2. **You'll see**: "Unlock Jenkins" screen

3. **Get initial password:**
   ```bash
   # On macOS/Linux:
   cat ~/.jenkins/secrets/initialAdminPassword
   
   # Or check the terminal output for the password
   ```

   **The password looks like:** `a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6`

4. **Copy the password** and paste it into the browser

5. **Click "Continue"**

---

## Step 5: Install Plugins

1. **Choose**: "Install suggested plugins" (recommended)
   - Or "Select plugins to install" for custom selection

2. **Wait** for plugins to install (5-10 minutes)

3. **Click "Continue"** after installation completes

---

## Step 6: Create Admin User

1. **Fill in form:**
   - Username: `admin` (or your choice)
   - Password: Create a strong password
   - Full name: Your name
   - Email: Your email

2. **Save the credentials** - you'll need these for GitHub Secrets!

3. **Click "Save and Continue"**

---

## Step 7: Configure Jenkins URL

1. **Jenkins URL** should show: `http://localhost:8080/`
2. **Click "Save and Finish"**

---

## Step 8: Jenkins is Ready!

You should see: "Jenkins is ready!"

**Click "Start using Jenkins"**

---

## Step 9: Create Your First Job (Pipeline)

1. **Click "New Item"** (or "Create a job")

2. **Enter name:** `irctc-monolith-build`

3. **Select:** "Pipeline"

4. **Click "OK"**

---

## Step 10: Configure Pipeline

1. **Scroll down** to "Pipeline" section

2. **Definition:** "Pipeline script from SCM"

3. **SCM:** Git

4. **Repository URL:** Your GitHub repository URL
   ```
   https://github.com/YOUR_USERNAME/irctc.git
   ```

5. **Branches to build:** `*/main` or `*/master`

6. **Script Path:** Leave blank (uses Jenkinsfile if present)

7. **Click "Save"**

---

## Step 11: Get Jenkins Credentials for GitHub

### Get JENKINS_URL
```
http://localhost:8080
```

### Get JENKINS_USER
- This is the username you created in Step 6
- Usually: `admin` or what you chose

### Generate JENKINS_TOKEN

1. **Click your username** (top-right corner)

2. **Click "Configure"** (left sidebar)

3. **Scroll to "API Token"** section

4. **Click "Add new Token"**

5. **Description:** `GitHub Actions`

6. **Click "Generate"**

7. **COPY THE TOKEN IMMEDIATELY!**
   - Looks like: `11a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a`
   - You'll only see it once!

---

## Step 12: Add to GitHub Secrets

1. **Go to GitHub**: https://github.com/YOUR_USERNAME/irctc
2. **Settings** → **Secrets and variables** → **Actions**
3. **New repository secret**

**Add these three secrets:**

| Secret Name | Value |
|------------|-------|
| `JENKINS_URL` | `http://localhost:8080` |
| `JENKINS_USER` | `admin` (or your username) |
| `JENKINS_TOKEN` | `11a1a1a1a1a1a1a1...` (the token from Step 11) |

---

## Step 13: Test the Integration

1. **Go to GitHub Actions** → "Trigger Jenkins Pipeline"
2. **Run workflow** manually
3. **Check Jenkins** - you should see a build triggered!
4. **Check build logs** in Jenkins dashboard

---

## Quick Start Commands (macOS)

```bash
# Install Jenkins
brew install jenkins-lts

# Start Jenkins
brew services start jenkins-lts

# Check status
brew services list | grep jenkins

# Stop Jenkins
brew services stop jenkins-lts

# Restart Jenkins
brew services restart jenkins-lts

# View logs
tail -f ~/Library/Logs/Jenkins/jenkins.log
```

---

## Quick Start Commands (Linux/Windows)

```bash
# Download Jenkins
mkdir -p ~/jenkins
cd ~/jenkins
curl -L https://get.jenkins.io/war-stable/latest/jenkins.war -o jenkins.war

# Start Jenkins
java -jar jenkins.war

# Or run in background
nohup java -jar jenkins.war > jenkins.log 2>&1 &
```

---

## Jenkins File Locations (macOS)

- **Jenkins home:** `~/.jenkins/`
- **Logs:** `~/Library/Logs/Jenkins/jenkins.log`
- **Config:** `~/.jenkins/config.xml`

---

## Common Issues

### Port 8080 Already in Use

**Change port:**
```bash
java -jar jenkins.war --httpPort=8081
```

Then access: http://localhost:8081

**Or kill process using port 8080:**
```bash
# macOS/Linux
lsof -ti:8080 | xargs kill -9

# Then restart Jenkins
```

### Jenkins Won't Start

1. **Check Java:**
   ```bash
   java -version
   ```

2. **Check port:**
   ```bash
   lsof -i :8080
   ```

3. **Check logs:**
   ```bash
   # macOS
   tail -f ~/Library/Logs/Jenkins/jenkins.log
   
   # Linux/WAR
   tail -f jenkins.log
   ```

### Can't Find Initial Password

```bash
# macOS
cat ~/.jenkins/secrets/initialAdminPassword

# Linux/WAR (check terminal output)
# Or:
cat ~/.jenkins/secrets/initialAdminPassword
```

---

## Next Steps

1. ✅ Jenkins installed and running
2. ✅ Job created
3. ✅ Credentials obtained
4. ✅ Added to GitHub Secrets
5. ✅ Test integration

**Now your GitHub Actions workflow can trigger Jenkins builds!**

---

## Useful Jenkins URLs

- **Dashboard:** http://localhost:8080
- **Manage Jenkins:** http://localhost:8080/manage
- **Configure System:** http://localhost:8080/configure
- **Plugin Manager:** http://localhost:8080/pluginManager
- **Your Job:** http://localhost:8080/job/irctc-monolith-build

---

## Stop Jenkins

### If using Homebrew:
```bash
brew services stop jenkins-lts
```

### If running WAR file:
- Press `Ctrl+C` in the terminal
- Or kill the process:
```bash
pkill -f jenkins.war
```

---

**Status**: ✅ Ready for Local Setup  
**Last Updated**: 2024-12-28

