// Jenkinsfile for IRCTC Monolith Pipeline
// This file defines the Jenkins build pipeline

pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9'
        jdk 'JDK-21'
    }
    
    environment {
        JAVA_HOME = tool 'JDK-21'
        MAVEN_HOME = tool 'Maven-3.9'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo '🔄 Checking out code from repository...'
                checkout scm
                script {
                    def gitCommit = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
                    env.GIT_COMMIT_SHORT = gitCommit
                    echo "📦 Git commit: ${gitCommit}"
                }
            }
        }
        
        stage('Build') {
            steps {
                echo '🔨 Building application with Maven...'
                sh './mvnw clean compile -DskipTests'
            }
        }
        
        stage('Test') {
            steps {
                echo '🧪 Running tests...'
                sh './mvnw test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'target/surefire-reports/**/*', allowEmptyArchive: true
                }
            }
        }
        
        stage('Package') {
            steps {
                echo '📦 Packaging application...'
                sh './mvnw package -DskipTests'
            }
            post {
                success {
                    echo '✅ Application packaged successfully'
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }
        
        stage('Publish') {
            when {
                anyOf {
                    branch 'main'
                    branch 'release/*'
                    expression { env.GITHUB_EVENT == 'release' }
                }
            }
            steps {
                echo '📤 Publishing to GitHub Packages...'
                script {
                    // Note: This requires Maven settings.xml with GitHub credentials
                    // sh './mvnw deploy -DskipTests'
                    echo '⚠️  Deploy step requires GitHub Packages configuration'
                    echo '📋 Artifacts are archived and available for download'
                }
            }
        }
    }
    
    post {
        always {
            echo '📊 Build Summary:'
            echo "   Status: ${currentBuild.currentResult}"
            echo "   Build Number: ${env.BUILD_NUMBER}"
            echo "   Git Commit: ${env.GIT_COMMIT_SHORT ?: 'N/A'}"
            cleanWs()
        }
        success {
            echo '✅ Pipeline completed successfully!'
            emailext (
                subject: "✅ Build Successful: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "Build ${env.BUILD_NUMBER} completed successfully.",
                to: "${env.CHANGE_AUTHOR_EMAIL ?: 'admin@example.com'}"
            )
        }
        failure {
            echo '❌ Pipeline failed!'
            emailext (
                subject: "❌ Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "Build ${env.BUILD_NUMBER} failed. Check console output.",
                to: "${env.CHANGE_AUTHOR_EMAIL ?: 'admin@example.com'}"
            )
        }
        unstable {
            echo '⚠️  Pipeline completed with warnings'
        }
    }
}

