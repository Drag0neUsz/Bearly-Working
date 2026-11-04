---
name: Gamified Habit & Task Companion
colors:
  surface: '#fbf9f8'
  surface-dim: '#dbd9d9'
  surface-bright: '#fbf9f8'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f5f3f3'
  surface-container: '#efeded'
  surface-container-high: '#eae8e7'
  surface-container-highest: '#e4e2e2'
  on-surface: '#1b1c1c'
  on-surface-variant: '#3f4a36'
  inverse-surface: '#303030'
  inverse-on-surface: '#f2f0f0'
  outline: '#6f7b64'
  outline-variant: '#becbb1'
  surface-tint: '#2b6c00'
  primary: '#2b6c00'
  on-primary: '#ffffff'
  primary-container: '#58cc02'
  on-primary-container: '#1e5000'
  inverse-primary: '#6be026'
  secondary: '#006590'
  on-secondary: '#ffffff'
  secondary-container: '#2fb8ff'
  on-secondary-container: '#004666'
  tertiary: '#8c5000'
  on-tertiary: '#ffffff'
  tertiary-container: '#ff9c27'
  on-tertiary-container: '#683a00'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#87fe45'
  primary-fixed-dim: '#6be026'
  on-primary-fixed: '#082100'
  on-primary-fixed-variant: '#1f5100'
  secondary-fixed: '#c8e6ff'
  secondary-fixed-dim: '#88ceff'
  on-secondary-fixed: '#001e2e'
  on-secondary-fixed-variant: '#004c6e'
  tertiary-fixed: '#ffdcbf'
  tertiary-fixed-dim: '#ffb872'
  on-tertiary-fixed: '#2d1600'
  on-tertiary-fixed-variant: '#6a3b00'
  background: '#fbf9f8'
  on-background: '#1b1c1c'
  surface-variant: '#e4e2e2'
typography:
  headline-xl:
    fontFamily: Nunito Sans
    fontSize: 36px
    fontWeight: '900'
    lineHeight: 44px
  headline-xl-mobile:
    fontFamily: Nunito Sans
    fontSize: 28px
    fontWeight: '900'
    lineHeight: 34px
  headline-lg:
    fontFamily: Nunito Sans
    fontSize: 24px
    fontWeight: '800'
    lineHeight: 30px
  headline-md:
    fontFamily: Nunito Sans
    fontSize: 20px
    fontWeight: '800'
    lineHeight: 26px
  headline-sm:
    fontFamily: Nunito Sans
    fontSize: 16px
    fontWeight: '800'
    lineHeight: 22px
    letterSpacing: 0.04em
  body-lg:
    fontFamily: Nunito Sans
    fontSize: 17px
    fontWeight: '600'
    lineHeight: 24px
  body-md:
    fontFamily: Nunito Sans
    fontSize: 15px
    fontWeight: '600'
    lineHeight: 21px
  body-sm:
    fontFamily: Nunito Sans
    fontSize: 13px
    fontWeight: '600'
    lineHeight: 18px
  label-lg:
    fontFamily: Nunito Sans
    fontSize: 14px
    fontWeight: '800'
    lineHeight: 18px
    letterSpacing: 0.05em
  label-md:
    fontFamily: Nunito Sans
    fontSize: 12px
    fontWeight: '800'
    lineHeight: 16px
    letterSpacing: 0.06em
  label-sm:
    fontFamily: Nunito Sans
    fontSize: 10px
    fontWeight: '900'
    lineHeight: 14px
    letterSpacing: 0.08em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-desktop: 1.5rem
  margin: 1rem
  margin-desktop: 2rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1rem
  space-xl: 1.5rem
---

## Brand & Style
The design system embodies a vibrant, tactile, gamified aesthetic where productivity meets unapologetic fun. Drawing direct inspiration from playful, character-driven edtech interfaces, the tone strikes a deliberate balance between motivational urgency and humorous irreverence.

