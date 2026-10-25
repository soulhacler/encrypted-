# ENCRYPTED

**The phonetic code-breaking party game for Android.**

Every card shows a string of real words that look like nonsense. Say them out loud, quickly, and they turn into a familiar phrase, person, place, film or song.

> “MISSED A WEEP HEAR EYES GRIM” → **Mr Whippy ice cream**
> “OLIVE YEW” → **I love you**
> “HITS RAIN INK AT SAND DOGS” → **It's raining cats and dogs**

One phone, no internet, no accounts, no adverts. Hand it round and start shouting.

> **Proprietary software. All rights reserved.** This repository is not open source and no licence is granted to copy, modify or redistribute it.

---

## Contents

- [Features](#features)
- [How to play](#how-to-play)
- [Install the test build](#install-the-test-build)
- [Building from source](#building-from-source)
- [Testing](#testing)
- [Project structure](#project-structure)
- [Architecture](#architecture)
- [The puzzle database](#the-puzzle-database)
- [Adding content and future premium packs](#adding-content-and-future-premium-packs)
- [Sound effects](#sound-effects)
- [Releasing to Google Play](#releasing-to-google-play)

## Features

- **538 hand-written puzzles** across eight categories, written for British English pronunciation:
  Everyday Phrases, Films & TV, Music, Famous People, Food & Drink, Places, Sports and Animals, plus **Random Mix**.
- **Easy, Medium and Hard** difficulty, or Mixed.
- **Four game modes**: Classic, Team Battle, Pass & Play and Party.
- **Round timer**: 30, 60 (default), 90 or 120 seconds, or a custom length from 10 seconds to 5 minutes.
- **No repeats**: a card is never dealt twice in a game, and cards you've already seen are held back for later games in the same session.
- **Scoring**: correct +1, wrong 0, skip 0. Live score, per-turn summaries, a running scoreboard, final standings, rematches, and a saved best score for Classic.
- **Interruption-safe clock**: the timer measures real elapsed time, pauses automatically on calls, notifications or leaving the app, and is unaffected by animations or screen rotation.
- **Polish**: animated splash, decrypting text effects, card transitions, a pulsing countdown ring, an animated answer reveal, confetti for the winner, synthesised sound effects and light haptics. Every effect can be turned off.
- **Offline and private**: the only permission is `VIBRATE`. No network, analytics, ads, accounts or purchases.
- Android 9 (API 28) and newer. Works in portrait and landscape, on phones and tablets.

## How to play

1. Choose a game mode, then a category, difficulty and round length.
2. Hand the phone to whoever is up and press **Start**. After **3, 2, 1, GO!** the first card appears.
3. Read the encrypted phrase aloud, again and again, faster and faster, trying different stresses until the real phrase jumps out.
4. Tap **Reveal answer**, then **Correct** or **Wrong**. Or tap **Skip** to pass on a card without revealing it.
5. The next card appears straight away. Keep going until time runs out.
6. If the buzzer goes while an answer is showing, you still get to say whether you got it.

There is no speech recognition: the players are the judges.

### Game modes

| Mode | Players | How it works |
| --- | --- | --- |
| **Classic** | 1 | One timed round. Solve as many as you can. Your best score for each round length is saved. |
| **Team Battle** | 2–6 teams | Teams alternate rounds (default 3 each). One team member reads aloud and teammates decode. Highest total wins. |
| **Pass & Play** | 2–12 players | Each player takes a timed turn on the same phone (1–5 rounds each). Individual scores. |
| **Party** | Everyone | No clock and no pressure. Reveal, then **Got it** or **Next**, for as long as you like. Shows a casual count of codes cracked. |

## Install the test build

Every push to GitHub builds a debug APK.

1. Open the **Actions** tab, pick the latest successful **Build** run, and download the `ENCRYPTED-debug-apk` artifact (a zip containing `ENCRYPTED-debug.apk`).
   Pushes to the default branch also publish the APK to the **latest** pre-release on the **Releases** page, which is easier to download on a phone.
2. Copy the APK to your phone (or download it there directly) and open it. Allow installs from that source when Android asks.
3. Open **ENCRYPTED**.

The debug build installs as `io.github.soulhacler.encrypted.debug`, so it can sit alongside a future Play Store install.

## Building from source

### Requirements

- JDK 17 or newer
- Android SDK with platform 35 (Android Studio Ladybug or newer installs this for you)

### Android Studio

Open the project folder, let Gradle sync, then press **Run** with a phone connected (USB debugging on) or an emulator running.

### Command line

```bash
# Point Gradle at your Android SDK if ANDROID_HOME isn't set
echo "sdk.dir=$HOME/Android/Sdk" > local.properties

./gradlew assembleDebug            # app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Engine only (no Android SDK needed)

The game engine is plain Kotlin. On a machine without the Android SDK or Google's Maven repository you can still build and test it:

```bash
./gradlew -PcoreOnly=true :core:test
```

## Testing

```bash
./gradlew :core:test                 # engine and puzzle database tests
./gradlew :core:test testDebugUnitTest
```

The tests in `core/src/test` cover:

| Area | Test class |
| --- | --- |
| Timer behaviour: real-time countdown, pause and resume, expiry, never negative | `RoundTimerTest` |
| Scoring: points per outcome, totals | `ScoringTest` |
| Puzzle selection: shuffling, category and difficulty filters, premium packs | `PuzzleDeckTest`, `PuzzleLibraryTest` |
| Duplicate prevention: no repeats in a game, seen cards held back, safe reshuffles | `PuzzleDeckTest`, `GameSessionTest` |
| Round progression: reveal, judge, skip, time running out mid-answer, game over | `GameSessionTest` |
| Team switching: turn order, rounds, per-team scores, standings and ties | `GameSessionTest` |
| Puzzle database: valid JSON, unique IDs, no answer given away, category and difficulty coverage | `PuzzleDatabaseTest` |

CI (`.github/workflows/build.yml`) runs the tests and builds the debug APK on every push.

## Project structure

```
core/                                   Pure Kotlin game engine (no Android)
└── src/main/kotlin/.../core/
    ├── model/       Puzzle, PuzzlePack, Difficulty, Category
    ├── engine/      GameSession, GameConfig, GameMode, RoundTimer, PuzzleDeck, Scoring
    └── data/        PuzzleLibrary, Entitlements, PuzzlePackParser (JSON + validation)

app/                                    Android app (Jetpack Compose, Material 3)
└── src/main/
    ├── assets/packs/core.json          The puzzle database
    ├── res/raw/                        Sound effects (generated)
    └── java/.../encrypted/
        ├── MainActivity.kt, EncryptedApplication.kt
        ├── data/        PuzzleRepository (assets), SettingsRepository (DataStore)
        ├── feedback/    SoundEffects (SoundPool), Haptics (Vibrator)
        ├── game/        GameViewModel, UI state models
        └── ui/          Theme, components (cipher background, decrypt text, neon buttons,
                         timer ring, confetti) and one file per screen

tools/generate_sounds.py                Synthesises the sound effects
docs/PLAY_STORE_RELEASE.md              Store release checklist
```

## Architecture

**MVVM with a framework-free core.**

- `:core` holds all the rules. `GameSession` is a small state machine (`READY → PLAYING → TURN_OVER → … → GAME_OVER`) that deals cards from a `PuzzleDeck`, records `CORRECT`/`INCORRECT`/`SKIPPED` outcomes, rotates participants and computes standings. It knows nothing about time, sound or Android, which makes it fully unit-testable.
- `RoundTimer` measures elapsed time from an injected monotonic `Clock`. The UI polls it, but the remaining time always comes from the clock, so slow frames or long animations never stretch a round. Pausing banks the elapsed time and resuming carries on from there.
- `GameViewModel` (an `AndroidViewModel`) connects the engine to the timer, sounds, haptics and settings, and exposes immutable snapshots (`PlayState`, `TurnSummary`, `GameResults`) as Compose state. The clock value has its own state, so only the timer ring redraws on each tick.
- The UI is single-activity Jetpack Compose. Navigation is a simple `Screen` enum with animated transitions. The activity's `ON_PAUSE` lifecycle event pauses the game unless the activity is only rotating.
- Settings, remembered team and player names, and Classic best scores are stored in Preferences **DataStore**.
- Puzzles are local **JSON** packs in `assets/packs/`, parsed with kotlinx.serialization.

## The puzzle database

`app/src/main/assets/packs/core.json`:

```json
{
  "id": "core",
  "name": "ENCRYPTED: Core Pack",
  "premium": false,
  "version": 1,
  "puzzles": [
    {"id": "FOD-056", "encrypted": "Missed a weep hear eyes grim", "answer": "Mr Whippy ice cream", "category": "food", "difficulty": "hard"}
  ]
}
```

| Field | Notes |
| --- | --- |
| `id` | Unique and permanent across all packs. Prefixes: `EVD` everyday, `FTV` films & TV, `MUS` music, `PPL` people, `FOD` food, `PLC` places, `SPT` sports, `ANM` animals. Never reuse an ID. |
| `encrypted` | Real words that, said aloud quickly, sound like the answer. At most 60 characters. |
| `answer` | The decoded phrase as it should be displayed. |
| `category` | A built-in category ID, or one declared in the pack's `categories`. |
| `difficulty` | `easy`, `medium` or `hard`. |

Current contents: 538 puzzles (290 easy, 195 medium, 53 hard).

### Writing good puzzles

Quality matters more than quantity. Before adding a puzzle, **say it out loud, quickly, in a British accent**:

- It must genuinely sound like the answer. If you have to squint, cut it.
- Use real words (names are fine) so it reads as nonsense on the page.
- Lean on British pronunciation: non-rhotic "r" (*water* → "wor-tuh", *Leicester* → "Less tah"), dropped "h" (*Hits rain…*), "-er" as "-ah".
- Re-splitting syllables across word boundaries is the heart of the game (*Head dover heels*). Give it a lower difficulty if the original words are still easy to see.
- Keep it family-friendly. Avoid slurs, stereotypes and anything that reads as a dig at a group.
- Avoid divisive current politicians.

`PuzzleDatabaseTest` rejects duplicate IDs, duplicate answers or phrases, unknown categories, stray whitespace, phrases that are just the answer with the spaces moved, packs under 300 free puzzles, and thin categories or difficulties.

## Adding content and future premium packs

The app is built so new content needs no code changes:

- **More puzzles**: add to `core.json`, or drop another pack file into `assets/packs/`. Every JSON file there is loaded at startup.
- **New categories** (Christmas, custom categories and so on): list them in the pack's `categories` array (`{"id": "christmas", "name": "Christmas", "icon": "🎄"}`). They appear on the setup screen automatically once they have playable puzzles.
- **Premium packs** (extra packs, adult party packs, seasonal editions): set `"premium": true`. `PuzzleLibrary` only offers packs that its `Entitlements` unlocks. Version 1 uses `Entitlements.FreeOnly`. A future billing integration would supply an `Entitlements` backed by Google Play Billing purchases, passed to `PuzzleRepository` in `AppContainer`. Locked packs are already parsed, so a store screen can list them.
- **Special challenge modes**: add a `GameMode` entry. Its limits (participants, rounds, timed or not, competitive or not) drive both validation and the setup UI.

No purchase code is included in version 1.

## Sound effects

All sounds are synthesised from scratch by `tools/generate_sounds.py` (tick, final tick, go, reveal, correct, incorrect, skip, time up, victory), so there is no third-party audio to license. Edit the script and run `python3 tools/generate_sounds.py` to regenerate `app/src/main/res/raw/`.

## Releasing to Google Play

See **[docs/PLAY_STORE_RELEASE.md](docs/PLAY_STORE_RELEASE.md)** for the full checklist: package name, signing, building an App Bundle, store listing, content rating, Data safety and staged rollout.

---

© 2026. All rights reserved.
