# Jenkins + GitHub Actions Integration Guide

This guide explains how to integrate Jenkins pipelines with GitHub Actions workflows.

---

## Overview

You can use GitHub Actions to trigger Jenkins pipelines in several ways:

1. **GitHub Actions → Jenkins** - Trigger Jenkins from GitHub Actions
2. **Webhook Integration** - Jenkins listens to GitHub webhooks
3. **Manual Trigger** - Trigger Jenkins jobs from GitHub Actions workflow_dispatch

---

## Setup Options

### Option 1: GitHub Actions Triggers Jenkins (Recommended)

Use the workflow: `.github/workflows/jenkins-trigger.yml`

**Prerequisites:**
1. Jenkins server running and accessible
2. Jenkins user with API token
3. GitHub Secrets configured

**Required GitHub Secrets:**
```
JENKINS_URL=https://your-jenkins-server.com
JENKINS_USER=your-username
JENKINS_TOKEN=your-api-token
```

**Usage:**
1. Go to Actions → "Trigger Jenkins Pipeline"
2. Click "Run workflow"
3. Enter Jenkins job name (default: `irctc-monolith-build`)
4. Select branch
5. Workflow will trigger Jenkins build

### Option 2: Jenkins Webhook (Traditional)

Jenkins listens to GitHub webhooks automatically.

**Jenkins Configuration:**
1. Install "GitHub" plugin in Jenkins
2. Configure job:
   - Source Code Management → Git
   - Build Triggers → "GitHub hook trigger for GITScm polling"
3. Add webhook in GitHub:
   - Settings → Webhooks → Add webhook
   - Payload URL: `https://your-jenkins.com/github-webhook/`
   - Content type: `application/json`
   - Events: Push, Pull request

---

## Jenkins Pipeline Example

Create `Jenkinsfile` in repository root:

```groovy
pipeline {
    agent any
    
    tools {
        maven 'Maven-3.8'
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
                sh './mvnw clean package -DskipTests'
            }
        }
        
        stage('Test') {
            steps {
                sh './mvnw test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Publish') {
            when {
                anyOf {
                    branch 'main'
                    branch 'release/*'
                }
            }
            steps {
                sh './mvnw deploy'
            }
        }
    }
    
    post {
        always {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }
        success {
            echo 'Build successful!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}
```

---

## GitHub Actions Workflow for Jenkins Trigger

The workflow `.github/workflows/jenkins-trigger.yml` includes:

- ✅ Manual trigger (workflow_dispatch)
- ✅ Automatic trigger on push/PR
- ✅ Configurable Jenkins job name
- ✅ Parameter passing (branch, commit, repo)
- ✅ Build status reporting

---

## Setup Steps

### 1. Configure GitHub Secrets

Go to: Settings → Secrets and variables → Actions → New repository secret

Add:
- `JENKINS_URL` - Your Jenkins server URL
- `JENKINS_USER` - Jenkins username
- `JENKINS_TOKEN` - Jenkins API token

**Get Jenkins Token:**
1. Log in to Jenkins
2. Click your username (top right)
3. Configure → Add new token
4. Copy token

### 2. Create Jenkins Job

**Create Pipeline Job:**
1. New Item → Pipeline
2. Name: `irctc-monolith-build`
3. Pipeline → Pipeline script from SCM
4. SCM: Git
5. Repository URL: Your GitHub repo
6. Save

### 3. Test Integration

**Test from GitHub Actions:**
1. Go to Actions → "Trigger Jenkins Pipeline"
2. Run workflow manually
3. Check Jenkins dashboard for triggered build

---

## Benefits

✅ **Unified CI/CD**: Use both GitHub Actions and Jenkins
✅ **Flexibility**: Choose which tool for which job
✅ **Automation**: Auto-trigger on code changes
✅ **Manual Control**: Trigger Jenkins from GitHub UI

---

## Troubleshooting

### Jenkins Build Not Triggering

1. Check GitHub Secrets are correct
2. Verify Jenkins URL is accessible
3. Confirm Jenkins user has permissions
4. Check Jenkins job name matches
5. Review workflow logs in Actions tab

### Authentication Errors

1. Regenerate Jenkins API token
2. Verify token has build permissions
3. Check Jenkins user permissions
4. Ensure Jenkins URL includes protocol (http/https)

---

**Status**: ✅ Ready to Use  
**Last Updated**: 2024-12-28

