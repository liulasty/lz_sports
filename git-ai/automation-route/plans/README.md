# Plans Template

Use this folder to register upcoming automation tasks before touching module code.

## Required Fields

- task_id
- goal
- scope_in_repo
- out_of_scope
- validation_commands
- rollback_notes

## Minimal Example

```md
task_id: BE-012
goal: Add uploadLogo boundary tests
scope_in_repo:
  - lz_sports_backend/src/test/java/com/lz/service/impl/SchoolConfigServiceImplTest.java
out_of_scope:
  - lz_sports_frontend/**
validation_commands:
  - cd lz_sports_backend && mvn -Pnon-container-baseline test
rollback_notes: Revert the new test cases if baseline breaks.
```
