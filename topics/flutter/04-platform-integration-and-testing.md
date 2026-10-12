# Platform Integration and Tests in Flutter

Camera, notifications, secure storage, deep links, and other plugins cross into operating-system behavior. Give each important platform call one app-owned boundary so feature logic can handle its outcomes and tests can replace the plugin.

## Wrap the capability, not every call

A feature asks for an app-level action such as `loadSession` or `openCamera`. An adapter uses the plugin and translates success, denial, unavailability, and cancellation into outcomes the feature understands. Keep plugin types out of widgets when they would spread platform details through the flow.

Do not add a wrapper with no decision or translation just to follow a pattern. The value is a stable boundary around behavior that varies by device, permission, or plugin.

## Match tests to the possible failure

| Risk | Useful test |
| --- | --- |
| Wrong feature state after denial, timeout, or retry | State/controller test with a fake boundary |
| Wrong rendering or button behavior | Widget test |
| Broken route, session restore, or multi-screen journey | Integration test on a device or emulator |
| OS permission dialog or native UI behavior | Platform-aware test or targeted device check |

A widget test can prove that tapping a button asks the camera boundary for permission and displays the resulting state. It cannot prove how the operating system renders its permission dialog. An integration test can cover more of the app handoff, but choose an OS-aware check for the OS-owned step.

## Example: session restore

1. State test: a missing stored session leads to logged-out state.
2. Widget test: the login gate renders the correct branch.
3. Integration test: start the app with a stored session and verify the route to the signed-in area.
4. Platform check: confirm secure storage behavior on supported devices.

Each layer answers a different question. Repeating the same happy-path assertion in all layers adds cost without much new evidence.

## Review check

- Is the plugin boundary owned by the app?
- Are denial, cancellation, missing data, and plugin failure explicit?
- Does each test reach the layer where its claimed failure can occur?
- Have the highest-risk device and app flows been checked on a real target?

Further reading: [Flutter platform integration](https://docs.flutter.dev/platform-integration), [Flutter testing](https://docs.flutter.dev/testing/overview).
