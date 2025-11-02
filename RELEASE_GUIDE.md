# Release Guide - IRCTC Monolith

Complete guide for creating releases and publishing to GitHub Packages.

---

## Prerequisites

1. ✅ GitHub Actions workflow is working
2. ✅ Build passes successfully
3. ✅ Repository permissions configured
4. ✅ GitHub token with `write:packages` permission (if manual)

---

## Creating a Release

### Method 1: GitHub Web UI (Recommended)

1. **Navigate to Releases**
   - Go to your repository on GitHub
   - Click on "Releases" (right sidebar or `/releases`)

2. **Create New Release**
   - Click "Create a new release" or "Draft a new release"
   - Fill in the release details:
     - **Tag**: `v1.0.0` (follow semantic versioning)
     - **Target**: `main` or your release branch
     - **Title**: `v1.0.0 - Initial Release`
     - **Description**: Use the release template from `.github/release-template.md`

3. **Publish Release**
   - Check "Set as the latest release"
   - Click "Publish release"

4. **Workflow Triggers Automatically**
   - GitHub Actions workflow will:
     - Build the project
     - Run tests
     - Publish to GitHub Packages
     - Upload artifacts

### Method 2: GitHub CLI

```bash
# Install GitHub CLI if not installed
# brew install gh (macOS)
# apt install gh (Linux)

# Authenticate
gh auth login

# Create release
gh release create v1.0.0 \
  --title "v1.0.0 - Initial Release" \
  --notes-file .github/release-template.md \
  --target main

# Or with a specific branch
gh release create v1.0.0 \
  --title "v1.0.0" \
  --notes "Release notes here" \
  --target develop
```

### Method 3: Git Tags (Manual)

```bash
# Create and push tag
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0

# Then create release on GitHub UI with that tag
```

---

## Version Management

### Semantic Versioning

Follow [Semantic Versioning](https://semver.org/):

- **MAJOR.MINOR.PATCH** (e.g., `1.0.0`)
- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

### Update Version in Code

Before creating a release, update the version in `pom.xml`:

```xml
<version>1.0.0</version>
```

**Important**: After release, increment version for next development:

```xml
<version>1.0.1-SNAPSHOT</version>
```

---

## Verifying Release

### 1. Check GitHub Actions

1. Go to "Actions" tab
2. Find the workflow run triggered by release
3. Verify all steps completed successfully
4. Check for "Publish to GitHub Packages" step

### 2. Check GitHub Packages

1. Go to your repository
2. Click on "Packages" (right sidebar)
3. Find package: `com.irctc-backend:irctc`
4. Verify version is listed

### 3. Check Artifacts

1. Go to the Actions run
2. Scroll to "Artifacts" section
3. Download and verify JAR file

---

## Using the Published Package

### Maven Dependency

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

### Authentication

Add to `~/.m2/settings.xml`:

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

Create GitHub Personal Access Token:
- Settings → Developer settings → Personal access tokens → Tokens (classic)
- Scopes: `read:packages`, `write:packages`

---

## Release Checklist

Before creating a release:

- [ ] All tests passing
- [ ] Build successful on main/develop
- [ ] Version updated in `pom.xml`
- [ ] Release notes prepared
- [ ] Changelog updated (if maintained)
- [ ] Documentation reviewed
- [ ] Breaking changes documented
- [ ] Dependencies up to date

After release:

- [ ] Verify package published to GitHub Packages
- [ ] Download and test JAR artifact
- [ ] Update version to next SNAPSHOT
- [ ] Tag commit in local repository
- [ ] Announce release (if public project)

---

## Troubleshooting

### Release Created But Package Not Published

1. Check workflow logs for errors
2. Verify `GITHUB_TOKEN` has `packages:write` permission
3. Check `distributionManagement` in `pom.xml`
4. Verify repository URL is correct

### Authentication Issues

1. Ensure GitHub token is valid
2. Check token has required scopes
3. Verify `settings.xml` configuration
4. Try regenerating token

### Build Fails on Release

1. Check compilation errors
2. Review test failures
3. Verify dependencies available
4. Check Maven configuration

---

## Quick Release Script

Create a helper script `release.sh`:

```bash
#!/bin/bash

VERSION=$1
if [ -z "$VERSION" ]; then
    echo "Usage: ./release.sh <version>"
    echo "Example: ./release.sh 1.0.0"
    exit 1
fi

# Update version in pom.xml
sed -i '' "s/<version>.*<\/version>/<version>${VERSION}<\/version>/" pom.xml

# Commit version change
git add pom.xml
git commit -m "chore: Bump version to ${VERSION}"

# Create and push tag
git tag -a "v${VERSION}" -m "Release v${VERSION}"
git push origin main
git push origin "v${VERSION}"

echo "✅ Version ${VERSION} tagged and pushed"
echo "📦 Now create release on GitHub UI with tag v${VERSION}"
```

Make executable:
```bash
chmod +x release.sh
```

---

## Next Steps After Release

1. **Update Development Version**
   ```bash
   # Update pom.xml to next SNAPSHOT version
   ```

2. **Create Release Notes**
   - Document what changed
   - Highlight new features
   - Note breaking changes

3. **Notify Team**
   - Share release announcement
   - Update documentation links
   - Update deployment guides

---

**Status**: ✅ Ready for Release  
**Last Updated**: 2024-12-28

