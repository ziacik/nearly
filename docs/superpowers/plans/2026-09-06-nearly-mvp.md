# Nearly MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a local-only Android + Wear OS app that can silently find a paired phone or watch using vibration, best-effort glow, and BLE RSSI-based hot/cold proximity in both directions.

**Architecture:** A three-module Gradle project (`mobile`, `wear`, `shared`). Wear OS Data Layer carries idempotent control commands between directly nearby paired nodes; the target temporarily advertises a per-search BLE session token and the searching device scans it for RSSI samples. Platform-independent protocol/proximity logic lives in `shared`; Android/Wear-specific Data Layer, BLE, haptic, permission, cue, and Compose code stays in the corresponding app module.

**Tech Stack:** Kotlin 2.3.21, AGP 9.3.1, Gradle 9.5.0, Java 17, compileSdk/targetSdk 36, Compose BOM 2026.06.00, Activity Compose 1.13.0, Lifecycle 2.11.0, Wear Compose 1.6.2, Google Play Services Wearable 20.0.1, kotlinx-coroutines 1.10.2, JUnit 4.13.2.

**Spec:** `docs/superpowers/specs/2026-09-06-nearly-mvp-design.md`

## Global Constraints

- Repository default development branch: `master`.
- Package root: `sk.ziacik.nearly`.
- Modules: exactly `mobile`, `wear`, `shared` for the MVP.
- `compileSdk = 36`, `targetSdk = 36`, Java 17.
- `mobile` minSdk = 26; `wear` minSdk = 30; `shared` minSdk = 26.
- No account, backend, GPS tracking, map, history, UWB, loud-ringtone escalation, or continuous background proximity monitoring.
- Local finding targets only a directly nearby Wear Data Layer node (`Node.isNearby == true`).
- BLE proximity is feature-detected and may fail independently without breaking silent cue functionality.
- RSSI is relative only; never expose estimated meters.
- Search timeout is exactly `120_000L` ms.
- Glow is best-effort only; do not use overlays, full-screen-intent abuse, alarm/call privileges, or other policy-sensitive workarounds.
- On Android 12+ request Nearby Devices Bluetooth permissions as needed. On API 30 and below, request location only when required by the platform for BLE scanning.
- Use tabs for source-code indentation where the formatter/configuration permits; do not introduce spaces-only project-specific style rules.

---

## File Map

### Root/build
- `settings.gradle.kts` — repositories and module includes.
- `build.gradle.kts` — root plugin declarations.
- `gradle.properties` — standard AndroidX/Gradle settings.
- `gradle/libs.versions.toml` — all dependency/plugin versions.
- `gradle/wrapper/gradle-wrapper.properties`, `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar` — Gradle 9.5.0 wrapper.
- `.gitignore` — Android/Gradle/IDE outputs.
- `.github/workflows/android-ci.yml` — build, lint, unit tests, and Android test APK compilation.

### shared
- `shared/build.gradle.kts` — Android library configuration.
- `shared/src/main/AndroidManifest.xml` — empty library manifest.
- `shared/src/main/java/sk/ziacik/nearly/shared/protocol/FindCommand.kt` — command model.
- `shared/src/main/java/sk/ziacik/nearly/shared/protocol/FindProtocol.kt` — Data Layer paths and payload codec.
- `shared/src/main/java/sk/ziacik/nearly/shared/proximity/Proximity.kt` — RSSI smoothing, thresholds, and haptic cadence.
- `shared/src/main/java/sk/ziacik/nearly/shared/search/SearchState.kt` — common search state and timeout helpers.
- Matching unit tests under `shared/src/test/...`.

