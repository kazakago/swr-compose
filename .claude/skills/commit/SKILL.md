---
name: commit
description: Commit changes in meaningful, well-scoped units.
argument-hint: [-b <base-branch>]
model: sonnet
context: fork
---

# Create Commits

Split changes into multiple commits, each representing a meaningful unit of work.

## 1. Parse Arguments

The following options are accepted:

- `-b <base-branch>`: Specify the base branch

## 2. Branch Check

If the current branch is `main`, `production`, or the branch specified with `-b`, create a new branch with an appropriate name before committing.

## 3. Commit Strategy

- Consider all changes for commits, regardless of whether they are staged or unstaged
- If the same file has both staged and unstaged changes, combine them into a single commit
- Split changes into multiple commits by meaningful units
- Each commit should serve a single purpose
- Keep commit granularity small enough that the commit message fits in one line
  - If a change cannot be described in one line, split it further
- Write commit messages in English
- Before committing, reorganize staging as needed to ensure each commit contains only relevant files

### Bad Example

```
fix: Fix multiple bugs
```

### Good Example

```
:bug: Fix keyboard not dismissing on login screen
:bug: Fix character encoding issue in username display
```

## Notes

- Never commit directly to `main` or `production`; always create a separate branch
- Separate unrelated changes into different commits
