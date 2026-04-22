# Contributor Workflow (Read Permissions)

This document explains how external contributors (without direct write access) should contribute to the project. It covers how to fork the repository, create feature branches, and submit pull requests while following the project’s branching conventions and standards.

---
## Branch Overview

|Branch Type|Purpose|Access / PR Requirement|
|---|---|---|
|`main`|Stable, released code|Protected: **no direct pushes allowed**|
|`develop`|Integration branch for the next release|Protected: **changes via pull request only**|
|`feature/*`|Short-lived branches for developing individual features|Created within your fork; merged via PR to `develop`|
|`hotfix/*`|Short-lived branches for urgent fixes to `main`|Created within your fork; merged via PR to `main`|

---
## Workflow for Features

1. **Fork the repository**
    - Click the **Fork** button on GitHub to create your own copy of the repository under your account.
    - Clone your fork locally:
        ```bash
        git clone https://github.com/your-username/repository-name.git
        cd repository-name
        ```
2. **Create a feature branch**
    - Always branch off `develop` (from your fork):
        ```bash
        git checkout develop
        git pull origin develop
        git checkout -b feature/short-description
        ```
    - Work within this branch for all related changes.
3. **Develop your feature**
    - Make your changes, commit them regularly with clear messages:
        ```bash
        git add .
        git commit -m "Add description of your change"
        ```
    - Push your branch to your fork:
        ```bash
        git push origin feature/short-description
        ```
4. **Open a pull request**
    - On GitHub, navigate to your fork and click **Compare & Pull Request**.
    - Set the PR base as:
        - **Source:** `your-username:feature/short-description`
        - **Target:** `upstream-repo:develop`
    - Fill out the PR template, link any relevant issues, and request a review.

---
## Workflow for Hotfixes

1. **Create a hotfix branch**
    - For urgent fixes to released versions, branch off `main`:
        ```bash
        git checkout main
        git pull origin main
        git checkout -b hotfix/fix-description
        ```
2. **Develop and test the fix**
    - Commit your changes and push the branch to your fork:
        ```bash
        git add .
        git commit -m "Fix description of issue"
        git push origin hotfix/fix-description
        ```
3. **Open a pull request**
    - Create a PR from your fork’s hotfix branch to the main repository:
        - **Source:** `your-username:hotfix/fix-description`
        - **Target:** `upstream-repo:main`
    - After review and merge, maintainers will ensure the fix is also merged into `develop`.

---
## Workflow for `develop` and `main`

- Contributors **cannot push directly** to `develop` or `main`.
- All changes must be proposed through pull requests.
- Pull requests are reviewed by maintainers and must:
    - Pass all automated checks (if any)
    - Have at least one approval
    - Resolve all review conversations before merging
- Merges into `develop` or `main` are performed by maintainers after review.

---
## Best Practices

- **Sync your fork regularly:**
    - Keep your fork up to date with the upstream repository:
        ```bash
        git remote add upstream https://github.com/original-author/repository-name.git
        git fetch upstream
        git merge upstream/develop
        ```
- **Commit messages:** Write concise, descriptive messages summarizing each change.
- **Small, focused PRs:** Keep pull requests minimal and well-scoped.
- **Branch naming:** Follow naming conventions:
    - Features: `feature/short-description`
    - Hotfixes: `hotfix/short-description`
- **Code quality:** Follow project code style and linting rules (if applicable).
- **Clean up after merging:** Delete your feature/hotfix branch after your PR is merged.