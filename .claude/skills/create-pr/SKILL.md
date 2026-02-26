---
name: create-pr
description: Create a PR from the current commits.
argument-hint: [-b <base-branch>] [-d (draft)] [-p (include prompt)]
model: sonnet
context: fork
---

# Create PR

Follow the steps below to create a PR.

## 1. Parse Arguments

The following options are accepted:

- `-b <base-branch>`: Specify the base branch (default: `main`)
- `-d`: Create as a draft PR
- `-p`: Include session prompts in the PR body

Examples:

- `/create-pr` → Normal PR based on main
- `/create-pr -b production` → Normal PR based on production
- `/create-pr -d` → Draft PR based on main
- `/create-pr -b production -d` → Draft PR based on production
- `/create-pr -p` → Normal PR based on main (with prompt)
- `/create-pr -b production -d -p` → Draft PR based on production (with prompt)

## 2. Determine Base Branch

- If `-b` option is specified: Use that branch as base
- If `-b` option is not specified: Use `main` as base

## 3. Pre-checks

Review the commit log and diff against the base branch to understand the full scope of changes.

## 4. Retrieve Prompt (only when -p option is specified)

If the `-p` option is specified, execute the `/export-prompt` command and keep the retrieved prompts for later use.

## 5. Compose PR Body

Use the template (@.github/PULL_REQUEST_TEMPLATE.md) to compose the PR body.

### Auto-generate PR Title and Description

- PR title: Summarize the commit log concisely in English
- What was done: Auto-generate from commit log analysis
- Why it was done: Auto-generate from commit log analysis

**Important**: Do not stop and ask the user for input. Auto-generate everything from the commit log.

### Include Prompt (only when -p option is specified)

If the `-p` option is specified, append the following to the end of the template:

**Important**: Output backticks (`` ` ``) as-is without escaping, as they are used for Markdown formatting.

````markdown
## :robot: Prompt

<details>
<summary>Prompt</summary>
<p>

```
[Retrieved prompt content]
```

</p>
</details>
````

## 6. Push and Create PR

Push the branch and create the PR. Use `--draft` flag if `-d` option was specified.

- If including related URLs, verify they are accessible and not 404
- Do not include links to changed files in related URLs (they can be viewed in the PR diff)
- Only include meaningful links in related URLs. Leaving it empty is fine

## Notes

- **This command must be executed to completion.**
- Do not create new commits
- Only use existing commits to create the PR
- Do not make assumptions from conversation context (e.g., assuming commits are missing)
