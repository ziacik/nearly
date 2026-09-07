# Nearly Soft Playful Visual Refresh Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the functional Nearly MVP into a polished soft-playful Android + Wear OS product with a coherent brand, proximity visuals, cue controls, Glow screen, and launcher identity while preserving all existing find behavior.

**Architecture:** Keep all BLE/Data Layer/state behavior untouched and replace only presentation. Introduce small reusable Compose components and module-local themes, then wire the existing `FindUiState` into the new visual components. Use vector/adaptive Android resources for launcher identity and Compose drawing for reusable in-app brand/proximity visuals.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Wear Compose Material 3, Android VectorDrawable/adaptive icons, Java 17, compileSdk/targetSdk 36.

**Spec:** `docs/superpowers/specs/2026-09-07-soft-playful-visual-refresh-design.md`

## Global Constraints

- Preserve the existing `FindUiState`, BLE, Data Layer, timeout, permissions, proximity smoothing, and target cue architecture.
- Keep the app local-only with no Internet permission or client.
- Dark-first visual system with graphite/charcoal surfaces and coral/peach/amber/rose/lavender accents.
- `Cold` uses lavender, `Warmer` rose/coral, `Hot` coral/peach, `Very close` peach/amber.
- Friendly but not childish: no mascots, faces, confetti, handwriting, or novelty copy.
- No new Devices/Settings navigation in this refresh.
- `Very close` is the warmest searching state; tapping `Found it` ends the search and returns to idle.
- Phone and Wear layouts must remain focused on one primary find action.
- Keep tabs, not spaces, in Kotlin source.

---

### Task 1: Visual tokens and brand primitives

**Files:**
- Create: `mobile/src/main/java/sk/ziacik/nearly/mobile/ui/theme/NearlyTheme.kt`
- Create: `mobile/src/main/java/sk/ziacik/nearly/mobile/ui/NearlyBrandMark.kt`
- Create: `mobile/src/main/java/sk/ziacik/nearly/mobile/ui/NearlyIcons.kt`
- Create: `wear/src/main/java/sk/ziacik/nearly/wear/ui/theme/NearlyWearTheme.kt`
- Create: `wear/src/main/java/sk/ziacik/nearly/wear/ui/NearlyBrandMark.kt`
- Create: `wear/src/main/java/sk/ziacik/nearly/wear/ui/NearlyIcons.kt`

**Interfaces:**
- Produces: `NearlyTheme(content: @Composable () -> Unit)` for mobile.
- Produces: `NearlyWearTheme(content: @Composable () -> Unit)` for Wear.
- Produces module-local `NearlyBrandMark(modifier: Modifier)` using two overlapping soft forms.
- Produces module-local cue icon composables used by later tasks.

- [ ] **Step 1: Add visual token/theme files.**

Mobile theme must expose a dark `ColorScheme` with starting values from the spec (`#111218`, `#1B1D26`, `#252834`, `#F7F2EE`, `#B8B2B3`, coral/peach/amber/rose/lavender) and a rounded-shape `Shapes` set.

Wear theme must map the same conceptual palette into Wear Material 3 color roles.

- [ ] **Step 2: Add brand and cue drawing primitives.**

Implement the brand mark with Compose `Canvas`: two overlapping soft circular/oval forms, coral/peach on the left and lavender/rose on the right. Add lightweight Canvas cue icons for Glow, Vibrate, Both, Search/Proximity, and Stop/Found so Wear does not depend on tiny text initials.

- [ ] **Step 3: Build both modules.**

Run:

```bash
./gradlew :mobile:compileDebugKotlin :wear:compileDebugKotlin
```

Expected: PASS.

- [ ] **Step 4: Commit.**

```bash
git add mobile/src/main/java/sk/ziacik/nearly/mobile/ui wear/src/main/java/sk/ziacik/nearly/wear/ui
git commit -m "feat: add Nearly visual system"
```

---

### Task 2: Mobile home/search UI and proximity indicator

