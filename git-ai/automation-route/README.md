# Automation Route Partition

This directory is the default landing zone for automation artifacts in this repository.

## Scope

- Allowed: automation flow notes, route manifests, task orchestration docs, run templates.
- Not allowed: adding new automation orchestration files outside this directory unless explicitly approved by the user.

## Route Rules

1. Automation outputs should default to `git-ai/automation-route`.
2. If a task includes business code changes, record target and boundaries here before changing module files.
3. Frontend and backend quality gates must follow existing global project rules; do not redefine commands here.

## Suggested Structure

- `plans/`: iteration plans and execution breakdowns
- `runs/`: run logs and result snapshots
- `policies/`: route, gate, and rollback policies

This commit only initializes route constraints; subdirectories can be added in later iterations.
