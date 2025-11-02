# Release Notes Template

**⚠️ IMPORTANT: Before creating a release, you MUST create a git tag first!**

**Quick Steps:**
1. Run: `./release.sh 1.0.0` (or manually create tag: `git tag -a v1.0.0 -m "Release v1.0.0"`)
2. Push tag: `git push origin v1.0.0`
3. Then create release on GitHub UI and select the tag `v1.0.0`

---

## Version 1.0.0 - 2024-12-28

### 🚀 What's New
- [Add new features]
- [Add improvements]

### 🐛 Bug Fixes
- [Add bug fixes]

### 📦 Changes
- [Add breaking changes or important updates]

### 📋 Technical Details
- **Build**: [GitHub Actions Build #]
- **JDK**: 21
- **Maven**: [Version]
- **Spring Boot**: 3.5.6

### 📥 Download
- **JAR**: Available in GitHub Packages and Actions artifacts
- **Package**: `com.irctc-backend:irctc:[VERSION]`

### 🔗 Links
- [Documentation](#)
- [Changelog](#)

---

## Installation

**Replace `[OWNER]` with your GitHub username and `[REPO]` with your repository name.**

### Maven
```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/YOUR_USERNAME/YOUR_REPO</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.irctc-backend</groupId>
        <artifactId>irctc</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

**Also add to `~/.m2/settings.xml`:**
```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>YOUR_USERNAME</username>
            <password>YOUR_GITHUB_TOKEN</password>
        </server>
    </servers>
</settings>
```

### Gradle
```groovy
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/YOUR_USERNAME/YOUR_REPO")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.token") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation 'com.irctc-backend:irctc:1.0.0'
}
```