**Files:**
- Modify: `mobile/src/main/java/sk/ziacik/nearly/mobile/ui/MobileApp.kt`
- Create: `mobile/src/main/java/sk/ziacik/nearly/mobile/ui/ProximityIndicator.kt`
- Create: `mobile/src/main/java/sk/ziacik/nearly/mobile/ui/CueSelector.kt`
- Modify: `mobile/src/androidTest/java/sk/ziacik/nearly/mobile/ui/MobileAppTest.kt`

**Interfaces:**
- Consumes: `NearlyTheme`, `NearlyBrandMark`, cue icon primitives.
- Produces: `ProximityIndicator(level: ProximityLevel?, searching: Boolean, modifier: Modifier = Modifier)`.
- Produces: `CueSelector(selected: CueMode, onCue: (CueMode) -> Unit, modifier: Modifier = Modifier)`.

- [ ] **Step 1: Write/extend failing Compose UI assertions.**

Add coverage that idle shows `Nearly`, `Find your watch`, and `Start searching`; search shows `Finding your watch…`, the proximity label, all three cue labels, and `Found it`; fallback still shows Glow/Vibrate/Both when hot/cold is unavailable.

- [ ] **Step 2: Run instrumentation compile/test target to confirm the old UI does not satisfy the new copy/layout assertions.**

Run:

```bash
./gradlew :mobile:assembleAndroidTest
```

Then execute on an available emulator/device when present; at minimum compilation must expose any missing UI/test APIs.

- [ ] **Step 3: Implement `ProximityIndicator`.**

Use Compose `Canvas` with concentric rings/radial emphasis. Map `COLD`, `WARMER`, `HOT`, and `VERY_CLOSE` to progressively warmer colors. Animate only a subtle pulse/scale/alpha; label text remains authoritative.

- [ ] **Step 4: Implement `CueSelector`.**

Render three large rounded tiles (`Glow`, `Vibrate`, `Both`) with icon + label. Selected tile gets a warm surface/accent treatment and short color/scale transition; callback behavior remains the existing `onCue(mode)`.

- [ ] **Step 5: Replace `MobileApp` layout.**

Idle layout: compact Nearly header/mark, prominent rounded hero card, supporting status/error copy, `Start searching` primary action, permission action only when required.

Searching layout: `Finding your watch…`, proximity indicator, large proximity state/guidance, cue selector, relevant error/fallback text, and large `Found it` button. Do not add tabs or a separate found destination.

- [ ] **Step 6: Run mobile verification.**

Run:

```bash
./gradlew :mobile:testDebugUnitTest :mobile:lintDebug :mobile:assembleDebug :mobile:assembleAndroidTest
```

Expected: PASS.

- [ ] **Step 7: Commit.**

```bash
git add mobile/src/main/java/sk/ziacik/nearly/mobile/ui mobile/src/androidTest/java/sk/ziacik/nearly/mobile/ui/MobileAppTest.kt
git commit -m "feat: redesign Nearly mobile find flow"
```

---

### Task 3: Wear OS home/search UI

**Files:**
- Modify: `wear/src/main/java/sk/ziacik/nearly/wear/ui/WearApp.kt`
- Create: `wear/src/main/java/sk/ziacik/nearly/wear/ui/WearProximityIndicator.kt`
- Create: `wear/src/main/java/sk/ziacik/nearly/wear/ui/WearCueSelector.kt`
- Modify: `wear/src/androidTest/java/sk/ziacik/nearly/wear/ui/WearAppTest.kt`

**Interfaces:**
- Consumes: `NearlyWearTheme`, Wear brand/cue primitives.
- Produces: `WearProximityIndicator(level: ProximityLevel?, searching: Boolean, modifier: Modifier = Modifier)`.
- Produces: `WearCueSelector(selected: CueMode, onCue: (CueMode) -> Unit, modifier: Modifier = Modifier)`.

- [ ] **Step 1: Extend Wear Compose UI assertions.**

Idle must show `Find phone`; search must show proximity state plus real `Glow`, `Vibrate`, `Both` semantics/text rather than `G/V/B`; fallback must preserve cue actions.

