---
name: so-status
description: Shows the current User Story workflow status, stage inputs/outputs, and health in customer-portal using story-orchestrator in read-only status mode. Use when checking workflow status (e.g. /so:status or 'workflow status').
---

# Workflow Status Report (`so-status`)

Shows the current User Story workflow status.

## Instructions

1. Invoke the `story-orchestrator` skill in `status` mode.
2. Adhere to the following requirements:
   - Use read-only operations;
   - Do not invoke a stage Skill;
   - Do not modify workflow state or any artifact;
   - Resolve stage + paths from `docs/workflow/stage-map.yaml` and `docs/workflow/artifact-paths.yaml`;
   - Report workflow health, current stage inputs/outputs and their status, stale inputs, blockers, the pending Human Gate with its exact approval command, and the recommended next command.
