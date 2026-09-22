# Nearly Soft Playful Visual Refresh

## Goal

Refresh Nearly from a functional MVP into a visually distinctive Android + Wear OS product without changing its core behavior.

The chosen direction is **soft playful**: warm, friendly, memorable, and visually expressive, but clearly more restrained than a cute or mascot-driven style.

The design should feel like a small polished product with personality, not a generic Material demo and not a toy.

## Design Principles

1. **Quiet, not clinical.** Nearly exists to help users find devices without waking the house. The interface should feel calm even when it is visually distinctive.
2. **Warmth communicates proximity.** As the user gets closer, color, glow, and motion become warmer and more present.
3. **One obvious action at a time.** The MVP remains intentionally simple. Visual richness must not add navigational complexity.
4. **Friendly, not childish.** Use soft geometry and playful color, but no faces, mascots, confetti overload, handwriting, or novelty copy.
5. **Brand and interaction share one language.** The app icon, proximity indicator, cue icons, Glow screen, mobile UI, and Wear UI should feel like one system.

## Brand Direction

### Mark

Use a simple mark made from **two soft overlapping rounded forms**. The forms suggest two nearby devices, two points moving together, or overlapping proximity zones.

The mark must work at three levels:

- full-color launcher icon,
- small in-app brand mark,
- monochrome Android themed icon.

Do not use a literal phone/watch silhouette or Bluetooth symbol as the main brand mark.

### Wordmark

The word **Nearly** remains the primary name. Use the platform typography rather than introducing a bundled custom font in the first implementation.

The brand should rely on the mark, color, spacing, and shape language rather than font novelty.

### Voice

Keep copy short and warm. Examples:

- `Find your watch`
- `Move around slowly`
- `You're getting closer`
- `It's here!`

Avoid jokey microcopy, mascots, or overly sentimental slogans inside the app.

## Color System

Use a dark-first visual system for both phone and watch because it suits night-time use and gives the warm proximity states room to stand out.

Initial design tokens:

- Background: near-black graphite, approximately `#111218`
- Elevated surface: soft charcoal, approximately `#1B1D26`
- Secondary surface: approximately `#252834`
- Primary text: warm off-white, approximately `#F7F2EE`
- Secondary text: muted warm gray, approximately `#B8B2B3`
- Coral: approximately `#FF6E68`
- Peach: approximately `#FF9C72`
- Amber: approximately `#FFBD63`
- Rose: approximately `#E96A8D`
- Lavender: approximately `#8D77D9`

These values are starting points, not contractual brand constants. They may be tuned during implementation for contrast and device rendering.

### Proximity Mapping

- `Cold`: lavender / muted violet
- `Warmer`: rose to coral
- `Hot`: coral to peach
- `Very close`: peach to amber with the strongest warm glow

Do not use blue for the cold state; Nearly should not inherit the generic Bluetooth/system-utility visual language.

## Shape Language

Use generous rounded rectangles and circular/radial forms.

- Cards: large rounded corners, visually soft but not bubbly.
- Primary actions: pill or highly rounded rectangular buttons.
- Cue controls: rounded square tiles with icons and labels.
- Proximity UI: circles, rings, and soft radial gradients.

Avoid excessive outlines, glassmorphism, neon sci-fi styling, hard-edged cards, and decorative blobs scattered around the UI.

## Mobile UI

### Idle / Home

The phone app still has one primary purpose: find the paired watch.

Layout:

- compact Nearly brand header,
- one prominent hero card for `Find your watch`,
- short status text such as connected / unavailable,
- large primary `Start searching` action,
- permission/error action only when needed.

The first refresh should **not** add Devices or Settings tabs. Those were illustrative in visual concepts but conflict with the MVP scope and are unnecessary now.

### Searching

The searching state is the visual centerpiece of the app.

Layout:

- title such as `Finding your watch…`,
- large central radial proximity indicator,
- current state as large text: `Cold`, `Warmer`, `Hot`, `Very close`,
- one short guidance line,
- three cue tiles: `Glow`, `Vibrate`, `Both`,
- one clear `Found it` action that stops the search.

The existing signal-dot indicator is replaced by a branded proximity ring/orb.

The ring should communicate the state even before the user reads the label. Color warmth, illuminated arc/rings, and subtle scale or pulse may change with proximity.

### Very Close / Completion

`Very close` is the strongest and warmest search state. It should feel satisfying but restrained, using the warmest ring/orb treatment and a short reassuring line such as `It's right around here.`

There is **no separate persistent or transient Found screen** in this refresh. Pressing `Found it` immediately stops target cues, proximity work, and the active search exactly as it does today, then returns to the idle screen.

