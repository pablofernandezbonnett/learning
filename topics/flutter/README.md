# Flutter for Product and Backend-Aware Engineers

Use this folder when you want a practical Flutter refresh that stays connected
to API shape, auth flows, mobile constraints, and real app behavior instead of
stopping at widget syntax.

Focus:

- project structure and state boundaries
- layout, navigation, and async UI behavior
- mobile auth and API-facing judgment
- platform integration and Flutter testing layers

Working style:

- keep Flutter connected to backend contracts and product flows
- prefer concrete app examples over framework taxonomy
- explain where mobile constraints change the design, not only the code

## Recommended Order

1. [01-flutter-architecture-and-project-shape.md](./01-flutter-architecture-and-project-shape.md): how to structure Flutter code around features, state boundaries, and backend-facing contracts
2. [02-layout-navigation-and-async-ui.md](./02-layout-navigation-and-async-ui.md): how screens behave under layout constraints, navigation, loading, error, and retry states
3. [03-auth-api-and-mobile-boundaries.md](./03-auth-api-and-mobile-boundaries.md): how Flutter clients should think about tokens, mobile AppSec, and backend trust boundaries
4. [04-platform-integration-and-testing.md](./04-platform-integration-and-testing.md): platform channels, plugin boundaries, and the testing layers that catch the right failures

## Refresh

- [01-flutter-architecture-and-project-shape.md](./01-flutter-architecture-and-project-shape.md)
- [02-layout-navigation-and-async-ui.md](./02-layout-navigation-and-async-ui.md)
- [03-auth-api-and-mobile-boundaries.md](./03-auth-api-and-mobile-boundaries.md)

## Required

- [04-platform-integration-and-testing.md](./04-platform-integration-and-testing.md)

## Core Rule

- Flutter app design is still system design in miniature
- state boundaries, contract clarity, and failure handling matter more than widget trivia
- mobile constraints change the trust model, not only the UI code
