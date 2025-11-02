# Quick Setup: Jenkins GitHub Secrets

## Problem

If you see the error: `missing jenkins config`, it means GitHub Secrets are not configured.

---

## Solution: Add GitHub Secrets

### Step 1: Get Jenkins Credentials

**Jenkins URL:**
- Your Jenkins server URL
- Example: `https://jenkins.example.com` or `http://192.168.1.100:8080`

**Jenkins Username:**
- Your Jenkins login username

**Jenkins API Token:**
1. Log in to Jenkins
2. Click your username (top right)
3. Click **Configure**
4. Scroll to **API Token** section
5. Click **Add new Token** → **Generate**
6. Copy the token (you'll only see it once!)

---

### Step 2: Add Secrets to GitHub

1. Go to your GitHub repository
2. Click **Settings** (top menu)
3. Click **Secrets and variables** → **Actions** (left sidebar)
4. Click **New repository secret**

Add these three secrets:

#### Secret 1: JENKINS_URL
- **Name**: `JENKINS_URL`
- **Value**: Your Jenkins server URL (with http:// or https://)
- Example: `https://jenkins.example.com`

#### Secret 2: JENKINS_USER
- **Name**: `JENKINS_USER`
- **Value**: Your Jenkins username
- Example: `admin` or `jenkins-user`

#### Secret 3: JENKINS_TOKEN
- **Name**: `JENKINS_TOKEN`
- **Value**: Your Jenkins API token (from Step 1)
- Example: `11a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a`

---

### Step 3: Verify

1. Go to **Actions** tab
2. Run **Trigger Jenkins Pipeline** workflow
3. Should see: "✅ Jenkins configuration found"
4. Build should trigger successfully

---

## Troubleshooting

### Still Getting "missing jenkins config"

1. **Verify secrets exist:**
   - Settings → Secrets → Actions
   - Should see: `JENKINS_URL`, `JENKINS_USER`, `JENKINS_TOKEN`

2. **Check secret names:**
   - Must be EXACTLY: `JENKINS_URL`, `JENKINS_USER`, `JENKINS_TOKEN`
   - Case-sensitive!

3. **Verify Jenkins URL:**
   - Must include protocol: `http://` or `https://`
   - Must be accessible from GitHub Actions runners

4. **Check Jenkins token:**
   - Token must have build permissions
   - Generate new token if needed

### Jenkins Build Not Triggering

1. **Verify Jenkins job exists:**
   - Job name must match (default: `irctc-monolith-build`)
   - Or specify custom job name in workflow

2. **Check Jenkins permissions:**
   - User must have permission to trigger builds
   - Check Jenkins user roles/permissions

3. **Verify Jenkins URL is accessible:**
   - Test from browser
   - Check firewall/network settings

---

## Alternative: Skip Jenkins Integration

If you don't want to use Jenkins right now:

1. The workflow will skip automatically if secrets are missing
2. Or use `skip_jenkins: true` when manually triggering
3. Your GitHub Actions build will still work normally

---

**Quick Reference:**
- Secrets location: Settings → Secrets and variables → Actions
- Jenkins token: Your username → Configure → API Token → Generate
- Workflow file: `.github/workflows/jenkins-trigger.yml`