### mobile
- `mobile/build.gradle.kts`, `mobile/src/main/AndroidManifest.xml` — phone app setup and permissions.
- `mobile/src/main/java/sk/ziacik/nearly/mobile/NearlyMobileApplication.kt` — process-scoped target controller wiring.
- `mobile/src/main/java/sk/ziacik/nearly/mobile/MainActivity.kt` — Compose host.
- `mobile/src/main/java/sk/ziacik/nearly/mobile/data/WearDataLayerTransport.kt` — nearby-node lookup and command send.
- `mobile/src/main/java/sk/ziacik/nearly/mobile/data/MobileWearListenerService.kt` — incoming commands.
- `mobile/src/main/java/sk/ziacik/nearly/mobile/proximity/AndroidBleAdvertiser.kt` — target BLE advertising.
- `mobile/src/main/java/sk/ziacik/nearly/mobile/proximity/AndroidBleScanner.kt` — searching-device RSSI scan.
- `mobile/src/main/java/sk/ziacik/nearly/mobile/cue/MobileCueController.kt` — vibration and foreground glow state.
- `mobile/src/main/java/sk/ziacik/nearly/mobile/search/MobileFindCoordinator.kt` — search/target orchestration.
- `mobile/src/main/java/sk/ziacik/nearly/mobile/ui/MobileApp.kt` — home/search UI.
- `mobile/src/main/java/sk/ziacik/nearly/mobile/ui/MobileFindViewModel.kt` — UI state and actions.
- `mobile/src/main/java/sk/ziacik/nearly/mobile/permissions/BluetoothPermissionState.kt` — version-aware permission checks.
- Tests under `mobile/src/test/...` and `mobile/src/androidTest/...`.

### wear
- Same responsibility split as mobile, under `wear/src/main/java/sk/ziacik/nearly/wear/...`, using Wear Compose Material 3 and watch-specific UI/haptics.

---

### Task 1: Scaffold the three-module project and prove it builds

**Files:**
- Create: root Gradle files, wrapper files, `.gitignore`
- Create: `mobile/build.gradle.kts`, `wear/build.gradle.kts`, `shared/build.gradle.kts`
- Create: minimal manifests and `MainActivity.kt` files for `mobile` and `wear`
- Create: minimal `shared/src/main/AndroidManifest.xml`

**Interfaces:**
- Produces buildable modules `:mobile`, `:wear`, `:shared` used by every later task.

- [ ] **Step 1: Add the version catalog and root plugin aliases**

Use these versions in `gradle/libs.versions.toml`:

```toml
[versions]
agp = "9.3.1"
kotlin = "2.3.21"
composeBom = "2026.06.00"
activityCompose = "1.13.0"
lifecycle = "2.11.0"
wearCompose = "1.6.2"
playServicesWearable = "20.0.1"
coroutines = "1.10.2"
junit = "4.13.2"
androidxTestRunner = "1.7.0"

[libraries]
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
androidx-compose-foundation = { module = "androidx.compose.foundation:foundation" }
androidx-compose-material3 = { module = "androidx.compose.material3:material3" }
androidx-compose-ui = { module = "androidx.compose.ui:ui" }
androidx-compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
androidx-compose-ui-test-junit4 = { module = "androidx.compose.ui:ui-test-junit4" }
androidx-compose-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest" }
androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-wear-compose-foundation = { module = "androidx.wear.compose:compose-foundation", version.ref = "wearCompose" }
androidx-wear-compose-material3 = { module = "androidx.wear.compose:compose-material3", version.ref = "wearCompose" }
play-services-wearable = { module = "com.google.android.gms:play-services-wearable", version.ref = "playServicesWearable" }
kotlinx-coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-play-services = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services", version.ref = "coroutines" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
junit = { module = "junit:junit", version.ref = "junit" }
androidx-test-runner = { module = "androidx.test:runner", version.ref = "androidxTestRunner" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

- [ ] **Step 2: Configure module includes and Gradle wrapper**

`settings.gradle.kts` must include:

```kotlin
rootProject.name = "Nearly"
include(":mobile")
include(":wear")
include(":shared")
```

Wrapper distribution URL must be:

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-9.5.0-bin.zip
```

- [ ] **Step 3: Configure Android modules**

Use namespaces/application IDs:

```text
mobile: sk.ziacik.nearly.mobile
wear:   sk.ziacik.nearly.wear
shared: sk.ziacik.nearly.shared
```

Both app modules depend on `project(":shared")`; both enable Compose; all modules use Java 17.

- [ ] **Step 4: Add minimal launcher activities and manifests**

