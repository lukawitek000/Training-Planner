#!/bin/sh
# Antigravity PreToolUse Guardrail for Training Planner
# 1. Blocks accidental git commits directly to 'master' or 'develop'
# 2. Allows all other commands

INPUT=$(cat)

# Check if the command is attempting a git commit
if echo "$INPUT" | grep -qE '"CommandLine"[[:space:]]*:[[:space:]]*"[^"]*git[[:space:]]+commit'; then
  BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "")
  if [ "$BRANCH" = "master" ] || [ "$BRANCH" = "main" ] || [ "$BRANCH" = "develop" ]; then
    cat <<EOF
{
  "decision": "deny",
  "reason": "Git Branch Rule Violation: Direct commits to '$BRANCH' are blocked. Please create and switch to a dedicated feature/study branch (e.g., 'study/<topic>' or 'refactor/<module>') before committing."
}
EOF
    exit 0
  fi
fi

echo '{"decision": "allow"}'
