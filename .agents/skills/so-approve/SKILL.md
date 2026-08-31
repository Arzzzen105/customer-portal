---
name: so-approve
description: Records human approval for the current workflow human gate in customer-portal using story-orchestrator. Use when approving a human gate (e.g. /so:approve or 'approve current stage').
---

# Human Approval Gate (`so-approve`)

Records human approval for the current workflow human gate.

## Instructions

1. Invoke the `story-orchestrator` skill to record human approval of the current human gate.
2. Adhere to the following requirements:
   - Read `docs/workflow/workflow-state.yaml`; the `current_stage` MUST be a stage whose type is `human_gate` in `docs/workflow/stage-map.yaml` (`HUMAN_SPEC_APPROVAL`, `HUMAN_PLAN_APPROVAL`, `HUMAN_PR_APPROVAL`, `READY_FOR_PR`, `COMPLETED`). If it is not, refuse and report the current stage;
   - Confirm every artifact in the gate's `required_artifacts` exists, is current (not `SUPERSEDED` / `ARCHIVED`), and its recorded automated verdict is `PASS` with no blocking findings; if not, refuse and report what is missing;
   - Set `pending_human_gate.status = APPROVED`, `decided_at` (runtime), `decided_by`; store approver comment if provided;
   - Append a `docs/workflow/history.jsonl` event with verdict `HUMAN_APPROVED`;
   - Advance `current_stage` to the gate's `on_approve` target; clear `pending_human_gate`; set workflow status to `IN_PROGRESS`, or `COMPLETED` / `ARCHIVED` when entering those stages;
   - Do not invoke any stage Skill;
   - Do not create, push, or merge a Pull Request;
   - Finish with the Orchestration Result.