Phone manifest launches `MainActivity`. Wear manifest additionally declares:

```xml
<uses-feature android:name="android.hardware.type.watch" />
```

Each activity should initially render only `Text("Nearly")` so scaffolding stays independent from later UI work.

- [ ] **Step 5: Run the first build**

Run:

```bash
./gradlew :shared:testDebugUnitTest :mobile:assembleDebug :wear:assembleDebug --stacktrace
```

Expected: all three modules build successfully.

- [ ] **Step 6: Commit**

```bash
git add .
git commit -m "feat: scaffold Nearly Android and Wear OS apps"
```

---

### Task 2: Implement and test the shared protocol and proximity core

**Files:**
- Create: `shared/src/main/java/sk/ziacik/nearly/shared/protocol/FindCommand.kt`
- Create: `shared/src/main/java/sk/ziacik/nearly/shared/protocol/FindProtocol.kt`
- Create: `shared/src/main/java/sk/ziacik/nearly/shared/proximity/Proximity.kt`
- Create: `shared/src/main/java/sk/ziacik/nearly/shared/search/SearchState.kt`
- Test: matching files under `shared/src/test/...`

**Interfaces:**
- Produces `CueMode`, `FindCommand`, `FindProtocol`, `RssiSmoother`, `ProximityLevel`, `ProximityConfig`, `hapticIntervalMs`, `SearchPhase`, and `SEARCH_TIMEOUT_MS`.

- [ ] **Step 1: Write failing protocol tests**

Cover round trips for these commands:

```kotlin
sealed interface FindCommand {
	data class StartFind(val sessionToken: Int, val cueMode: CueMode) : FindCommand
	data class StopFind(val sessionToken: Int) : FindCommand
	data class StartProximity(val sessionToken: Int) : FindCommand
	data class StopProximity(val sessionToken: Int) : FindCommand
	data class SetCue(val sessionToken: Int, val cueMode: CueMode) : FindCommand
}

enum class CueMode { GLOW, VIBRATE, BOTH }
```

The codec exposes:

```kotlin
data class EncodedCommand(val path: String, val payload: ByteArray)

object FindProtocol {
	fun encode(command: FindCommand): EncodedCommand
	fun decode(path: String, payload: ByteArray): FindCommand?
}
```

Paths are exactly:

```text
/nearly/find/start
/nearly/find/stop
/nearly/proximity/start
/nearly/proximity/stop
/nearly/cue/set
```

- [ ] **Step 2: Run protocol tests and verify failure**

```bash
./gradlew :shared:testDebugUnitTest --tests '*FindProtocolTest' --stacktrace
```

Expected: FAIL because protocol types do not exist yet.

- [ ] **Step 3: Implement the minimal codec**

Encode the session token as a big-endian 4-byte integer. Commands carrying `CueMode` append one byte: `0 = GLOW`, `1 = VIBRATE`, `2 = BOTH`. Unknown paths, malformed payload lengths, and invalid cue bytes return `null`.

- [ ] **Step 4: Write failing proximity tests**

Use exactly this initial configuration:

```kotlin
data class ProximityConfig(
	val alpha: Double = 0.25,
	val warmerThresholdDbm: Double = -75.0,
	val hotThresholdDbm: Double = -62.0,
	val veryCloseThresholdDbm: Double = -50.0,
)

enum class ProximityLevel { COLD, WARMER, HOT, VERY_CLOSE }
```

Tests must assert:

```text
-80 dBm -> COLD
-75 dBm -> WARMER
-62 dBm -> HOT
-50 dBm -> VERY_CLOSE
```

`RssiSmoother(alpha = 0.25)` uses an exponential moving average; the first sample becomes the initial value.

Haptic intervals are exactly:

```text
COLD       2500 ms
WARMER     1500 ms
HOT         750 ms
VERY_CLOSE  300 ms
```

- [ ] **Step 5: Implement the proximity core and timeout helpers**

Expose:

```kotlin
const val SEARCH_TIMEOUT_MS = 120_000L

class RssiSmoother(private val alpha: Double = 0.25) {
	fun add(sampleDbm: Int): Double
	fun reset()
}

fun proximityLevel(rssiDbm: Double, config: ProximityConfig = ProximityConfig()): ProximityLevel
fun hapticIntervalMs(level: ProximityLevel): Long

enum class SearchPhase { IDLE, ACTIVE, TIMED_OUT }
fun searchPhase(startedAtMs: Long?, nowMs: Long): SearchPhase
```

`searchPhase(null, any)` is `IDLE`; elapsed `< 120_000` is `ACTIVE`; elapsed `>= 120_000` is `TIMED_OUT`.

- [ ] **Step 6: Run all shared tests**

```bash
./gradlew :shared:testDebugUnitTest --stacktrace
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add shared
git commit -m "feat: add Nearly protocol and proximity core"
```

---

### Task 3: Add a testable nearby-node Data Layer transport on phone and watch

**Files:**
- Create: `shared/src/main/java/sk/ziacik/nearly/shared/transport/PeerTransport.kt`
- Create: `mobile/src/main/java/sk/ziacik/nearly/mobile/data/WearDataLayerTransport.kt`
- Create: `wear/src/main/java/sk/ziacik/nearly/wear/data/WearDataLayerTransport.kt`
- Test: transport-adjacent coordinator tests in app unit-test source sets.

**Interfaces:**
- Produces the transport interface consumed by both find coordinators.

```kotlin
interface PeerTransport {
	suspend fun nearbyNodeId(): String?
	suspend fun send(nodeId: String, command: FindCommand)
}
```

- [ ] **Step 1: Add failing coordinator tests using a fake transport**

Create a minimal fake that records commands and returns a configured nearby node ID. Assert that a search cannot start when `nearbyNodeId()` returns `null`, and that a start sequence sends `StartFind` before `StartProximity`.

- [ ] **Step 2: Implement `WearDataLayerTransport` in each app module**

Use:

```kotlin
Wearable.getNodeClient(context).connectedNodes.await()
```

Choose the first node where:

```kotlin
node.isNearby
```

Return `null` if no directly nearby node exists. Send messages through `Wearable.getMessageClient(context).sendMessage(...).await()` using `FindProtocol.encode(command)`.

Do not intentionally fall back to non-nearby nodes.

- [ ] **Step 3: Run app unit tests**

```bash
./gradlew :mobile:testDebugUnitTest :wear:testDebugUnitTest --stacktrace
```

Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add shared mobile wear
git commit -m "feat: add local Wear Data Layer transport"
```

---

### Task 4: Implement BLE advertising/scanning with per-search token filtering

**Files:**
- Create in both app modules: `proximity/BleAdvertiser.kt`, `proximity/BleScanner.kt`, `proximity/AndroidBleAdvertiser.kt`, `proximity/AndroidBleScanner.kt`
- Test: coordinator tests use fake advertiser/scanner implementations.

**Interfaces:**

```kotlin
interface BleAdvertiser {
	val isSupported: Boolean
	suspend fun start(sessionToken: Int): Result<Unit>
	suspend fun stop()
}