### Error States

Errors stay inside the same screen hierarchy instead of replacing the entire visual design.

Examples:

- watch unavailable,
- Bluetooth off,
- permission missing,
- hot/cold unavailable,
- search timed out.

Use calm explanatory text and one relevant action. Proximity failure must not visually obscure Glow/Vibrate controls when those still work.

## Glow Screen

Glow is a functional cue, so visibility takes precedence over brand decoration.

The refreshed phone Glow screen should:

- remain extremely bright,
- use a warm cream / peach-centered full-screen gradient instead of a plain white demo screen,
- pulse slowly and smoothly,
- keep enough high-luminance area to remain useful across a room,
- show only a minimal `Found it` / close affordance if UI controls are visible.

Do not turn Glow into an illustration or animation-heavy scene.

Wear Glow follows the same visual idea within the limits of the small circular display.

## Wear OS UI

Wear remains more minimal than mobile.

### Idle

- small Nearly brand mark,
- large `Find phone` label,
- one prominent start button.

### Searching

- radial proximity ring using the same state colors as mobile,
- large proximity label in the center,
- minimal secondary guidance,
- cue controls reduced to icons or very short labels,
- easy `Found it` action reachable without dense layout.

The ring must respect round-screen safe areas and remain readable on Galaxy Watch FE-sized displays.

### Very Close

Use the warmest state and a simple confirmation such as `Right around here.` while keeping the existing `Found it` action. No celebration graphics or confetti.

## Iconography

Create a small coherent icon family for:

- Glow,
- Vibrate,
- Both,
- proximity/search,
- Found/Stop where needed.

Prefer simple rounded line/filled hybrid shapes that remain legible at Wear OS sizes.

Where suitable, use Material Symbols / platform vector primitives as a base, but tune presentation consistently. Do not introduce raster icon buttons for ordinary UI controls.

## Launcher and Brand Assets

Create:

- Android adaptive launcher foreground,
- launcher background color/gradient-compatible base,
- monochrome themed icon,
- matching Wear launcher icon configuration,
- splash identity using the same mark.

The full-color launcher icon should use the overlapping-two-form brand mark with a coral/peach and lavender/rose relationship on a dark or warm-neutral base.

The mark must remain recognizable after Android adaptive-icon masking.

## Motion

Motion is subtle and functional.

### Proximity

- Cold: slow, faint breathing/pulse.
- Warmer: slightly stronger pulse and warmer arc.
- Hot: more visible warmth and faster but still calm feedback.
- Very close: strongest warm glow, not frantic flashing.

### Cue Selection

Selected cue tiles may use a short scale/color transition.

### Accessibility

Respect system reduced-motion / animation settings where practical. Information must never depend only on animation or color; text labels remain authoritative.

## Compose Structure

The visual refresh should introduce reusable UI pieces rather than growing the existing `MobileApp.kt` and `WearApp.kt` into monoliths.

Expected mobile pieces include concepts such as:

- `NearlyTheme`
- `NearlyBrandMark`
- `FindHeroCard`
- `ProximityIndicator`
- `CueSelector`
- `CueTile`
- themed status/error presentation

Wear should have equivalent focused components sized for round screens, reusing shared conceptual tokens where practical but not forcing phone layouts into Wear abstractions.

No changes to BLE, Data Layer, proximity smoothing, commands, permissions, timeout behavior, or target cue architecture are part of this visual refresh unless a small UI integration change is strictly necessary.

## Testing

Keep all existing functional tests green.

Add or update UI tests where useful for:

- idle/searching state switching,
- proximity labels,
- cue selection state,
- permission/error actions,
- hot/cold-unavailable fallback,
- Glow screen action availability.

Build and lint both modules.

Real-device visual verification should cover:

- Moto G31 or comparable Android phone,
- Galaxy Watch FE or comparable round Wear OS watch,
- daylight readability,
- dark-room usability,
- Very close state legibility,
- Glow visibility from across a room.

## Non-goals

This refresh does not add:

- multi-device management,
- Devices or Settings destinations,
- cloud or remote finding,
- maps,
- history,
- accounts,
- mascots,
- sound-based finding,
- new Bluetooth behavior,
- new permissions.

## Success Criteria

The refresh is successful when:

1. A screenshot is recognizable as Nearly rather than stock Material 3.
2. Mobile and Wear clearly belong to the same brand.
3. Cold → Very close can be understood immediately from both text and visual warmth.
4. The app remains simple enough to operate half-asleep at night.
5. The design feels friendly and memorable without looking childish.
6. Existing find behavior remains unchanged and all CI checks stay green.
