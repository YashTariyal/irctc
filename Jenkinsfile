// Jenkinsfile for IRCTC Monolith Pipeline
// This file defines the Jenkins build pipeline

pipeline {
    agent any
    
    environment {
        // Use system Java from environment or default location
        JAVA_HOME = "${env.JAVA_HOME ?: sh(script: 'echo $JAVA_HOME', returnStdout: true).trim() ?: '/usr/lib/jvm/java-21-openjdk'}"
        // Use Maven wrapper if Maven tool not configured
        PATH = "${env.PATH}:${pwd()}"
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
                echo "Java Version: ${sh(returnStdout: true, script: 'java -version 2>&1').trim()}"
                sh '''
                    chmod +x ./mvnw || true
                    ./mvnw clean compile -DskipTests
                '''
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
                sh '''
                    ./mvnw package -DskipTests
                    echo "JAR files created:"
                    ls -lh target/*.jar || echo "No JAR files found"
                '''
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
            script {
                echo '📊 Build Summary:'
                echo "   Status: ${currentBuild.currentResult}"
                echo "   Build Number: ${env.BUILD_NUMBER}"
                echo "   Git Commit: ${env.GIT_COMMIT_SHORT ?: 'N/A'}"
                // Clean workspace if inside node context
                try {
                    cleanWs()
                } catch (Exception e) {
                    echo "⚠️  Workspace cleanup skipped: ${e.message}"
                }
            }
        }
        success {
            echo '✅ Pipeline completed successfully!'
            // Email disabled - configure SMTP in Jenkins if needed
            // emailext (
            //     subject: "✅ Build Successful: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            //     body: "Build ${env.BUILD_NUMBER} completed successfully.",
            //     to: "${env.CHANGE_AUTHOR_EMAIL ?: 'admin@example.com'}"
            // )
        }
        failure {
            echo '❌ Pipeline failed!'
            echo "Check console output for details."
            // Email disabled - configure SMTP in Jenkins if needed
            // emailext (
            //     subject: "❌ Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            //     body: "Build ${env.BUILD_NUMBER} failed. Check console output.",
            //     to: "${env.CHANGE_AUTHOR_EMAIL ?: 'admin@example.com'}"
            // )
        }
        unstable {
            echo '⚠️  Pipeline completed with warnings'
        }
    }
}

