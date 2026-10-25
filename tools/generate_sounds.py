#!/usr/bin/env python3
"""Synthesises the game's sound effects into app/src/main/res/raw.

All sounds are generated from scratch here, so the app ships no third-party audio and there is
nothing to license. Run again after tweaking: python3 tools/generate_sounds.py
"""
import math
import os
import random
import struct
import wave

RATE = 22050
OUT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res", "raw")
random.seed(7)


def env(i, n, attack=0.005, release=0.08):
    t = i / RATE
    total = n / RATE
    a = min(1.0, t / attack) if attack > 0 else 1.0
    r = min(1.0, (total - t) / release) if release > 0 else 1.0
    return max(0.0, min(a, r))


def tone(freq, dur, vol=0.5, shape="sine", attack=0.005, release=0.08, glide_to=None):
    n = int(RATE * dur)
    out = []
    phase = 0.0
    for i in range(n):
        f = freq if glide_to is None else freq + (glide_to - freq) * (i / n)
        phase += 2 * math.pi * f / RATE
        if shape == "sine":
            s = math.sin(phase)
        elif shape == "soft_square":
            s = math.tanh(3 * math.sin(phase)) * 0.8
        elif shape == "tri":
            s = 2 / math.pi * math.asin(math.sin(phase))
        else:
            raise ValueError(shape)
        out.append(s * vol * env(i, n, attack, release))
    return out


def noise(dur, vol=0.3, lowpass=0.2, attack=0.01, release=0.15, sweep=None):
    n = int(RATE * dur)
    out, y = [], 0.0
    for i in range(n):
        k = lowpass if sweep is None else sweep[0] + (sweep[1] - sweep[0]) * (i / n)
        y += k * (random.uniform(-1, 1) - y)
        out.append(y * vol * env(i, n, attack, release))
    return out


def mix(*tracks):
    n = max(len(t) for t in tracks)
    return [sum(t[i] for t in tracks if i < len(t)) for i in range(n)]


def seq(*parts, gap=0.0):
    out = []
    for p in parts:
        out += p + [0.0] * int(RATE * gap)
    return out


def delay(track, seconds):
    return [0.0] * int(RATE * seconds) + track


def write(name, samples):
    peak = max(1e-9, max(abs(s) for s in samples))
    scale = min(1.0, 0.9 / peak)
    path = os.path.join(OUT, name + ".wav")
    with wave.open(path, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes(b"".join(struct.pack("<h", int(s * scale * 32767)) for s in samples))
    print(path, f"{len(samples) / RATE:.2f}s")


def chord(freqs, dur, vol=0.25, **kw):
    return mix(*(tone(f, dur, vol, **kw) for f in freqs))


os.makedirs(OUT, exist_ok=True)

# Countdown tick: a short digital blip.
write("tick", tone(1320, 0.06, 0.5, "soft_square", release=0.04))
# Final seconds: higher and brighter.
write("tick_final", mix(tone(1760, 0.09, 0.5, "soft_square", release=0.06), tone(3520, 0.05, 0.15)))
# Round start.
write("go", mix(tone(880, 0.35, 0.4, "soft_square", glide_to=1760, release=0.2), tone(1760, 0.35, 0.2, glide_to=2640)))
# Answer reveal: a rising "decrypt" shimmer.
shimmer = seq(*(tone(f, 0.045, 0.35, "tri", release=0.03) for f in [523, 659, 784, 1047, 1319, 1568]))
write("reveal", mix(shimmer, noise(0.3, 0.12, sweep=(0.05, 0.6), release=0.2)))
# Correct: bright two-note chime.
write("correct", mix(chord([1047, 1319], 0.12, 0.3, release=0.05), delay(chord([1568, 2093], 0.35, 0.3, release=0.3), 0.1)))
# Incorrect: low falling buzz.
write("incorrect", tone(220, 0.35, 0.5, "soft_square", glide_to=130, release=0.15))
# Skip: quick whoosh.
write("skip", noise(0.22, 0.6, sweep=(0.6, 0.05), attack=0.02, release=0.12))
# Time up: three alarm pulses.
pulse = mix(tone(660, 0.16, 0.45, "soft_square", release=0.04), tone(990, 0.16, 0.2, "soft_square", release=0.04))
write("time_up", seq(pulse, pulse, mix(tone(660, 0.45, 0.45, "soft_square", release=0.3), tone(990, 0.45, 0.2, "soft_square", release=0.3)), gap=0.06))
# Victory fanfare: arpeggio into a held major chord.
arp = seq(*(chord([f, f * 2], 0.11, 0.22, shape="soft_square", release=0.05) for f in [523, 659, 784]))
write("victory", seq(arp, mix(chord([523, 659, 784, 1047], 0.9, 0.18, shape="soft_square", release=0.6), noise(0.9, 0.04, release=0.6))))
