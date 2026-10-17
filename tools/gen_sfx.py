"""Synthesise the game's sound effects into assets/audio/sfx/*.wav.

Every sound is generated from noise and oscillators with a fixed seed, so the output is
reproducible and free of licence questions. Run:  python tools/gen_sfx.py
"""
from __future__ import annotations

import sys
import wave
from pathlib import Path

import numpy as np
from scipy.signal import butter, lfilter

RATE = 44_100
OUT = Path(__file__).resolve().parents[1] / "assets" / "audio" / "sfx"
rng = np.random.default_rng(20241129)


# ---------- building blocks ----------
def t_axis(seconds: float) -> np.ndarray:
    return np.arange(int(seconds * RATE)) / RATE


def env(seconds: float, attack: float = 0.005, decay: float = 0.2) -> np.ndarray:
    t = t_axis(seconds)
    rise = np.clip(t / max(attack, 1e-4), 0, 1)
    return rise * np.exp(-np.maximum(t - attack, 0) / decay)


def noise(seconds: float) -> np.ndarray:
    return rng.uniform(-1, 1, int(seconds * RATE))


def band(x: np.ndarray, low: float, high: float, order: int = 2) -> np.ndarray:
    b, a = butter(order, [low / (RATE / 2), high / (RATE / 2)], btype="band")
    return lfilter(b, a, x)


def lowpass(x: np.ndarray, cutoff: float, order: int = 2) -> np.ndarray:
    b, a = butter(order, cutoff / (RATE / 2), btype="low")
    return lfilter(b, a, x)


def highpass(x: np.ndarray, cutoff: float, order: int = 2) -> np.ndarray:
    b, a = butter(order, cutoff / (RATE / 2), btype="high")
    return lfilter(b, a, x)


def sweep(seconds: float, f0: float, f1: float, shape: str = "sine") -> np.ndarray:
    t = t_axis(seconds)
    freq = f0 * (f1 / f0) ** (t / seconds)
    phase = 2 * np.pi * np.cumsum(freq) / RATE
    return np.sin(phase) if shape == "sine" else np.sign(np.sin(phase)) * 0.6


def tone(seconds: float, freq: float, partials=((1, 1.0),)) -> np.ndarray:
    t = t_axis(seconds)
    return sum(amp * np.sin(2 * np.pi * freq * mult * t) for mult, amp in partials)


def place(canvas: np.ndarray, clip: np.ndarray, at: float) -> np.ndarray:
    start = int(at * RATE)
    end = min(len(canvas), start + len(clip))
    out = canvas.copy()
    out[start:end] += clip[: end - start]
    return out


