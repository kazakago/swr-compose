---
name: export-prompt
description: Export only user inputs from the conversation. Copies to clipboard by default, or outputs to a file if a filepath is specified.
argument-hint: [filepath]
model: haiku
context: fork
---

# Export User Inputs

Export only the user-entered content from this conversation.

## 1. Target Content

Extract user inputs following these rules:

- Target user messages from the **entire conversation history** (including history before session resumption via `claude -c`)
- Do not include Claude's responses
- Do not include system messages or command execution results
- Output each input in chronological order, separated by dividers (`---`)

## 2. Output Format

```
# User Inputs

---
[First user input]

---
[Second user input]

---
[Nth user input]
```

## 3. Execute Output

Arguments: $ARGUMENTS

### No arguments (clipboard)

1. Compose the content using the format above
2. **Use the `Bash` tool to execute the following command**:
   ```bash
   cat <<'EOF' | pbcopy
   [Composed content]
   EOF
   ```
3. Report: "Copied to clipboard"

### With arguments (file output)

1. Compose the content using the format above
2. **Use the `Write` tool** to output to the specified file path
3. Report: "Output to [filepath]"

## Important Notes

- **Never report "Copied" without actually using a tool**
- Carefully review the conversation history and extract only user inputs
- Follow the output format strictly
- No need to display the content in the response (it will be in the clipboard or file)