interface BleScanner {
	val isSupported: Boolean
	fun scan(sessionToken: Int): Flow<Int>
	suspend fun stop()
}
```

- [ ] **Step 1: Write failing idempotency tests around fake BLE components**

Assert that two identical `StartProximity(token)` commands result in one active advertising session, and duplicate stop commands are harmless.

- [ ] **Step 2: Implement BLE advertisement payload**

Use one fixed app service UUID in both modules:

```text
8a8f4bd0-1b9e-4f47-9b7a-8f5729b3d341
```

Advertise the 4-byte big-endian session token as service data for that UUID. Do not advertise the device name.

Feature detection:

```kotlin
BluetoothAdapter.getDefaultAdapter()?.bluetoothLeAdvertiser != null
```

and the adapter must be enabled.

- [ ] **Step 3: Implement filtered scanning**

Create a `ScanFilter` matching the same service UUID and exact 4-byte session token service data. Emit `ScanResult.rssi` from callbacks into a `callbackFlow<Int>` and stop the platform scan in `awaitClose`.

- [ ] **Step 4: Add capability failure mapping**

BLE start/scan failures must surface as `Result.failure(...)` or closed-flow errors that the coordinator converts to “proximity unavailable”; they must not abort the Data Layer find session or target vibration.

- [ ] **Step 5: Run app unit tests and compile both APKs**

```bash
./gradlew :mobile:testDebugUnitTest :wear:testDebugUnitTest :mobile:assembleDebug :wear:assembleDebug --stacktrace
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add mobile wear
git commit -m "feat: add BLE proximity beacon and scanner"
```

---

### Task 5: Add permissions, silent target cues, and incoming command services

**Files:**
- Modify: both app manifests
- Create in each app: `permissions/BluetoothPermissionState.kt`, `cue/*CueController.kt`, `data/*WearListenerService.kt`, application class, target session controller.

**Interfaces:**

```kotlin
data class BluetoothPermissionState(
	val canScan: Boolean,
	val canAdvertise: Boolean,
	val canConnect: Boolean,
	val missingRuntimePermissions: List<String>,
)

interface CueController {
	val glowActive: StateFlow<Boolean>
	fun set(mode: CueMode)
	fun stop()
}
```

- [ ] **Step 1: Add platform permissions**

Both manifests declare:

```xml
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" android:usesPermissionFlags="neverForLocation" />
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
```

BLE hardware remains optional:

```xml
<uses-feature android:name="android.hardware.bluetooth_le" android:required="false" />
```

- [ ] **Step 2: Implement version-aware runtime permission calculation**

For API 31+, runtime requirements are `BLUETOOTH_SCAN`, `BLUETOOTH_ADVERTISE`, and `BLUETOOTH_CONNECT`. For API 30 and below, BLE scanning requires `ACCESS_FINE_LOCATION`; advertising uses legacy Bluetooth permissions granted at install time.

- [ ] **Step 3: Implement repeating target vibration**

Use `VibratorManager` on API 31+ and `Vibrator` below it. The target pattern should be recognizable but quiet:

```text
vibrate 180 ms, pause 820 ms, repeat
```

`stop()` must call `cancel()` and clear glow state.

- [ ] **Step 4: Implement best-effort glow**

The cue controller exposes `glowActive`; when the app activity is already visible, Compose renders a bright full-screen pulsing surface and keeps the screen on while active. Incoming background commands must not force-launch an Activity. `GLOW` received while the activity is not visible simply results in no visual cue; `BOTH` still vibrates.

- [ ] **Step 5: Add `WearableListenerService` on both platforms**

Each service decodes incoming messages through `FindProtocol.decode` and forwards them to a process-scoped target session controller from the application object.

Target session rules:

```text
StartFind(token, cue): if token changed, stop previous session; start requested cue.
SetCue(same token, cue): update cue.
StartProximity(same token): start advertising once.
StopProximity(same token): stop advertising.
StopFind(same token): stop cue + advertiser and clear session.
Commands for stale tokens: ignore.
```

- [ ] **Step 6: Add unit tests for target idempotency**

Use fake cue/advertiser objects. Test duplicate starts, stale-token commands, cue changes, and stop cleanup.

- [ ] **Step 7: Run tests/build**

```bash
./gradlew :mobile:testDebugUnitTest :wear:testDebugUnitTest :mobile:assembleDebug :wear:assembleDebug --stacktrace
```

Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add mobile wear
git commit -m "feat: add silent target cues and command handling"
```

---

### Task 6: Implement the search coordinators, smoothing, haptic guidance, and timeout

**Files:**
- Create: `mobile/search/MobileFindCoordinator.kt`
- Create: `wear/search/WearFindCoordinator.kt`
- Create app-specific search state models if needed
- Test both coordinators with fake transport/scanner/haptics and coroutine test scheduler.

**Interfaces:**

```kotlin
data class FindUiState(
	val searching: Boolean = false,
	val proximityLevel: ProximityLevel? = null,
	val smoothedRssi: Double? = null,
	val cueMode: CueMode = CueMode.BOTH,
	val proximityAvailable: Boolean = true,
	val error: FindError? = null,
)

enum class FindError {
	PEER_NOT_CONNECTED,
	BLUETOOTH_OFF,
	CAPABILITY_UNAVAILABLE,
	PERMISSION_MISSING,
	TIMED_OUT,
}
```

- [ ] **Step 1: Write failing coordinator lifecycle tests**

Test this exact happy-path order:

```text
resolve nearby node
create random 32-bit session token
send StartFind(token, BOTH)
send StartProximity(token)
start scanner(token)
consume RSSI -> EMA -> proximity level
run local guidance haptic cadence
on stop: stop scan/haptics, send StopProximity(token), send StopFind(token)
```

Also test timeout at exactly 120 seconds and cleanup after cancellation.

- [ ] **Step 2: Implement search session startup**

Use `SecureRandom().nextInt()` for the token. Do not reuse a previous session token.

Permission/capability behavior:

- Missing nearby peer: fail with `PEER_NOT_CONNECTED`, send nothing.
- Missing required Bluetooth permission: fail with `PERMISSION_MISSING` before BLE work.
- BLE proximity unsupported: still send `StartFind`; set `proximityAvailable = false`; do not fail the silent find.
- Bluetooth off: keep Data Layer cue attempt if a nearby peer is already available, but mark proximity unavailable with `BLUETOOTH_OFF`.

- [ ] **Step 3: Implement RSSI collection and local haptic guidance**

Every scan sample passes through one `RssiSmoother` instance per search. Reset the smoother on every new search. When the mapped `ProximityLevel` changes, update UI state immediately.

The searching device emits one short haptic tick at `hapticIntervalMs(level)` cadence; do not create continuous vibration.

- [ ] **Step 4: Implement cue switching**

`setCue(mode)` updates local UI state and sends `SetCue(currentToken, mode)` to the current nearby node. If sending fails, keep the current search alive and expose a non-fatal cue-send error only if needed by the UI; do not terminate BLE scanning.

- [ ] **Step 5: Implement timeout and idempotent stop**

Start one timeout coroutine per search:

```kotlin
delay(SEARCH_TIMEOUT_MS)
stop(timedOut = true)
```

Multiple calls to `stop()` must execute remote/local cleanup only once.

- [ ] **Step 6: Run coordinator tests**

```bash
./gradlew :mobile:testDebugUnitTest :wear:testDebugUnitTest --stacktrace
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add mobile wear
git commit -m "feat: orchestrate local hot cold finding"
```

---

### Task 7: Build the phone and watch Compose UI

**Files:**
- Create/modify mobile UI files and `MobileFindViewModel.kt`
- Create/modify wear UI files and `WearFindViewModel.kt`
- Add Compose instrumentation tests.

**Interfaces:**
- Consumes `FindUiState` and coordinator actions `start()`, `stop()`, `setCue()`.

- [ ] **Step 1: Add phone UI tests first**

Verify these states:

```text
idle -> visible “Find watch” action
searching with COLD -> “Cold”
searching with WARMER -> “Warmer”
searching with HOT -> “Hot”
searching with VERY_CLOSE -> “Very close”
proximity unavailable -> hide signal meter but keep Glow/Vibrate/Both + Stop
peer missing -> explicit counterpart-not-available message
permission missing -> explicit permission action/message
```

- [ ] **Step 2: Implement phone screens**

Keep one activity and two logical screens; navigation can be simple conditional Compose state rather than adding Navigation dependency.

Search screen contains:

```text
Nearly
[ Cold / Warmer / Hot / Very close ]
[ simple 4-step signal indicator ]
Glow   Vibrate   Both
[ Found it / Stop ]
```

Default selected cue is `Both`.

- [ ] **Step 3: Add Wear UI tests first**

Verify idle/search/error states with Wear Compose semantics. The primary idle action is `Find phone`.

- [ ] **Step 4: Implement watch UI with Wear Compose Material 3**

Use `ScreenScaffold` and Wear Material 3 components. Keep the search status dominant; cue controls may be compact buttons/chips. The stop action must always remain reachable without scrolling through nonessential content.

- [ ] **Step 5: Wire runtime permission requests from both activities**

Request only the permission set returned by `BluetoothPermissionState`. On denial, keep the app usable and explain that silent cue may still work while hot/cold is unavailable where applicable.

- [ ] **Step 6: Wire foreground glow rendering**

When `CueController.glowActive == true` and the activity is visible, replace normal content with a high-luminance pulsing surface and a large `Found it / Stop` button. Do not add audio.

- [ ] **Step 7: Run UI test compilation and app builds**

```bash
./gradlew :mobile:assembleAndroidTest :wear:assembleAndroidTest :mobile:assembleDebug :wear:assembleDebug --stacktrace
```

Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add mobile wear
git commit -m "feat: add Nearly phone and watch UI"
```

---

### Task 8: Add CI, README, and final verification

**Files:**
- Create: `.github/workflows/android-ci.yml`
- Create: `README.md`

**Interfaces:**
- Produces repeatable verification for the whole repository.

- [ ] **Step 1: Add GitHub Actions CI**

Workflow triggers on pushes to `master` and pull requests. Use Java 17, Android SDK 36, and Gradle setup actions consistent with the existing Android repos.

Verification command:

```bash
./gradlew \
	:shared:testDebugUnitTest \
	:mobile:testDebugUnitTest \
	:wear:testDebugUnitTest \
	:mobile:lintDebug \
	:wear:lintDebug \
	:mobile:assembleDebug \
	:wear:assembleDebug \
	:mobile:assembleAndroidTest \
	:wear:assembleAndroidTest \
	--stacktrace
```

Upload test/lint reports only on failure.

- [ ] **Step 2: Add README with exact MVP behavior and manual test matrix**

Document:

```text
1. Install mobile APK on Android phone.
2. Install wear APK on paired Wear OS watch.
3. Grant requested Nearby Devices permissions; on API <= 30 grant location only for BLE scanning.
4. Watch -> Find phone: verify target vibration, hot/cold state changes while walking toward/away, stop cleanup.
5. Phone -> Find watch: repeat the same verification.
6. Disable Bluetooth: verify proximity degrades gracefully.
7. Deny scan/advertise permission: verify explicit permission state and no crash.
8. Leave a search active for 120 seconds: verify automatic cleanup and Timed out state.
9. Put target app in background/lock screen: verify vibration still provides the reliable silent cue and glow is not force-launched.
```

Also state clearly that RSSI thresholds are initial tuning values and real-device calibration is expected after the first Moto G31 + Galaxy Watch FE run.

- [ ] **Step 3: Run the full verification command locally**

Expected: every Gradle task passes.

- [ ] **Step 4: Inspect APK manifests for accidental permissions**

Run:

```bash
apkanalyzer manifest permissions mobile/build/outputs/apk/debug/mobile-debug.apk
apkanalyzer manifest permissions wear/build/outputs/apk/debug/wear-debug.apk
```

Expected: only permissions justified by the design/dependencies; investigate any unexpected dangerous permission before release.

- [ ] **Step 5: Confirm no internet/cloud implementation slipped in**

Run:

```bash
! grep -R -E 'Retrofit|OkHttp|HttpURLConnection|Firebase|INTERNET' mobile/src wear/src shared/src
```

Expected: command exits successfully with no matches from app source/manifests.

- [ ] **Step 6: Commit**

```bash
git add .github README.md
git commit -m "ci: verify Nearly mobile and Wear OS apps"
```

---

## Plan Self-Review

- Every approved MVP feature is covered: both directions, local-only Data Layer control, BLE RSSI hot/cold, smoothing, haptic guidance, Glow/Vibrate/Both, fallback behavior, permissions, timeout, UI, tests, and CI.
- The cue controls require one additional protocol command beyond the four initial design commands; `SetCue` is explicitly included because the approved UI allows changing cue mode during an active search.
- BLE sessions use a random per-search token so another nearby Nearly installation cannot be mistaken for the intended target solely because it advertises the same service UUID.
- No task depends on UWB, cloud, account infrastructure, GPS, maps, or background continuous tracking.
- Real-device RSSI threshold calibration is intentionally deferred until functional device testing; the initial threshold values are centralized and covered by unit tests so calibration is a small later change.
