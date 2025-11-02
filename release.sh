#!/bin/bash

# IRCTC Monolith Release Script
# Usage: ./release.sh <version>
# Example: ./release.sh 1.0.0

set -e

VERSION=$1

if [ -z "$VERSION" ]; then
    echo "❌ Error: Version required"
    echo ""
    echo "Usage: ./release.sh <version>"
    echo "Example: ./release.sh 1.0.0"
    exit 1
fi

# Validate version format (semantic versioning)
if ! [[ $VERSION =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "❌ Error: Invalid version format"
    echo "Version must follow semantic versioning: MAJOR.MINOR.PATCH"
    echo "Example: 1.0.0"
    exit 1
fi

echo "🚀 Preparing release v${VERSION}..."
echo ""

# Get current directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Check if pom.xml exists
if [ ! -f "pom.xml" ]; then
    echo "❌ Error: pom.xml not found"
    exit 1
fi

# Check if git repository
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    echo "❌ Error: Not a git repository"
    exit 1
fi

# Check if working directory is clean
if ! git diff-index --quiet HEAD --; then
    echo "⚠️  Warning: Working directory has uncommitted changes"
    read -p "Continue anyway? (y/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Backup pom.xml
cp pom.xml pom.xml.backup

# Update version in pom.xml
echo "📝 Updating version in pom.xml to ${VERSION}..."

if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    sed -i '' "s/<version>\([^<]*\)<\/version>/<version>${VERSION}<\/version>/" pom.xml
else
    # Linux
    sed -i "s/<version>\([^<]*\)<\/version>/<version>${VERSION}<\/version>/" pom.xml
fi

# Verify version was updated
if ! grep -q "<version>${VERSION}</version>" pom.xml; then
    echo "❌ Error: Failed to update version in pom.xml"
    mv pom.xml.backup pom.xml
    exit 1
fi

echo "✅ Version updated to ${VERSION}"
echo ""

# Build and test
echo "🔨 Building project..."
./mvnw clean package -DskipTests || {
    echo "❌ Build failed. Restoring pom.xml..."
    mv pom.xml.backup pom.xml
    exit 1
}

echo "✅ Build successful"
echo ""

# Commit version change
echo "📦 Committing version change..."
git add pom.xml
git commit -m "chore: Bump version to ${VERSION}" || {
    echo "⚠️  Nothing to commit or commit failed"
}

echo "✅ Version change committed"
echo ""

# Create and push tag
echo "🏷️  Creating tag v${VERSION}..."
if git rev-parse "v${VERSION}" >/dev/null 2>&1; then
    echo "⚠️  Tag v${VERSION} already exists"
    read -p "Delete and recreate? (y/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        git tag -d "v${VERSION}"
        git push origin ":refs/tags/v${VERSION}" 2>/dev/null || true
    else
        echo "❌ Aborted"
        exit 1
    fi
fi

git tag -a "v${VERSION}" -m "Release v${VERSION}"

echo "✅ Tag created"
echo ""

# Push changes
echo "📤 Pushing to remote..."
read -p "Push changes and tag to origin? (y/N) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    git push origin main || git push origin master
    git push origin "v${VERSION}"
    echo "✅ Changes pushed"
    echo ""
    echo "🎉 Release v${VERSION} prepared!"
    echo ""
    echo "📋 Next Steps:"
    echo "   1. Go to GitHub → Releases → Draft a new release"
    echo "   2. Select tag: v${VERSION}"
    echo "   3. Add release notes"
    echo "   4. Click 'Publish release'"
    echo ""
    echo "   The GitHub Actions workflow will automatically:"
    echo "   - Build the project"
    echo "   - Publish to GitHub Packages"
    echo "   - Upload artifacts"
    echo ""
else
    echo "⚠️  Changes not pushed"
    echo ""
    echo "📋 Manual steps:"
    echo "   1. git push origin main"
    echo "   2. git push origin v${VERSION}"
    echo "   3. Create release on GitHub"
fi

# Cleanup backup
rm -f pom.xml.backup

echo ""
echo "✅ Done!"

