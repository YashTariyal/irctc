# How to Create a GitHub Release

## Quick Fix for "tag name can't be blank" Error

This error occurs when you try to create a release without first creating a git tag.

---

## Solution: Create Tag First, Then Release

### Method 1: Using the Release Script (Easiest)

```bash
# Run the release script - it creates the tag automatically
./release.sh 1.0.0
```

This script will:
1. ✅ Update version in `pom.xml`
2. ✅ Build the project
3. ✅ Commit changes
4. ✅ Create git tag `v1.0.0`
5. ✅ Push tag to GitHub

Then go to GitHub → Releases → Create new release → Select tag `v1.0.0`

### Method 2: Manual Tag Creation

If you prefer to do it manually:

```bash
# 1. Update version in pom.xml manually
# Change <version>0.0.1-SNAPSHOT</version> to <version>1.0.0</version>

# 2. Commit the change
git add pom.xml
git commit -m "chore: Bump version to 1.0.0"
git push origin main

# 3. Create and push the tag
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
```

Then create release on GitHub UI.

### Method 3: Create Tag from GitHub UI

1. Go to **Code** tab → **Tags** section
2. Click **"Create new tag"** or go to: `https://github.com/[OWNER]/[REPO]/releases/new`
3. Fill in:
   - **Tag name**: `v1.0.0` (must start with `v`)
   - **Target**: `main` (or your release branch)
   - **Description**: Optional
4. Click **"Create tag"**
5. Then click **"Generate release notes"** or **"Create release"**

---

## Common Mistakes

### ❌ Wrong: Creating Release Without Tag
- Going to Releases → "Create new release"
- Not selecting/creating a tag first
- **Result**: "tag name can't be blank" error

### ✅ Correct: Tag First, Then Release
- Create tag first (via script or manually)
- Then create release and select the existing tag
- **Result**: Success!

---

## Step-by-Step: Complete Release Process

### Step 1: Prepare for Release

```bash
# Option A: Use automated script
./release.sh 1.0.0

# Option B: Manual preparation
# 1. Update pom.xml version
# 2. Build and test: ./mvnw clean package
# 3. Commit: git add pom.xml && git commit -m "chore: Bump version"
```

### Step 2: Create Git Tag

```bash
# Create annotated tag
git tag -a v1.0.0 -m "Release v1.0.0"

# Push tag to GitHub
git push origin v1.0.0
```

**Verify tag exists:**
```bash
git tag -l
# Should see: v1.0.0
```

### Step 3: Create Release on GitHub

1. **Go to Releases Page**
   - Navigate to: `https://github.com/[OWNER]/[REPO]/releases`
   - Or click "Releases" in repository sidebar

2. **Click "Create a new release"** or **"Draft a new release"**

3. **Fill in Release Details**
   - **Choose a tag**: Select `v1.0.0` from dropdown (or type it)
   - **Target**: Select `main` (or your release branch)
   - **Release title**: `v1.0.0 - Initial Release`
   - **Description**: Copy from `.github/release-template.md` and fill in details

4. **Publish Release**
   - Check "Set as the latest release"
   - Click **"Publish release"**

### Step 4: Verify

- ✅ Check Actions tab - workflow should run automatically
- ✅ Check Packages tab - package should be published
- ✅ Download JAR from Actions artifacts

---

## Tag Naming Conventions

GitHub requires tags to be:
- ✅ Valid git tag names
- ✅ Not empty
- ✅ Follow naming conventions

**Good tag names:**
- `v1.0.0` ✅
- `v1.0.0-beta` ✅
- `1.0.0` ✅ (but `v1.0.0` is preferred)
- `release-1.0.0` ✅

**Bad tag names:**
- Empty string ❌
- `v 1.0.0` ❌ (spaces)
- `v1.0.0/v1.0.0` ❌ (invalid characters)

---

## Troubleshooting

### Error: "tag name can't be blank"

**Cause**: Trying to create release without selecting/creating a tag

**Solution**: 
1. Create tag first: `git tag -a v1.0.0 -m "Release" && git push origin v1.0.0`
2. Then create release and select that tag

### Error: "tag name is not well-formed"

**Cause**: Invalid characters in tag name

**Solution**: Use semantic versioning like `v1.0.0`

### Tag Not Showing in Dropdown

**Cause**: Tag not pushed to GitHub

**Solution**: 
```bash
git push origin v1.0.0
```

### Release Created But Package Not Published

**Cause**: Workflow may have failed

**Solution**: 
1. Check Actions tab for errors
2. Verify `GITHUB_TOKEN` has `packages:write` permission
3. Check workflow logs

---

## Quick Reference

```bash
# Complete release process in one go
./release.sh 1.0.0              # Creates tag and updates version
# Then create release on GitHub UI
```

Or manually:
```bash
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
# Then create release on GitHub UI
```

---

**Remember**: Tag first, Release second! 🏷️

