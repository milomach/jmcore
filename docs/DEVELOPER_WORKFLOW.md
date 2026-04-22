# Developer Workflow (Write Permissions)

This document explains how developers with write permissions should contribute to the project. It covers branch usage, pull requests, and best practices for maintaining a clean and stable codebase.

---
## Branch Overview

| Branch Type | Purpose                                                 | Protection / PR Requirement                |
| ----------- | ------------------------------------------------------- | ------------------------------------------ |
| `main`      | Stable, released code                                   | Protected: **all changes via PR required** |
| `develop`   | Integration branch for the next release                 | Protected: **all changes via PR required** |
| `feature/*` | Short-lived branches for developing individual features | Unprotected: developers can push freely    |
| `hotfix/*`  | Short-lived branches for urgent fixes to `main`         | Unprotected: developers can push freely    |

---
## Workflow for Features

1. **Create a branch**
   - Branch off `develop` for new features:
```bash
git checkout develop
git checkout -b feature/short-description
```
- This branch is unprotected. You can commit and push directly.

2. **Develop your feature**
    - Make as many commits as needed.
    - Push your branch to `origin`:
    ```bash
    git push origin feature/short-description
    ```
3. **Merge back to develop**
    - When the feature is complete, create a pull request:
        - **Source:** `feature/short-description`
        - **Target:** `develop`
    - Assign reviewers and follow the PR template.
    - The pull request ensures code review and automated checks before merging.

---
## Workflow for Hotfixes

1. **Create a hotfix branch**
    - Branch off `main`:
	```bash
	git checkout main
	git checkout -b hotfix/fix-description
	```
    - You can commit and push directly to the `hotfix/*` branch.
2. **Develop and test the fix**
    - Push changes as needed.
    - This branch is also unprotected to allow rapid development.
3. **Merge back**
    - Open a pull request:
        - **Source:** `hotfix/fix-description`
        - **Target:** `main`
    - After merging into `main`, also merge `main` → `develop` to propagate the fix.

---
## Workflow for `develop`

- The `develop` branch is **protected** to ensure integration quality.
- You **cannot push directly**; all changes must come through pull requests.
- Pull requests into `develop` should originate from:
    - Completed **`feature/*`** branches
    - Occasionally from **`main`** (to integrate post-release hotfixes)

**Requirements:**
- At least 1 approval
- All conversations resolved
- Passing status checks (if configured)

**Purpose:**
- `develop` serves as the staging ground for the next release.
- Once stable and tested, it will be merged into `main` via pull request.

---
## Workflow for `main`

- The `main` branch is **fully protected** to ensure production stability.
- You **cannot push directly**; all changes must go through pull requests.
- Pull requests into `main` should originate from:
    - The **`develop`** branch (for major or minor releases)
    - The **`hotfix/*`** branches (for urgent patches)

**Requirements:**
- At least 1 approval
- All conversations resolved
- Passing status checks (if configured)

**Purpose:**
- `main` represents the latest stable release version of the project.
- Each merge into `main` should create a tagged release.

---
## Best Practices

- **Commit messages:** Use clear, descriptive messages summarizing the change.
- **Small, focused PRs:** Keep changes minimal and logically grouped.
- **Keep branches up to date:** Regularly pull from `develop` (or `main` for hotfixes) to minimize merge conflicts.
- **Branch naming:** Follow consistent naming conventions:
    - Features: `feature/short-description`
    - Hotfixes: `hotfix/short-description`
- **Delete merged branches:** Remove feature/hotfix branches after merging to keep the repository clean.