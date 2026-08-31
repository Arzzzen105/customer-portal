#!/usr/bin/env python3
"""PostToolUse hook: logs tool usage telemetry to docs/hooks/tool-usage.jsonl."""

import json
import sys
from pathlib import Path
from datetime import datetime, timezone

def main():
    try:
        raw = sys.stdin.read()
        if not raw.strip():
            print(json.dumps({}))
            return
        
        payload = json.loads(raw)
    except Exception:
        print(json.dumps({}))
        return

    # Extract tool information from Antigravity payload (camelCase or legacy)
    tool_call = payload.get("toolCall", {})
    tool_name = tool_call.get("name") or payload.get("tool_name") or payload.get("tool", "UNKNOWN")
    inputs = tool_call.get("args") or payload.get("inputs", {})
    response = payload.get("response") or payload.get("result") or payload.get("output")
    error = payload.get("error")

    input_json = json.dumps(inputs, ensure_ascii=False)
    response_json = json.dumps(response, ensure_ascii=False) if response is not None else ""

    if tool_name.startswith("mcp__idea__") or tool_name.startswith("idea__") or "idea" in tool_name.lower():
        category = "idea"
    elif tool_name.startswith("mcp__github__") or tool_name.startswith("github__") or "github" in tool_name.lower():
        category = "github"
    else:
        category = "other"

    entry = {
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "category": category,
        "response_head": str(response or error or "")[:200],
        "tool": tool_name,
        "input_size_bytes": len(input_json.encode("utf-8")),
        "response_size_bytes": len(response_json.encode("utf-8")),
        "total_size_bytes": len(input_json.encode("utf-8")) + len(response_json.encode("utf-8")),
        "input_keys": list(inputs.keys()) if isinstance(inputs, dict) else [],
        "response_type": type(response).__name__ if response is not None else ("error" if error else "None"),
        "error": error if error else None
    }

    log_file = Path("docs/hooks/tool-usage.jsonl")
    log_file.parent.mkdir(parents=True, exist_ok=True)

    try:
        with open(log_file, "a", encoding="utf-8") as f:
            f.write(json.dumps(entry, ensure_ascii=False) + "\n")
    except Exception:
        pass

    # Antigravity PostToolUse expects empty JSON object on stdout
    print(json.dumps({}))

if __name__ == "__main__":
    main()
