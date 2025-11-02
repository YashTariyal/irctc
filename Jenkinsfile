// Jenkinsfile for IRCTC Monolith Pipeline
// This file defines the Jenkins build pipeline

pipeline {
    agent any
    
    environment {
        // Use Maven wrapper if Maven tool not configured
        PATH = "${env.PATH}:${pwd()}"
        // Don't set JAVA_HOME here - let the build stage detect it properly
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo '🔄 Checking out code from repository...'
                script {
                    try {
                        // Try using checkout scm (works if Pipeline script from SCM)
                        checkout scm
                        echo "✅ Checked out using SCM"
                    } catch (Exception e) {
                        echo "⚠️  SCM checkout not available, using manual checkout"
                        // Manual git checkout (works for inline scripts)
                        script {
                            def repoUrl = env.GIT_URL ?: 'https://github.com/YOUR_USERNAME/irctc.git'
                            def branch = env.GIT_BRANCH ?: 'main'
                            
                            sh """
                                if [ -d .git ]; then
                                    echo "Git repository already exists, pulling latest..."
                                    git fetch --all || true
                                    git reset --hard origin/${branch} || git reset --hard origin/main || git reset --hard origin/master || true
                                else
                                    echo "No git repository found, cloning..."
                                    echo "Repository: ${repoUrl}"
                                    echo "Branch: ${branch}"
                                    echo ""
                                    echo "⚠️  Using default repository URL. For your repo, set GIT_URL environment variable"
                                    echo "   or configure 'Pipeline script from SCM' (recommended)"
                                    echo ""
                                    # Try to clone if git is available
                                    if command -v git >/dev/null 2>&1; then
                                        git clone --depth 1 --branch ${branch} ${repoUrl} . || \
                                        git clone --depth 1 ${repoUrl} . || \
                                        echo "⚠️  Could not clone repository. Please configure repository URL or use 'Pipeline script from SCM'"
                                    else
                                        echo "⚠️  Git not found. Please install git or use 'Pipeline script from SCM'"
                                    fi
                                fi
                            """
                        }
                    }
                    
                    // Get git commit info if available
                    try {
                        def gitCommit = sh(returnStdout: true, script: 'git rev-parse --short HEAD 2>/dev/null || echo "N/A"').trim()
                        env.GIT_COMMIT_SHORT = gitCommit
                        echo "📦 Git commit: ${gitCommit}"
                    } catch (Exception e) {
                        env.GIT_COMMIT_SHORT = "N/A"
                        echo "📦 Git commit: N/A (not a git repository or git not available)"
                    }
                }
            }
        }
        
        stage('Build') {
            steps {
                echo '🔨 Building application with Maven...'
                script {
                    // Find Java location and set JAVA_HOME
                    def javaHome = sh(script: '''
                        if [ -n "$JAVA_HOME" ]; then
                            echo "$JAVA_HOME"
                        else
                            # Try to find Java
                            java_path=$(which java 2>/dev/null)
                            if [ -n "$java_path" ]; then
                                java_dir=$(dirname "$java_path")
                                dirname "$java_dir"
                            else
                                # Default locations
                                if [ -d "/usr/lib/jvm/java-21-openjdk" ]; then
                                    echo "/usr/lib/jvm/java-21-openjdk"
                                elif [ -d "/Library/Java/JavaVirtualMachines" ]; then
                                    # macOS - find latest JDK
                                    ls -d /Library/Java/JavaVirtualMachines/*/Contents/Home 2>/dev/null | head -1 || echo ""
                                else
                                    echo ""
                                fi
                            fi
                        fi
                    ''', returnStdout: true).trim()
                    
                    if (javaHome) {
                        env.JAVA_HOME = javaHome
                        echo "✅ JAVA_HOME set to: ${javaHome}"
                    } else {
                        echo "⚠️  Could not detect JAVA_HOME, using system Java"
                    }
                    
                    echo "Java Version: ${sh(returnStdout: true, script: 'java -version 2>&1').trim()}"
                }
                sh '''
                    # Find and set JAVA_HOME properly
                    # First, check if existing JAVA_HOME is valid
                    if [ -n "$JAVA_HOME" ] && [ -d "$JAVA_HOME" ] && [ -f "$JAVA_HOME/bin/java" ]; then
                        echo "Using existing JAVA_HOME: $JAVA_HOME"
                        export JAVA_HOME
                    else
                        # Find Java using which
                        JAVA_PATH=$(which java 2>/dev/null)
                        if [ -n "$JAVA_PATH" ]; then
                            # Resolve symlinks and get real path
                            JAVA_PATH=$(readlink -f "$JAVA_PATH" 2>/dev/null || echo "$JAVA_PATH")
                            # For macOS, use realpath or calculate manually
                            if [[ "$OSTYPE" == "darwin"* ]]; then
                                # macOS: /usr/bin/java -> /Library/Java/JavaVirtualMachines/.../Contents/Home/bin/java
                                JAVA_DIR=$(dirname "$JAVA_PATH")
                                # Check if we're in a bin directory
                                if [[ "$JAVA_DIR" == */bin ]]; then
                                    export JAVA_HOME=$(dirname "$JAVA_DIR")
                                else
                                    # Try to go up to find Home
                                    export JAVA_HOME=$(dirname "$JAVA_DIR")
                                fi
                            else
                                # Linux: usually /usr/bin/java -> /usr/lib/jvm/java-21-openjdk/bin/java
                                JAVA_DIR=$(dirname "$JAVA_PATH")
                                export JAVA_HOME=$(dirname "$JAVA_DIR")
                            fi
                            
                            # Verify JAVA_HOME exists
                            if [ -n "$JAVA_HOME" ] && [ -d "$JAVA_HOME" ] && [ -f "$JAVA_HOME/bin/java" ]; then
                                echo "✅ Detected JAVA_HOME: $JAVA_HOME"
                                export JAVA_HOME
                            else
                                # Try macOS default locations
                                if [[ "$OSTYPE" == "darwin"* ]]; then
                                    MACOS_JAVA=$(/usr/libexec/java_home 2>/dev/null || ls -d /Library/Java/JavaVirtualMachines/*/Contents/Home 2>/dev/null | head -1)
                                    if [ -n "$MACOS_JAVA" ] && [ -d "$MACOS_JAVA" ]; then
                                        export JAVA_HOME="$MACOS_JAVA"
                                        echo "✅ Using macOS Java: $JAVA_HOME"
                                    else
                                        echo "⚠️  Could not find valid JAVA_HOME"
                                        unset JAVA_HOME
                                    fi
                                else
                                    echo "⚠️  Could not find valid JAVA_HOME"
                                    unset JAVA_HOME
                                fi
                            fi
                        else
                            echo "⚠️  Java not found in PATH"
                            unset JAVA_HOME
                        fi
                    fi
                    
                    # Final verification
                    if [ -n "$JAVA_HOME" ] && [ -f "$JAVA_HOME/bin/java" ]; then
                        echo "✅ Using JAVA_HOME: $JAVA_HOME"
                        echo "Java version: $($JAVA_HOME/bin/java -version 2>&1 | head -1)"
                        export JAVA_HOME
                    else
                        echo "⚠️  Warning: JAVA_HOME not properly set, Maven wrapper will attempt to find Java"
                        echo "Java path: $(which java 2>/dev/null || echo 'not found')"
                    fi
                    
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

