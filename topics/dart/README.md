# Dart Refresh for Experienced Flutter Developers

Use this folder when your Dart is rusty but you do not need a full Flutter
relearn.

This track is mainly about reopening the language changes that matter most if
you last used Dart before the recent Dart 3 era.

Why this folder matters:

- Dart changes affect how you model state and control flow in Flutter code
- reopening the language first makes the wider Flutter refresh much lighter

## Focus

- modern value modeling
- pattern matching and destructuring
- sealed-state design
- language features that change day-to-day Flutter code
- async, null-safety, tooling, and boundary habits that make Dart feel current again

Working style:

- refresh the language before reopening wider Flutter framework concerns
- focus on features that change how you model state and control flow
- keep the material practical for engineers who already know Flutter app structure

## Recommended Order

1. [01-dart3-features.md](./01-dart3-features.md): records, patterns, sealed classes, and class modifiers
2. [02-async-null-safety-and-collections.md](./02-async-null-safety-and-collections.md): async flow, streams, null safety, and collection shaping
3. [03-tooling-json-and-boundaries.md](./03-tooling-json-and-boundaries.md): `dart pub`, analyzer, tests, JSON boundaries, and extension types

## Related Reading

- [../flutter/README.md](../flutter/README.md): continue here when you want the wider Flutter architecture, auth, mobile-boundary, and testing refresh

## Core Rule

- refresh the language first, then decide whether any broader Flutter refresh is needed
- focus on modeling and control flow changes before UI framework details
- treat async, null safety, and typed boundaries as part of the language refresh, not optional extras
