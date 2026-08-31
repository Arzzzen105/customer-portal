---
name: so-next
description: Advances the active User Story by one workflow stage using story-orchestrator in continue mode. Use when advancing or continuing the active story (e.g. /so:next or 'continue workflow').
---

# Advance Story Workflow (`so-next`)

Advances the active User Story by exactly one workflow stage.

## Instructions

1. Invoke the `story-orchestrator` skill in `continue` mode.
2. Adhere to the following requirements:
   - Process only the active User Story;
   - Perform at most one workflow transition;
   - Invoke at most one stage Skill;
   - Resolve stage routing from `docs/workflow/stage-map.yaml` and artifact paths from `docs/workflow/artifact-paths.yaml`;
   - At a human gate (`HUMAN_SPEC_APPROVAL`, `HUMAN_PLAN_APPROVAL`, `HUMAN_PR_APPROVAL`, `READY_FOR_PR`, `COMPLETED`): invoke no Skill, set `WAITING_FOR_HUMAN`, list the artifacts to review with versions and the automated verdict, and report that `so-approve` (`/so:approve`) or `so-reject` (`/so:reject`) records the decision;
   - Respect all Human Gates and configured hooks;
   - Record the transition in `docs/workflow/workflow-state.yaml` and append `docs/workflow/history.jsonl`;
   - Finish with the Orchestration Result;
   - Do not recursively continue to another stage.