def save(name: str, x: np.ndarray, peak: float = 0.85, rate: int = RATE) -> None:
    x = np.nan_to_num(x)
    x = x / (np.max(np.abs(x)) + 1e-9) * peak
    fade = min(len(x), int(0.004 * rate))
    x[-fade:] *= np.linspace(1, 0, fade)
    OUT.mkdir(parents=True, exist_ok=True)
    with wave.open(str(OUT / f"{name}.wav"), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(rate)
        w.writeframes((x * 32767).astype(np.int16).tobytes())
    print(f"{name:16s} {len(x) / rate:5.2f}s")


# ---------- the sounds ----------
def sling_stretch() -> np.ndarray:
    d = 0.45
    creak = band(noise(d), 300, 900) * (0.5 + 0.5 * np.sin(2 * np.pi * 38 * t_axis(d)) ** 8)
    rubber = sweep(d, 140, 260) * 0.25
    return (creak + rubber) * env(d, 0.08, 0.5)


def sling_release() -> np.ndarray:
    d = 0.55
    twang = tone(d, 110, ((1, 1), (2, 0.5), (3, 0.25))) * env(d, 0.002, 0.08)
    whoosh = band(noise(d), 500, 3000) * np.sin(np.pi * np.clip(t_axis(d) / d, 0, 1)) ** 2
    return twang * 0.9 + whoosh * 0.7


def wood_hit() -> np.ndarray:
    d = 0.35
    knock = tone(d, 190, ((1, 1), (2.7, 0.4), (5.1, 0.15))) * env(d, 0.001, 0.06)
    thump = lowpass(noise(d), 600) * env(d, 0.001, 0.04) * 2
    return knock + thump


def wood_break() -> np.ndarray:
    d = 0.8
    out = np.zeros(int(d * RATE))
    for i in range(9):
        crack = band(noise(0.12), 400 + 120 * i, 2500) * env(0.12, 0.001, 0.025)
        out = place(out, crack * rng.uniform(0.5, 1), rng.uniform(0, 0.35))
    return place(out, wood_hit(), 0.0)


def glass_hit() -> np.ndarray:
    d = 0.6
    ping = sum(tone(d, f) * a for f, a in ((2350, 1), (3810, 0.6), (5230, 0.4))) * env(d, 0.001, 0.12)
    return ping + highpass(noise(d), 4000) * env(d, 0.001, 0.01) * 0.6


def glass_break() -> np.ndarray:
    d = 1.1
    out = highpass(noise(d), 2500) * env(d, 0.001, 0.08) * 0.8
    for _ in range(26):
        f = rng.uniform(2500, 7500)
        tinkle = tone(0.25, f) * env(0.25, 0.001, rng.uniform(0.02, 0.08))
        out = place(out, tinkle * rng.uniform(0.2, 0.7), rng.uniform(0, 0.7))
    return out


def stone_hit() -> np.ndarray:
    d = 0.45
    thud = sweep(d, 120, 55) * env(d, 0.001, 0.09)
    grit = band(noise(d), 800, 3500) * env(d, 0.001, 0.03) * 0.5
    return thud * 1.2 + grit


def stone_break() -> np.ndarray:
    d = 1.2
    rumble = lowpass(noise(d), 300, 3) * env(d, 0.005, 0.35) * 3
    out = rumble
    for _ in range(14):
        chip = band(noise(0.08), 900, 4000) * env(0.08, 0.001, 0.02)
        out = place(out, chip * rng.uniform(0.3, 0.8), rng.uniform(0, 0.6))
    return place(out, stone_hit(), 0.0)


def pig_pop() -> np.ndarray:
    d = 0.4
    pop = sweep(d, 900, 120) * env(d, 0.001, 0.05)
    puff = lowpass(noise(d), 1800) * env(d, 0.002, 0.12) * 0.6
    return pop + puff


def bird_thud() -> np.ndarray:
    d = 0.25
    return sweep(d, 220, 90) * env(d, 0.001, 0.05) + lowpass(noise(d), 900) * env(d, 0.001, 0.03)


def bird_hop() -> np.ndarray:
    d = 0.18
    return sweep(d, 380, 760) * env(d, 0.002, 0.06) * 0.6


def dash() -> np.ndarray:
    d = 0.6
    zip_ = sweep(d, 300, 2400) * env(d, 0.005, 0.25)
    air = band(noise(d), 1500, 6000) * env(d, 0.01, 0.3)
    return zip_ * 0.6 + air


def whirl() -> np.ndarray:
    """Rising wind vortex: band noise whose centre climbs, with a fast spin flutter."""
    d = 0.45
    t = t_axis(d)
    flutter = 0.55 + 0.45 * np.sin(2 * np.pi * (8 + 30 * t / d) * t)
    rise = sweep(d, 200, 900) * 0.3
    air = band(noise(d), 300, 2600) * flutter
    return (air + rise) * env(d, 0.05, 0.6)


def slash() -> np.ndarray:
    """Clean blade: a very fast bright swish plus a short metallic ring."""
    d = 0.5
    swish = highpass(noise(d), 3500) * np.exp(-((t_axis(d) - 0.04) ** 2) / 0.0008)
    ring = tone(d, 2637, ((1, 1), (1.51, 0.5), (2.76, 0.3))) * env(d, 0.002, 0.12) * 0.5
    return swish * 1.2 + ring


def fuse() -> np.ndarray:
    d = 0.35
    hiss = highpass(noise(d), 3000) * (0.6 + 0.4 * rng.uniform(0, 1, int(d * RATE)))
    return hiss * env(d, 0.02, 0.4)


def explosion() -> np.ndarray:
    d = 1.6
    boom = lowpass(noise(d), 180, 3) * env(d, 0.002, 0.45) * 6
    crack = band(noise(d), 600, 5000) * env(d, 0.001, 0.08)
    sub = sweep(d, 70, 30) * env(d, 0.002, 0.3) * 0.8
    return boom + crack + sub


def ui_click() -> np.ndarray:
    d = 0.08
    return tone(d, 1400, ((1, 1), (2, 0.3))) * env(d, 0.001, 0.015)


def ui_hover() -> np.ndarray:
    d = 0.06
    return tone(d, 900) * env(d, 0.002, 0.015) * 0.5


def whoosh_ui() -> np.ndarray:
    d = 0.4
    shape = np.sin(np.pi * t_axis(d) / d) ** 2
    return band(noise(d), 400, 2500) * shape


def star() -> np.ndarray:
    d = 0.9
    bell = tone(d, 1046.5, ((1, 1), (2, 0.4), (3, 0.2), (4.2, 0.1))) * env(d, 0.002, 0.3)
    return bell + highpass(noise(d), 6000) * env(d, 0.001, 0.05) * 0.3


def score_tick() -> np.ndarray:
    d = 0.05
    return tone(d, 1800) * env(d, 0.001, 0.01)


def jingle(notes: list[tuple[float, float]], wave_partials, step: float) -> np.ndarray:
    total = step * len(notes) + 0.8
    out = np.zeros(int(total * RATE))
    for i, (freq, length) in enumerate(notes):
        note = tone(length, freq, wave_partials) * env(length, 0.01, length * 0.5)
        out = place(out, note, i * step)
    return out


def win() -> np.ndarray:
    c, e, g, c2 = 523.25, 659.25, 783.99, 1046.5
    return jingle([(c, 0.25), (e, 0.25), (g, 0.25), (c2, 0.9)], ((1, 1), (2, 0.35), (3, 0.15)), 0.14)


def lose() -> np.ndarray:
    return jingle([(392.0, 0.35), (349.2, 0.35), (311.1, 0.35), (261.6, 1.0)], ((1, 1), (3, 0.2)), 0.3)


def ambience() -> np.ndarray:
    """12 s loopable meadow bed: soft wind plus a few distant chirps (22.05 kHz)."""
    d = 12.0
    wind = lowpass(noise(d), 500, 2)
    swell = 0.6 + 0.4 * np.sin(2 * np.pi * t_axis(d) / d)  # one full cycle so it loops cleanly
    out = wind * swell * 2.5
    for at in (1.3, 1.45, 4.8, 7.2, 7.32, 10.1):
        chirp = sweep(0.09, 2800, 4200) * env(0.09, 0.005, 0.03) * 0.25
        out = place(out, chirp, at)
    edge = int(0.5 * RATE)
    out[:edge] *= np.linspace(0, 1, edge)  # gentle crossfade points for the loop seam
    out[-edge:] *= np.linspace(1, 0, edge)
    return out[::2]


SOUNDS = {
    "sling_stretch": sling_stretch, "sling_release": sling_release,
    "wood_hit": wood_hit, "wood_break": wood_break,
    "glass_hit": glass_hit, "glass_break": glass_break,
    "stone_hit": stone_hit, "stone_break": stone_break,
    "pig_pop": pig_pop, "bird_thud": bird_thud, "bird_hop": bird_hop,
    "dash": dash, "whirl": whirl, "slash": slash, "fuse": fuse, "explosion": explosion,
    "ui_click": ui_click, "ui_hover": ui_hover, "whoosh": whoosh_ui,
    "star": star, "score_tick": score_tick, "win": win, "lose": lose,
}


def main() -> int:
    for name, make in SOUNDS.items():
        save(name, make())
    save("ambience", ambience(), peak=0.35, rate=RATE // 2)
    return 0


if __name__ == "__main__":
    sys.exit(main())
