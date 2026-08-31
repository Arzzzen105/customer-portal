#!/usr/bin/env python3
"""PreInvocation hook: checks for accidental backslash at start of prompt."""

import json
import sys
from pathlib import Path

def main():
    try:
        raw_input = sys.stdin.read()
        if not raw_input.strip():
            print(json.dumps({}))
            return

        payload = json.loads(raw_input)
        
        # Check direct prompt in payload
        prompt = payload.get("prompt") or payload.get("userMessage") or payload.get("content") or ""
        
        # Or inspect transcript if path is provided
        if not prompt and "transcriptPath" in payload:
            t_path = Path(payload["transcriptPath"])
            if t_path.is_file():
                try:
                    with open(t_path, "r", encoding="utf-8") as f:
                        lines = f.readlines()
                    for line in reversed(lines):
                        step = json.loads(line)
                        if step.get("type") == "USER_INPUT":
                            prompt = step.get("content", "")
                            break
                except Exception:
                    pass

        if prompt and prompt.lstrip().startswith("\\"):
            print(json.dumps({
                "injectSteps": [
                    {
                        "ephemeralMessage": (
                            "Повідомлення починається з `\\`. Команди Antigravity вводяться з `/` або через відповідний скіл (наприклад `so-next`, `so-start`). "
                            "Якщо це був звичайний текст, приберіть `\\` на початку."
                        )
                    }
                ]
            }))
            return

        print(json.dumps({}))
    except Exception:
        print(json.dumps({}))

if __name__ == "__main__":
    main()
