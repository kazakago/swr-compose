---
name: merge-pr
description: Merge a PR and checkout the target branch.
argument-hint: [-b <target-branch>]
model: sonnet
context: fork
---

# Merge PR

## 1. Parse Arguments

The following options are accepted:

- `-b <target-branch>`: Specify the target branch (default: current branch)

## 2. Check for Uncommitted Changes

If there are uncommitted changes, use `AskUserQuestion` to let the user choose:
- Stash the uncommitted changes
- Discard the uncommitted changes

## 3. Find and Verify PR Status

1. Find the PR targeting the specified branch. If multiple PRs are found, use `AskUserQuestion` to ask which PR to target.
2. Check if the PR is mergeable (state, CI checks, review status). If CI checks are still running, wait for completion. If not mergeable, inform the user and terminate.

## 4. Merge PR and Update Local

1. Merge the PR using merge commit strategy with branch deletion
2. Update local Git information (fetch and prune)
3. If changes were stashed in step 2, use `AskUserQuestion` to ask the user:
   - Apply the stashed changes (restore but keep stash)
   - Pop the stashed changes (restore and remove stash)
   - Do nothing

## Notes

- **This command must be executed to completion.**
- If an error or unrecoverable state is encountered, inform the user and terminate
