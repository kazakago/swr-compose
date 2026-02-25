---
name: merge-pr
description: Merge a PR and checkout the target branch.
argument-hint: [-b <target-branch>]
model: sonnet
context: fork
---

## 1. Parse Arguments

The following options are accepted:

- `-b <target-branch>`: Specify the target branch (default: current branch from `$(git branch --show-current)`)

# Check for Uncommitted Changes

1. Check the current Git status for uncommitted changes

2. If there are uncommitted changes, follow the steps below:
   - Use the `AskUserQuestion` tool to present the following options:
     - Stash the uncommitted changes
     - Discard the uncommitted changes

3. If the user selects an option in step 2, proceed with the corresponding Git operation

# Find and Verify PR Status

1. Find the PR targeting the specified branch:

```bash
gh pr view <target-branch>
```

If multiple PRs are found, use the `AskUserQuestion` tool to ask which PR to target.
Once the PR is determined, run the following command and note the PR number and base branch:

```bash
gh pr view <target-branch> --json number,baseRefName
```

2. Check if the PR is mergeable:

```bash
gh pr view <PR-number> --json title,state,mergeable,mergeStateStatus,statusCheckRollup,reviewDecision
```

If mergeable, proceed to the next step.
If not mergeable, inform the user and terminate.
If CI checks are still running, wait for completion before making a decision.

# Merge PR and Update Local

1. Merge the PR:

```bash
gh pr merge <PR-number> --merge --delete-branch
```

2. Update local Git information:

```bash
git fetch --prune
```

3. If changes were stashed, ask what to do with them:

Use the `AskUserQuestion` tool to ask the user how to handle stashed changes:

- Apply the stashed changes (restore but keep stash)
- Pop the stashed changes (restore and remove stash)
- Do nothing

※ Skip this step if no changes were stashed

## Notes

- **This command must be executed to completion.**
- If an error or unrecoverable state is encountered, inform the user and terminate