### Target Audience & Personality
Built for digital natives, chronic procrastinators, and goal-setters who find sterile productivity dashboards dull and demotivating. The brand voice is spunky, sharp-witted, and encouraging. Interactions are high-energy, responsive, and rewarding.

### Design Movement: Tactile Gamification & Soft Pop
The visual language relies on tactile "squishy" physical metaphors:
- **3D Pressable Affordance**: Buttons and cards feel extruded with distinct bottom-edge depth lips that visually compress upon touch.
- **Expressive Dialogues**: The bear companion speaks through physical speech bubbles featuring crisp borders and playful pointer tails.
- **Gamified Progression**: Highlighting streaks, gems, roast collections, and completion scores with vibrant, celebratory visual anchors.

## Colors
The color palette uses clean, high-saturation accents set against an ultra-soft, warm canvas to reduce cognitive fatigue while keeping interactive triggers vivid and immediate.

### Palette Architecture
- **Primary (`#58CC02` - Feather Green)**: Success states, active progression bars, main affirmative actions, and completion meters. Its 3D base shadow is `#46A302`.
- **Secondary (`#1CB0F6` - Electric Sky)**: Informational pills, interactive schedule blocks, calendar active selections, and navigation badges. Its 3D shadow is `#1899D6`.
- **Tertiary (`#FF9600` - Fire Orange)**: Streaks, urgent alerts, roast callouts, and timer milestones. Its 3D shadow is `#D97F00`.
- **Alert / Danger (`#FF4B4B` - Berry Red)**: Overdue tasks, savage bear poke warnings, and negative triggers. Shadow: `#D33131`.
- **Highlight / Reward (`#FFC800` - Honey Gold)**: Gems, XP counters, and bonus milestones. Shadow: `#DDAE00`.
- **Neutral Primary (`#4B4B4B` - Dark Slate)**: Primary typography and assertive outlines.
- **Neutral Muted (`#777777` - Ash Gray)**: Secondary descriptions, microcopy, and timestamps.
- **Surface Canvas (`#F7F9FA` - Cloud White)**: Default canvas background.
- **Surface Elevated (`#FFFFFF` - Pure White)**: Card faces, dialog modals, and floating speech containers.
- **Borders & Dividers (`#E5E5E5` - Crisp Contour)**: Subtle card borders and list separators.

## Typography
Typographic rhythm uses the rounded geometry of **Nunito Sans** across all hierarchies. The type system emphasizes bold, friendly chunkiness, making even scathing companion roasts look warm, game-like, and approachable.

### Rules & Details
- **Uppercase Headers**: `headline-sm` and `label-*` styles default to uppercase styling with slight positive letter spacing (`0.04em` to `0.08em`) to preserve punchy legibility across badges, scorecards, and tab navigation.
- **Weight Pairing**: Body text avoids light weights entirely; it stays anchored at weight `600` (SemiBold) to ensure high visibility against colorful backdrops and thick component borders.
- **Dialogues & Roasts**: Speech bubble messages use `body-lg` or `headline-md` with `font-weight: 800` to mirror physical cartoon speech panels.

## Layout & Spacing
The layout follows a modular, mobile-first column model designed around self-contained touch cards and vertical stack rhythms.

### Grid & Density
- **Mobile (< 768px)**: Single-column fluid view with `16px` outer margins (`margin: 1rem`) and `12px` to `16px` gutters between stacked cards.
- **Tablet / Desktop (≥ 768px)**: Centered container capped at max-width `480px` for phone emulation, or a 12-column dashboard layout with `gutter-desktop: 1.5rem` and `margin-desktop: 2rem`.
- **Rhythm**: Component internals follow an 8pt base grid. Compact badge elements utilize `space-xs` and `space-sm`, while internal card containers rely on `space-lg` to create breathable surfaces for chubby visual elements.

## Elevation & Depth
Elevation rejects diffuse, realistic blur shadows in favor of **chunky tactile extrusions** inspired by isometric game tokens and toy blocks.

