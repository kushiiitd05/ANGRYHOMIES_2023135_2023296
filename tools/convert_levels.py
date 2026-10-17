"""One-off: convert the hard-coded LevelOne/Two/Three Java layouts into assets/levels/*.json.

Coordinates stay in the original world units (metres = old pixels / 10), so layouts are unchanged.
"""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "core/src/main/java/com/AngryHomies/AngryBirds/logic/level/levels"
OUT = ROOT / "assets/levels"
ARITH = re.compile(r"^[\d\s.+\-*/()f]+$")

MATERIAL = {"WoodBlock": "WOOD", "GlassBlock": "GLASS", "StoneBlock": "STONE"}
PIG = {"SmallPig": "SMALL", "BigPig": "BIG", "KingPig": "KING"}
TEXTURE_MATERIAL = {"stone": "STONE", "glass": "GLASS", "wood": "WOOD", "block.png": "WOOD"}
BIRD = {"RedBird": "RED", "YellowBird": "YELLOW", "BlackBird": "BLACK"}


def num(expr: str) -> float:
    expr = expr.strip()
    if not ARITH.match(expr):
        raise ValueError(f"not arithmetic: {expr!r}")
    return round(float(eval(expr.replace("f", ""), {"__builtins__": {}})), 3)


def split_args(arglist: str) -> list[str]:
    depth, cur, out = 0, "", []
    for ch in arglist:
        if ch == "(":
            depth += 1
        elif ch == ")":
            depth -= 1
        if ch == "," and depth == 0:
            out.append(cur)
            cur = ""
        else:
            cur += ch
    out.append(cur)
    return [a.strip() for a in out]


def parse(path: Path) -> dict:
    text = path.read_text()
    body = text[text.index("void initializeEntities"):text.index("void loadEntities")]
    level = {"birds": [], "pigs": [], "blocks": []}
    for kind, cls, args in re.findall(r"(birds|pigs|blocks)\.add\(new (\w+)\((.*)\)\);", body):
        a = split_args(args)
        tex = re.search(r'"images/([\w.]+)"', a[0]).group(1)
        if kind == "birds":
            level["birds"].append(BIRD[cls])
        elif kind == "pigs":
            x, y, w, h, dens, diam, hp = (num(v) for v in a[2:9])
            level["pigs"].append({"type": PIG[cls], "x": x, "y": y, "w": w, "h": h,
                                  "density": dens, "radius": round(diam / 2, 3)})
        else:
            x, y, w, h, dens = (num(v) for v in a[2:7])
            # The texture decides the material: LevelThree built WoodBlocks with stone art.
            material = next((m for key, m in TEXTURE_MATERIAL.items() if key in tex), MATERIAL[cls])
            level["blocks"].append({"material": material, "texture": tex,
                                    "x": x, "y": y, "w": w, "h": h, "density": dens})
    return level


def main() -> int:
    OUT.mkdir(parents=True, exist_ok=True)
    stars = {1: [15000, 25000, 35000], 2: [20000, 32000, 45000], 3: [25000, 40000, 55000]}
    for n, name in enumerate(("LevelOne", "LevelTwo", "LevelThree"), start=1):
        level = {"id": n, "name": f"Level {n}", "stars": stars[n], **parse(SRC / f"{name}.java")}
        (OUT / f"level{n}.json").write_text(json.dumps(level, indent=2) + "\n")
        print(n, {k: len(v) for k, v in level.items() if isinstance(v, list)})
    return 0


if __name__ == "__main__":
    sys.exit(main())
