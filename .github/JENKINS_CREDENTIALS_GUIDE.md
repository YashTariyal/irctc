# How to Get Jenkins Credentials for GitHub Secrets

## Overview

You need three values from your Jenkins server. These are **your specific credentials** - I cannot provide them, but here's how to get each one:

---

## Step-by-Step: Getting Each Credential

### 1. JENKINS_URL

**What it is:** The URL of your Jenkins server

**How to find it:**
1. Open your Jenkins server in a browser
2. Look at the address bar
3. Copy the full URL

**Examples:**
- `http://localhost:8080` (local Jenkins)
- `https://jenkins.example.com` (remote server)
- `http://192.168.1.100:8080` (local network)

**Format:** Must include `http://` or `https://`

---

### 2. JENKINS_USER

**What it is:** Your Jenkins username (the one you use to log in)

**How to find it:**
1. Log in to Jenkins
2. Look at the top-right corner - your username is displayed there
3. Or check your login credentials

**Examples:**
- `admin`
- `jenkins-user`
- `your-username`

**Note:** This is the username you use to log into Jenkins web interface

---

### 3. JENKINS_TOKEN (API Token)

**What it is:** A secure API token for Jenkins (NOT your password)

**How to create it:**
1. **Log in to Jenkins**
2. **Click your username** (top-right corner)
3. **Click "Configure"** (left sidebar)
4. **Scroll down** to "API Token" section
5. **Click "Add new Token"** or "Generate" button
6. **Enter a description** (e.g., "GitHub Actions")
7. **Click "Generate"**
8. **COPY THE TOKEN IMMEDIATELY** (you'll only see it once!)

**Important:**
- ⚠️ Copy the token right away - you won't see it again
- This is different from your password
- The token looks like: `11a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a` (long random string)

---

## Adding to GitHub Secrets

Once you have all three values:

1. **Go to your GitHub repository**
2. **Settings** → **Secrets and variables** → **Actions**
3. **Click "New repository secret"**

### Secret 1: JENKINS_URL
- **Name:** `JENKINS_URL`
- **Value:** Your Jenkins server URL (from Step 1)
- **Example:** `https://jenkins.example.com`

### Secret 2: JENKINS_USER
- **Name:** `JENKINS_USER`
- **Value:** Your Jenkins username (from Step 2)
- **Example:** `admin`

### Secret 3: JENKINS_TOKEN
- **Name:** `JENKINS_TOKEN`
- **Value:** Your Jenkins API token (from Step 3)
- **Example:** `11a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a`

---

## If You Don't Have Jenkins

**Option 1: Skip Jenkins Integration**
- The workflow will skip automatically
- No errors, no action needed
- GitHub Actions still works normally

**Option 2: Install Jenkins Locally**
- Download: https://www.jenkins.io/download/
- Run: `java -jar jenkins.war`
- Access: http://localhost:8080
- Follow setup wizard

**Option 3: Use Cloud Jenkins**
- Jenkins on AWS/Google Cloud/Azure
- Or use Jenkins cloud services

---

## Example Values (Format Only)

**⚠️ DO NOT USE THESE - These are examples of format only!**

```
JENKINS_URL: https://jenkins.example.com
JENKINS_USER: admin
JENKINS_TOKEN: 11a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a
```

**You must replace with YOUR actual values!**

---

## Verification

After adding secrets:

1. Go to **Actions** → **Trigger Jenkins Pipeline**
2. Click **Run workflow**
3. Should see: "✅ Jenkins configuration found"
4. Jenkins build should trigger

---

## Troubleshooting

### "Jenkins URL not accessible"
- Check Jenkins server is running
- Verify URL is correct (with http/https)
- Check firewall/network settings

### "Authentication failed"
- Verify username is correct
- Regenerate API token if needed
- Check token hasn't expired

### "Job not found"
- Verify Jenkins job name matches
- Default job name: `irctc-monolith-build`
- Or specify custom name in workflow input

---

**Remember:** These values are specific to YOUR Jenkins server. I cannot provide them - you must get them from your Jenkins instance!

