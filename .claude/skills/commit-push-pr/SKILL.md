---
name: commit-push-pr
description: Commit changes in meaningful units and create a PR.
argument-hint: [-b <base-branch>] [-d (draft)] [-p (include prompt)]
model: sonnet
context: fork
---

# Commit, Push & Create PR

Commit changes in meaningful units and create a PR in one go.

## 1. Execute Commits

Use the `/commit` command to create commits in meaningful units.
Pass any arguments provided to this command directly to the above command.

## 2. Create PR

Use the `/create-pr` command to create a PR.
Pass any arguments provided to this command directly to the above command.

## Notes

- **This command must be executed to completion.**
- If an error occurs, display the error message and abort the process
