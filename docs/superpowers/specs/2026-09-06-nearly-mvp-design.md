# Nearly MVP Design

## Product

**Store name:** Nearly: Find Phone & Watch

Nearly is a local Android + Wear OS companion app for finding a nearby phone or watch quietly, especially at night. It complements loud system find-device features with silent cues and a relative proximity mode.

## MVP Scope

The MVP is local-only. It has no account, cloud backend, history, remote internet lookup, or device map.

Both directions are supported:

- Wear OS watch finds Android phone.
- Android phone finds Wear OS watch.

The reference devices during development are a Moto G31 and Galaxy Watch FE, but the app must target generic Android + Wear OS devices and feature-detect unavailable Bluetooth capabilities.

## User Flow

The home screen has one primary action: **Find phone** on Wear OS and **Find watch** on Android.

Starting a search opens a finding screen with:

- Relative state: **Cold**, **Warmer**, **Hot**, **Very close**.
- A simple signal-strength indicator.
- Haptic feedback on the searching device that becomes more frequent as proximity increases.
- Target cue controls: **Glow**, **Vibrate**, **Both**.
- A large **Found it / Stop** action.

Search ends automatically after 2 minutes.

If proximity is unsupported or Bluetooth is unavailable, the app keeps silent find controls where possible and hides the hot/cold UI.

## Architecture

Single repository with three Gradle modules:

```text
nearly
├── mobile
├── wear
└── shared
```

- `mobile`: Android phone UI, phone-side Data Layer integration, phone BLE advertising/scanning, and phone cue execution.
- `wear`: Wear OS UI, watch-side Data Layer integration, watch BLE advertising/scanning, and watch cue execution.
- `shared`: Shared protocol models, proximity smoothing/state mapping, timeout constants, and platform-independent tests.

Use Kotlin, Jetpack Compose, Material 3, Java 17, `compileSdk = 36`, and `targetSdk = 36`, following the style of the user's existing Android projects. Package root: `sk.ziacik.nearly`.

## Device Communication

Wear OS Data Layer is used for control messages between paired phone/watch apps.

Initial protocol:

```text
START_FIND
STOP_FIND
START_PROXIMITY
STOP_PROXIMITY
```

Messages must be small and idempotent. Duplicate START/STOP messages must not create duplicate scan, advertise, vibration, or timeout jobs.

The MVP treats a direct nearby paired node as the eligible target for local finding. It does not intentionally provide an internet-based remote-find mode.

## Hot/Cold Proximity

Relative proximity uses temporary BLE advertising on the target and BLE scanning on the searching device.

The searching device reads RSSI samples and passes them through a smoothing layer before mapping them to one of four states:

```text
Cold → Warmer → Hot → Very close
```

The app must not show estimated meters because RSSI varies heavily with orientation, body obstruction, furniture, walls, antenna design, and device power.

The first implementation should use a simple, testable smoothing algorithm such as an exponential moving average, with thresholds kept in one shared configuration object so they can be tuned from real-device testing.

Haptic cadence is derived from the smoothed proximity state. It should become progressively faster toward **Very close**, without producing continuous vibration.

If BLE advertising or scanning is unsupported, blocked, or unavailable, proximity mode is disabled gracefully rather than failing the search.

## Silent Target Cues

Three user-selectable cue modes:

- **Glow**: illuminate/show a highly visible screen when Android/Wear OS permits it without policy-hostile workarounds.
- **Vibrate**: run a recognizable repeating vibration pattern.
- **Both**: use both available cues.

Glow is best-effort. The MVP must not depend on abusing full-screen intents, overlays, alarm/call privileges, or other policy-sensitive mechanisms just to wake a locked phone display from the background.

Vibration is therefore the reliable silent fallback.

Stopping a search immediately stops active vibration, BLE scanning, BLE advertising, and any target UI/cue state.

## Permissions and Privacy

Request only capabilities required for Bluetooth nearby-device communication, scanning/advertising where applicable, and vibration.

Do not request account access, contacts, location history, internet-backed tracking, or unrelated permissions. Avoid location permission on Android versions where Nearby Devices/Bluetooth permissions are sufficient.

All search state is ephemeral and local to the phone/watch pair.

## Error and Capability Handling

The UI must distinguish at least:

- counterpart not connected/available,
- Bluetooth disabled,
- required Bluetooth capability unavailable,
- missing permission,
- search timed out.

Failures in the proximity feature must not crash the app or prevent whatever silent cue remains available.

## Testing

Unit tests in `shared` cover:

- RSSI smoothing,
- proximity threshold mapping,
- haptic cadence mapping,
- protocol model/command handling,
- timeout-related state transitions where platform-independent.

Platform tests cover the core Compose screens and permission/capability-driven UI states for `mobile` and `wear`.

Real-device testing is required for tuning RSSI thresholds because emulators cannot validate meaningful radio proximity behavior.

## CI

GitHub Actions must build both Android modules and run unit tests on pushes and pull requests.

## Non-goals for MVP

- Internet/cloud finding.
- Accounts or authentication.
- GPS maps or location history.
- UWB direction/distance.
- Multiple watches/phones management UI.
- Background continuous proximity monitoring.
- Loud ringtone escalation.
- Settings/history screens beyond what the MVP needs.

## Future Options

Potential later additions include UWB-assisted direction/distance on supported hardware, internet-assisted remote finding, configurable cue patterns, a quick-settings tile/complication, and support for multiple paired devices.
