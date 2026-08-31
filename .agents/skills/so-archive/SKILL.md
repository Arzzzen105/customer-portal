---
name: so-archive
description: Archives a completed active User Story after required human gates in customer-portal using story-orchestrator in archive mode. Use when archiving a completed story (e.g. /so:archive or 'archive story').
---

# Archive Completed Story (`so-archive`)

Archives the completed active User Story after required human gates.

## Instructions

1. Invoke the `story-orchestrator` skill in `archive` mode.
2. Adhere to the following requirements:
   - Require `workflow-state.yaml` `current_stage == COMPLETED` and explicit human invocation of this command;
   - Create the delivery summary at the `delivery_summary` path from `docs/workflow/artifact-paths.yaml`;
   - Set the Story's catalog state to `ARCHIVED` in `docs/catalog/stories.yaml`;
   - Update `docs/knowledge/project-state.md`; propose (do not auto-apply) business-rules and architecture updates for human approval;
   - Preserve all historical artifacts in place (do not move or delete them);
   - Do not merge a Pull Request;
   - Request human approval before remote GitHub writes;
   - Clear `active-story.yaml` and set `current_stage` to `ARCHIVED` only after the delivery summary is written; append a `history.jsonl` event.