- [ ] **Step 2: Build the failing test target before production changes.**

Run:

```bash
./gradlew :wear:assembleAndroidTest
```

- [ ] **Step 3: Implement the round-safe proximity indicator and cue selector.**

Keep content inside round-screen safe areas. The proximity ring should dominate the search state; cue controls are compact icon-led controls with short labels/semantics and an obvious selected state.

- [ ] **Step 4: Replace `WearApp` layout.**

Idle: brand mark, `Find phone`, one large start button, contextual permission/error only when needed.

Searching: ring + proximity label + short guidance, cue selector, and easy `Found it`/stop action. Remove `G/V/B` text buttons.

- [ ] **Step 5: Run Wear verification.**

Run:

```bash
./gradlew :wear:testDebugUnitTest :wear:lintDebug :wear:assembleDebug :wear:assembleAndroidTest
```

Expected: PASS.

- [ ] **Step 6: Commit.**

```bash
git add wear/src/main/java/sk/ziacik/nearly/wear/ui wear/src/androidTest/java/sk/ziacik/nearly/wear/ui/WearAppTest.kt
git commit -m "feat: redesign Nearly Wear find flow"
```

---

### Task 4: Glow screens and launcher identity

**Files:**
- Modify: `mobile/src/main/java/sk/ziacik/nearly/mobile/ui/GlowScreen.kt`
- Modify: `wear/src/main/java/sk/ziacik/nearly/wear/ui/GlowScreen.kt`
- Modify: `mobile/src/main/AndroidManifest.xml`
- Modify: `wear/src/main/AndroidManifest.xml`
- Create: `mobile/src/main/res/values/colors.xml`
- Create: `mobile/src/main/res/drawable/ic_launcher_foreground.xml`
- Create: `mobile/src/main/res/drawable/ic_launcher_monochrome.xml`
- Create: `mobile/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- Create: `mobile/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- Create corresponding Wear resources under `wear/src/main/res/...`
- Update UI tests if Glow close-action copy changes.

**Interfaces:**
- `GlowScreen(onFound: () -> Unit)` signature remains unchanged on both platforms.
- Manifest launcher icon references become `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round`.

- [ ] **Step 1: Add launcher/adaptive icon resources.**

Use the two-overlapping-form Nearly mark. Full-color foreground uses coral/peach + lavender/rose; monochrome uses a single silhouette. Keep enough inset for adaptive-mask safety. Configure `<monochrome>` on adaptive icons for themed icons.

- [ ] **Step 2: Update both manifests.**

Add `android:icon` and `android:roundIcon` to each application while preserving all permissions/services exactly.

- [ ] **Step 3: Refresh Glow screens.**

Phone Glow: bright warm cream/peach gradient with a slow pulse and a minimal `Found it` affordance, retaining high luminance over most of the display.

Wear Glow: same identity adapted to the circular display, with a bright center and simple close/found action.

- [ ] **Step 4: Run complete verification.**

Run:

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
  :wear:assembleAndroidTest
```

Expected: PASS, with no new Internet permission in packaged manifests.

- [ ] **Step 5: Commit.**

```bash
git add mobile/src/main wear/src/main
git commit -m "feat: add Nearly brand and glow identity"
```

---

### Task 5: Final visual/functional regression review

**Files:**
- Review all files changed by Tasks 1-4.

**Interfaces:**
- No new behavior/interface changes are allowed in this task.

- [ ] **Step 1: Inspect diff for accidental scope growth.**

Verify there are no BLE/Data Layer/proximity algorithm changes, no new navigation destinations, no account/network code, and no Internet permission.

- [ ] **Step 2: Verify UI state coverage.**

Check idle, searching with each proximity level, hot/cold unavailable, permission missing, peer unavailable, and Glow states on both phone and watch.

- [ ] **Step 3: Run the full verification command again from a clean head.**

Use the exact full Gradle command from Task 4 and require all tasks to pass before claiming completion.

- [ ] **Step 4: Produce installable debug APK artifacts for phone and Wear and leave the implementation branch ready for user device testing.**
