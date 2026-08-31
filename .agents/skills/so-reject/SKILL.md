---
name: so-reject
description: Records human rejection with a reason for the current workflow human gate in customer-portal using story-orchestrator. Use when rejecting a human gate (e.g. /so:reject or 'reject current stage').
---

# Human Rejection Gate (`so-reject`)

Records human rejection of the current workflow human gate.

## Instructions

1. Invoke the `story-orchestrator` skill to record human rejection of the current human gate.
2. Adhere to the following requirements:
   - Read `docs/workflow/workflow-state.yaml`; the `current_stage` MUST be a stage whose type is `human_gate` in `docs/workflow/stage-map.yaml`. If it is not, refuse and report the current stage;
   - Require a non-empty reason; store it as `pending_human_gate.comment`;
   - Set `pending_human_gate.status = REJECTED`, `decided_at` (runtime), `decided_by`;
   - Append a `docs/workflow/history.jsonl` event with verdict `HUMAN_REJECTED`;
   - Route `current_stage` to the gate's `on_reject` target; clear `pending_human_gate`; set workflow status to `IN_PROGRESS`;
   - Do not invoke any stage Skill;
   - Finish with the Orchestration Result, including the loop-back target and the recommended next command.
