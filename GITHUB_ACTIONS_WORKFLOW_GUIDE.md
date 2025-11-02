# GitHub Actions Workflow Guide - Monolith

## Overview

This guide explains the GitHub Actions workflow configured for the IRCTC monolithic application. The workflow automatically builds, tests, and publishes the application to GitHub Packages when a release is created.

---

## Workflow File

**Location**: `.github/workflows/maven-package.yml`

### Triggers

The workflow runs on:
1. **Release Creation** (`release` event with `created` type) - Full build + publish to GitHub Packages
2. **Manual Trigger** (`workflow_dispatch`) - Allows manual execution from GitHub UI
3. **Push to Main/Develop** - Builds on code changes (publish disabled)

### Steps

1. **Checkout Code** - Retrieves the repository code
2. **Setup JDK 21** - Configures Java 21 (Temurin distribution)
3. **Cache Maven Dependencies** - Speeds up builds by caching `.m2` directory
4. **Build with Maven** - Executes `mvn clean package`
5. **Run Tests** - Executes `mvn test` (non-blocking)
6. **Publish to GitHub Packages** - Deploys artifacts (only on release)
7. **Upload JAR Artifact** - Stores build artifacts for download
8. **Generate Build Report** - Creates a summary in GitHub Actions UI

---

## Configuration Files

### 1. `pom.xml` Updates

**Added `distributionManagement` section**:
```xml
<distributionManagement>
    <repository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/${github.owner}/${github.repo}</url>
    </repository>
</distributionManagement>
```

**Properties**:
- Uses Maven properties `${github.owner}` and `${github.repo}` for dynamic repository URL
- These properties are set during the `mvn deploy` command

### 2. `settings.xml`

**Location**: Repository root (generated dynamically in workflow)

**Purpose**: Provides authentication for GitHub Packages

**Configuration**:
```xml
<servers>
    <server>
        <id>github</id>
        <username>${env.GITHUB_ACTOR}</username>
        <password>${env.GITHUB_TOKEN}</password>
    </server>
</servers>
```

**Note**: The `settings.xml` file is generated dynamically during the workflow execution to ensure proper authentication tokens are used.

---

## How It Works

### For Releases

1. **Trigger**: When you create a release in GitHub
2. **Build**: Maven builds the project with `clean package`
3. **Test**: Runs tests (optional, won't fail build)
4. **Deploy**: Publishes to GitHub Packages using `mvn deploy`
5. **Artifact**: JAR file is uploaded as an artifact

### For Regular Pushes

1. **Trigger**: Push to `main` or `develop` branches
2. **Build**: Same build process
3. **No Publish**: Artifacts are not published (only built)
4. **Artifact**: JAR file is available for download from Actions UI

---

## GitHub Packages

### Accessing Published Packages

Once published, your package will be available at:
```
https://github.com/OWNER/REPO/packages
```

### Using the Package

To use the published package in another Maven project, add to `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/OWNER/REPO</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.irctc-backend</groupId>
        <artifactId>irctc</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>
</dependencies>
```

### Authentication

When using the package, you'll need to authenticate:
1. Create a Personal Access Token (PAT) with `read:packages` permission
2. Add to `~/.m2/settings.xml`:
```xml
<servers>
    <server>
        <id>github</id>
        <username>YOUR_GITHUB_USERNAME</username>
        <password>YOUR_PERSONAL_ACCESS_TOKEN</password>
    </server>
</servers>
```

---

## Permissions

The workflow requires:
- `contents: read` - To checkout code
- `packages: write` - To publish to GitHub Packages

These are automatically granted to the `GITHUB_TOKEN` in GitHub Actions.

---

## Environment Variables

The workflow uses these environment variables:

- `GITHUB_TOKEN` - Automatically provided GitHub token
- `GITHUB_ACTOR` - GitHub username of the person triggering the workflow
- `GITHUB_REPOSITORY_OWNER` - Repository owner (from `github.repository_owner`)
- `GITHUB_REPOSITORY_NAME` - Repository name (from `github.event.repository.name`)

---

## Troubleshooting

### Build Failures

1. **Check JDK Version**: Ensure Java 21 is being used
2. **Check Maven Version**: Verify Maven wrapper is working
3. **Check Dependencies**: Ensure all dependencies are available
4. **Check Logs**: Review GitHub Actions logs for specific errors

### Publishing Failures

1. **Authentication**: Verify `GITHUB_TOKEN` has `packages:write` permission
2. **Repository URL**: Check that `${github.owner}` and `${github.repo}` are correctly set
3. **Distribution Management**: Verify `distributionManagement` in `pom.xml`
4. **Settings.xml**: Confirm settings.xml is generated correctly

### Common Issues

**Issue**: "401 Unauthorized" when publishing
- **Solution**: Check that `GITHUB_TOKEN` is properly configured and has write permissions

**Issue**: "Repository not found" error
- **Solution**: Verify the repository name and owner in the URL

**Issue**: "Artifact not found" after publishing
- **Solution**: Check GitHub Packages page - packages may take a few minutes to appear

---

## Manual Testing

### Test Build Locally

```bash
# Run the same commands locally
mvn clean package
mvn test
```

### Test Publishing Locally

1. Create a `settings.xml` with your GitHub credentials:
```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_USERNAME</username>
      <password>YOUR_PAT</password>
    </server>
  </servers>
</settings>
```

2. Run deploy:
```bash
mvn deploy -s settings.xml \
  -Dgithub.owner=YOUR_USERNAME \
  -Dgithub.repo=YOUR_REPO
```

---

## Workflow Customization

### Change Java Version

Edit `.github/workflows/maven-package.yml`:
```yaml
- name: Set up JDK 21
  uses: actions/setup-java@v4
  with:
    java-version: '21'  # Change this
```

### Add More Triggers

Edit the `on:` section:
```yaml
on:
  release:
    types: [created]
  workflow_dispatch:
  push:
    branches:
      - main
      - develop
      - feature/**  # Add more branches
```

### Skip Tests

Modify the test step:
```yaml
- name: Run tests
  run: mvn -B test --file pom.xml
  continue-on-error: true  # Already non-blocking
```

---

## Next Steps

1. ✅ **Workflow Created** - Ready to use
2. ⏳ **Create First Release** - Test the workflow
3. ⏳ **Verify Package** - Check GitHub Packages page
4. ⏳ **Update Documentation** - Document package usage

---

## Support

For issues or questions:
- Check GitHub Actions logs
- Review Maven build logs
- Verify GitHub Packages permissions
- Consult [GitHub Packages Documentation](https://docs.github.com/en/packages)

---

**Status**: ✅ **READY FOR USE**  
**Last Updated**: 2024-12-28