### The 3D Push-Down Model
- **Resting Depth**: Buttons, interactive tiles, and high-priority cards display a hard bottom border (typically `4px`) colored 20% darker than their surface fill (e.g., surface `#58CC02` paired with edge `#46A302`).
- **Active State (`:active`)**: Pressing down physically depresses the element using `transform: translateY(3px)` while compressing the bottom border to `1px`. This provides an immediate, game-like haptic feel.
- **Flat Containers**: Secondary passive containers (e.g., calendar month grid wrapper, scorecard metrics frame) use a crisp `2px` solid border (`#E5E5E5`) on pure white surfaces without dropshadows.
- **Floating Modals**: Pinned bottom navigation and overlays use a subtle outline of `2px solid #E5E5E5` paired with a sharp non-blurred offset: `box-shadow: 0px 4px 0px #E5E5E5`.

## Shapes
Shapes are soft, chubby, and friendly. Corners avoid sharp 90-degree points entirely to reinforce the non-intimidating, gamified environment.

### Rounded Tokens
- **Buttons & Chips (`rounded-full`)**: Primary action buttons, day chips, and pill switches are fully pill-shaped (`9999px`) or rounded with high curvature.
- **Cards & Speech Bubbles (`rounded-2xl` / `1rem` to `1.5rem`)**: Content containers, roast cards, and dialogue boxes feature generous curves.
- **Speech Bubble Triangle**: Mascots communicate through callout boxes tipped with an equilateral directional pointer constructed with matching `2px` stroke outlines and rounded joints.

## Components

### Buttons
- **Primary 3D Push**: Pill or `16px` rounded rect. Background is `#58CC02`, text is `#FFFFFF` (`label-lg`), with a `4px` solid bottom rim of `#46A302`. On tap, translates down `3px` with a `1px` rim.
- **Secondary Action**: Bordered button with white background, `2px` border `#E5E5E5`, bottom rim `4px` `#D0D0D0`, text `#1CB0F6`.
- **Danger Action**: Background `#FF4B4B` with `#D33131` bottom rim.
- **Sizing**: Minimum touch target of `48px` height with generous horizontal padding (`space-xl`).

### Cards & Speech Bubbles
- **Bear Dialogue Box**: White background with `2px` solid `#4B4B4B` or `#E5E5E5` border, `16px` border-radius, internal padding of `space-lg`. Includes top label tags (`label-sm` uppercase, e.g. `BEAR [UNFILTERED RAGE]`) in tertiary orange or red.
- **List / Task Cards**: White surface, `2px` border `#E5E5E5`, `3px` bottom pseudo-shadow. Contains task checkbox on the left, high-contrast title, and secondary metadata badges.

### Progress Bars
- **Chunky Capsule Bar**: Outer track has `16px` height, `#E5E5E5` background, fully rounded. Active inner bar is filled with `#58CC02` or `#1CB0F6` featuring an inner white highlight cap along the top edge for a glossy, tubular look.

### Segmented Controls & Chips
- **Interval & Status Selectors**: Enclosed inside a `2px` bordered pill or rectangle container. The active item fills with `#1CB0F6` or dark slate `#4B4B4B` with crisp white bold typography, while unselected segments remain transparent with muted text.

### Form Switches & Checkboxes
- **Toggle Switches**: Chunky pill track (`28px` height, `48px` width) in `#E5E5E5` (inactive) or `#58CC02` (active) with an extruded circular white knob that slides smoothly across.
- **Checkboxes**: Thick `2px` rounded squares (`24px x 24px`, radius `8px`). When marked, bursts with green fill `#58CC02` and a bold white checkmark.

### Bottom Navigation Bar
- Pinned bottom container with white surface, top border `2px solid #E5E5E5`.
- 4 navigation tabs with icon stacked above `label-sm` text. Active tab highlighted with brand primary `#58CC02` or `#1CB0F6`, using an active pill indicator or thick baseline bar to clearly communicate location.