# Release Notes Template

## Version [VERSION] - [DATE]

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

### Maven
```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/[OWNER]/[REPO]</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.irctc-backend</groupId>
        <artifactId>irctc</artifactId>
        <version>[VERSION]</version>
    </dependency>
</dependencies>
```

### Gradle
```groovy
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/[OWNER]/[REPO]")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.token") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation 'com.irctc-backend:irctc:[VERSION]'
}
```

