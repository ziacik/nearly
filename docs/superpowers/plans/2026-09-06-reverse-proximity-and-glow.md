# Reverse Proximity and Glow Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make hot/cold guidance work when the watch searches for the phone, and make phone Glow actually present a bright screen, including best-effort lock-screen wake/show behavior allowed by Android.

**Architecture:** Keep the already-working phone-searches-watch BLE direction unchanged. For watch-searches-phone, the watch advertises the session token, the phone scans that advertisement, and the phone returns RSSI samples over the existing Wear MessageClient transport. Glow remains policy-safe: an incoming Glow/Both cue attempts to bring `MainActivity` forward; when active, the activity uses Android's show-when-locked/turn-screen-on APIs, maximum window brightness, and keep-screen-on. Background activity launch remains best-effort because modern Android may block it.

**Tech Stack:** Kotlin, Android/Wear OS, Google Play Services Wearable MessageClient, BLE advertising/scanning, Jetpack Compose, coroutines/Flow, JUnit.

**Spec:** `docs/superpowers/specs/2026-09-06-nearly-mvp-design.md`

## Global Constraints

- Local-only; no Internet/cloud transport.
- Search timeout remains exactly `120_000L` ms.
- Do not use overlays, full-screen-intent abuse, alarm/call privileges, or other policy-sensitive Glow workarounds.
- Preserve phone -> watch proximity behavior.
- Android 12+ BLE runtime permissions remain SCAN/ADVERTISE/CONNECT as currently declared.

---

### Task 1: Add RSSI samples to the local wire protocol

**Files:**
- Modify: `shared/src/main/java/sk/ziacik/nearly/shared/FindCommand.kt`
- Modify: `shared/src/main/java/sk/ziacik/nearly/shared/FindProtocol.kt`
- Test: `shared/src/test/java/sk/ziacik/nearly/shared/FindProtocolTest.kt`

**Interfaces:**
- Produces: `FindCommand.ProximitySample(sessionToken: Int, rssi: Int)` and `/nearly/proximity/sample` encoding/decoding.

- [ ] **Step 1:** Add `ProximitySample(42, -73)` to the protocol round-trip test.
- [ ] **Step 2:** Run `./gradlew :shared:testDebugUnitTest` and verify the test fails because the command does not exist yet.
- [ ] **Step 3:** Add the sealed command and encode/decode it as two 32-bit integers.
- [ ] **Step 4:** Run `./gradlew :shared:testDebugUnitTest` and verify it passes.

### Task 2: Reverse only the watch -> phone proximity measurement path

**Files:**
- Modify: `mobile/src/main/java/sk/ziacik/nearly/mobile/NearlyMobileApplication.kt`
- Modify: `mobile/src/main/java/sk/ziacik/nearly/mobile/search/MobileTargetSessionController.kt`
- Modify: `mobile/src/test/java/sk/ziacik/nearly/mobile/search/MobileTargetSessionControllerTest.kt`
- Modify: `wear/src/main/java/sk/ziacik/nearly/wear/NearlyWearApplication.kt`
- Modify: `wear/src/main/java/sk/ziacik/nearly/wear/data/NearlyWearableListenerService.kt`
- Modify: `wear/src/main/java/sk/ziacik/nearly/wear/ui/WearFindViewModel.kt`
- Modify: `wear/src/main/java/sk/ziacik/nearly/wear/search/WearFindCoordinator.kt`
- Modify: `wear/src/main/java/sk/ziacik/nearly/wear/search/WearTargetSessionController.kt`
- Modify: `wear/src/test/java/sk/ziacik/nearly/wear/search/WearFindCoordinatorTest.kt`
- Modify: `wear/src/test/java/sk/ziacik/nearly/wear/search/WearTargetSessionControllerTest.kt`

**Interfaces:**
- Mobile target consumes `BleScanner`, `PeerTransport`, and `CoroutineScope`; on `StartProximity(token)` it scans and sends `ProximitySample(token, rssi)`.
- Wear application exposes a `FindCommandInbox` for received `ProximitySample`s.
- Wear search coordinator consumes a local `BleAdvertiseSession` and RSSI sample flow instead of a local scanner.

- [ ] **Step 1:** Rewrite target/coordinator tests so they require phone scanning + sample forwarding and watch local advertising + remote sample consumption.
- [ ] **Step 2:** Run mobile/wear unit tests and verify the new expectations fail.
- [ ] **Step 3:** Implement the minimal asymmetric path while leaving the phone-searches-watch path untouched.
- [ ] **Step 4:** Run `:mobile:testDebugUnitTest :wear:testDebugUnitTest :shared:testDebugUnitTest` and verify all pass.

### Task 3: Make Glow visible and bright when Android permits it

**Files:**
- Create: `mobile/src/main/java/sk/ziacik/nearly/mobile/cue/GlowLauncher.kt`
- Modify: `mobile/src/main/java/sk/ziacik/nearly/mobile/search/MobileTargetSessionController.kt`
- Modify: `mobile/src/main/java/sk/ziacik/nearly/mobile/NearlyMobileApplication.kt`
- Modify: `mobile/src/main/java/sk/ziacik/nearly/mobile/MainActivity.kt`
- Test: `mobile/src/test/java/sk/ziacik/nearly/mobile/search/MobileTargetSessionControllerTest.kt`

**Interfaces:**
- `fun interface GlowLauncher { fun show() }` with Android implementation that best-effort starts `MainActivity` using `NEW_TASK | SINGLE_TOP | CLEAR_TOP`.
- The target controller calls `show()` when a current StartFind/SetCue mode includes Glow.

- [ ] **Step 1:** Add a failing target-controller test proving Glow/Both asks the launcher to show and Vibrate does not.
- [ ] **Step 2:** Run the mobile unit test and verify failure before production changes.
- [ ] **Step 3:** Add the launcher and wire it into the mobile application.
- [ ] **Step 4:** While `glowActive`, make `MainActivity` call `setShowWhenLocked(true)`, `setTurnScreenOn(true)`, set window brightness to `1f`, and keep the screen on; restore state when Glow ends.
- [ ] **Step 5:** Run unit tests, lint, and debug assembly for both apps.

### Task 4: Final verification

- [ ] Run the repository CI-equivalent Gradle command from `.github/workflows/android-ci.yml`.
- [ ] Verify no Internet permission/client was introduced.
- [ ] Review the diff to ensure phone -> watch behavior stayed unchanged and no policy-hostile Glow mechanism was added.
- [ ] Open a PR from `fix/reverse-proximity-and-glow` to `master`.
