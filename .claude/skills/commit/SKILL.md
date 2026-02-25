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

1. Get the current branch name:

```bash
git branch --show-current
```

2. Check if the current branch matches any of the following:

- `main`
- `production`
- The branch specified with `-b`

If any match is found, perform the following steps:

1. Determine an appropriate branch name
2. Create the branch with `git checkout -b <branch-name>`

## 3. Check Current Status

```bash
git status
```

## 4. Commit Strategy

- Consider all changes for commits, regardless of whether they are staged or unstaged
- If the same file has both staged and unstaged changes, combine them into a single commit
- Split changes into multiple commits by meaningful units
- Each commit should serve a single purpose
- Keep commit granularity small enough that the commit message fits in one line
  - If a change cannot be described in one line, split it further
- Write commit messages in English

### Bad Example

```
fix: Fix multiple bugs
```

### Good Example

```
:bug: Fix keyboard not dismissing on login screen
:bug: Fix character encoding issue in username display
```

## 5. Organize Staging

Before committing, check the current staging state and reorganize as needed.

```bash
# If files are already staged, unstage everything first
git reset HEAD
```

Stage only the relevant files for each commit to ensure meaningful granularity.

## 6. Execute Commits

Stage and commit changes. For each commit, follow these steps:

1. Stage target files: `git add <file>`
2. Execute commit: `git commit -m "<message>"`
3. Repeat steps 1-2 for subsequent commits

## Notes

- Never commit directly to main or production; always create a separate branch
- Separate unrelated changes into different commits
