---
name: so-start
description: Activates a specific User Story and initializes its workflow state in customer-portal using story-orchestrator. Use when starting, activating, or initializing a User Story (e.g. /so:start US-001 or 'start story US-001').
---

# Start Story Workflow (`so-start`)

Activates a User Story and initializes its delivery workflow.

## Instructions

1. Invoke the `story-orchestrator` skill in `start` mode with the requested Story ID (e.g., `US-001`).
2. Adhere to the following requirements:
   - Activate only the explicitly requested Story;
   - Do not replace another active Story;
   - Initialize `docs/workflow/active-story.yaml` and `docs/workflow/workflow-state.yaml` per `docs/workflow/state-schema.md`;
   - Set the Story's catalog state to `IN_PROGRESS` in `docs/catalog/stories.yaml`;
   - Append an activation event to `docs/workflow/history.jsonl`;
   - Do not start stage execution automatically;
   - Finish with the Start Result and recommend `so-next` (or `/so:next`).
