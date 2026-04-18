# Automation Branch Scope Policy

## Objective

Keep automation execution constrained to `git-ai/automation-route` to avoid cross-branch drift.

## Mandatory Rules

1. Before edits, verify current branch is `git-ai/automation-route`.
2. If branch is different, switch to `git-ai/automation-route` before any automation change.
3. New automation artifacts must live under `git-ai/automation-route/`.
4. If `git-ai/automation-route` is bound to another worktree, migrate changes safely (stash/cherry-pick/manual merge) instead of committing on other branches.
5. If migration detects conflicts, keep existing branch content first, then append incoming automation updates.

## Verification Checklist

- `git branch --show-current` returns `git-ai/automation-route`.
- Changed files for automation tasks are under `git-ai/automation-route/` unless user explicitly approves otherwise.
- Run record includes branch confirmation and migration note when applicable.

