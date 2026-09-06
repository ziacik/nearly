# Nearly

**Nearly: Find Phone & Watch** is a local-only Android + Wear OS app for finding a nearby paired device without making it ring.

Instead of waking the whole room, Nearly can make the target vibrate, glow when its app is already visible, and guide you with relative BLE signal strength.

## MVP

- Find a paired Android phone from Wear OS.
- Find a paired Wear OS watch from Android.
- Silent target modes: **Glow**, **Vibrate**, or **Both**.
- **Cold → Warmer → Hot → Very close** proximity guidance from BLE RSSI.
- Haptic guidance on the searching device gets faster as the target gets closer.
- Local control uses the Wear OS Data Layer and only accepts a directly nearby node.
- BLE proximity uses a random per-search token and an exact service-data filter.
- Searches stop automatically after 120 seconds.
- No account, backend, map, location history, analytics, or internet connection.

RSSI is deliberately presented as relative proximity, never as an estimated distance in meters.

## Modules

- `mobile` — Android phone app.
- `wear` — Wear OS app.
- `shared` — wire protocol, RSSI smoothing, proximity thresholds, common UI state.

Both application modules use the same application ID, `sk.ziacik.nearly`, which is required for Wear Data Layer communication between the phone and watch apps.

## Build

Requirements:

- JDK 17
- Android SDK 36

```bash
./gradlew \
	:shared:testDebugUnitTest \
	:mobile:testDebugUnitTest \
	:wear:testDebugUnitTest \
	:mobile:assembleDebug \
	:wear:assembleDebug
```

Debug APKs are produced under:

```text
mobile/build/outputs/apk/debug/mobile-debug.apk
wear/build/outputs/apk/debug/wear-debug.apk
```

## Permissions

On Android 12 / API 31 and newer, Nearly requests the Nearby Devices Bluetooth permissions needed for scan, advertising, and connection. The BLE scan is declared with `neverForLocation`; Nearly does not derive physical location from scan results.

On API 30 and older, Android requires fine location permission for BLE scanning even though Nearly itself does not use GPS location.

BLE hardware is optional. If proximity is unsupported or permission is denied, the silent find session can still work without hot/cold guidance.

## Manual test matrix

1. Install the mobile APK on an Android phone.
2. Install the Wear APK on its paired Wear OS watch.
3. Open both apps once and grant requested Nearby Devices permissions. On API 30 and below, grant location for BLE scanning.
4. On the watch, tap **Find phone**. Verify that the phone vibrates silently.
5. Walk toward and away from the phone. Verify **Cold / Warmer / Hot / Very close** changes and the watch's guidance ticks get faster when closer.
6. Switch the target between **Glow / Vibrate / Both** while the search is active.
7. Tap **Found it**. Verify vibration, BLE advertising, scanning, and guidance stop.
8. Repeat the same flow from the phone with **Find watch**.
9. Disable Bluetooth. Verify the find session degrades gracefully instead of crashing.
10. Deny BLE scan/advertise permissions. Verify Nearly explains that hot/cold is unavailable while preserving the silent-find fallback where possible.
11. Leave a search active. At exactly 120 seconds it should clean itself up and show a timeout state.
12. Lock/background the target. Verify Nearly does not force-launch an activity. Vibration remains the reliable background cue; Glow is best-effort and only renders when the app is already visible.

## RSSI tuning

The initial thresholds are:

```text
Cold       < -75 dBm
Warmer    >= -75 dBm
Hot       >= -62 dBm
Very close >= -50 dBm
```

RSSI varies substantially with device radios, body position, walls, furniture, and orientation. These values are intentionally centralized and covered by unit tests so they can be calibrated after real-device testing without changing the rest of the design.

## Privacy

Nearly has no server component and does not request the `INTERNET` permission. The MVP does not store search history or location data.
