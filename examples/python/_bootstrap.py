from __future__ import annotations

import os
import sys
from pathlib import Path


def bootstrap_local_sdk() -> None:
    repo_root = Path(__file__).resolve().parents[2]
    sdk_src = repo_root / "sdk" / "python" / "src"
    sdk_src_str = str(sdk_src)
    if sdk_src_str not in sys.path:
        sys.path.insert(0, sdk_src_str)

    cwd_env = Path.cwd() / ".env"
    repo_env = repo_root / ".env"

    _load_env_file(cwd_env)
    if repo_env != cwd_env:
        _load_env_file(repo_env)


def _load_env_file(path: Path) -> None:
    if not path.is_file():
        return

    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#"):
            continue

        if line.startswith("export "):
            line = line[len("export ") :].strip()

        if "=" not in line:
            continue

        key, value = line.split("=", 1)
        key = key.strip()
        value = value.strip()

        if not key:
            continue

        if len(value) >= 2 and value[0] == value[-1] and value[0] in {"'", '"'}:
            value = value[1:-1]

        os.environ.setdefault(key, value)
