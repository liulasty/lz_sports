---
name: frontend-design
description: Create distinctive, production-grade frontend interfaces with high design quality. Use this skill when the user asks to build web components, pages, dashboards, or applications (websites, landing pages, dashboards, React/Vue components, HTML/CSS layouts, or when styling/beautifying any web UI). Generates creative, polished code and UI design that avoids generic AI aesthetics.
---

# Frontend Design

This skill guides creation of distinctive, production-grade frontend interfaces that avoid generic aesthetics. Implement real working code with exceptional attention to aesthetic details and creative choices.

## Design Thinking

Before coding, understand the context and commit to a BOLD aesthetic direction:
- **Purpose**: What problem does this interface solve? Who uses it?
- **Tone**: Pick an extreme: brutally minimal, maximalist chaos, retro-futuristic, organic/natural, luxury/refined, playful/toy-like, editorial/magazine, brutalist/raw, art deco/geometric, soft/pastel, industrial/utilitarian, etc.
- **Constraints**: Technical requirements (framework, performance, accessibility).
- **Differentiation**: What makes this UNFORGETTABLE?

**CRITICAL**: Choose a clear conceptual direction and execute it with precision. Bold maximalism and refined minimalism both work - the key is intentionality, not intensity.

Then implement working code that is:
- Production-grade and functional
- Visually striking and memorable
- Cohesive with a clear aesthetic point-of-view
- Meticulously refined in every detail

## Frontend Aesthetics Guidelines

Focus on:
- **Typography**: Distinctive, characterful font choices. Avoid Inter, Roboto, Arial. Pair a distinctive display font with a refined body font.
- **Color & Theme**: CSS variables for consistency. Dominant colors with sharp accents over timid palettes.
- **Motion**: CSS-only animations for micro-interactions. Focus on high-impact moments: staggered reveals, scroll-triggering, hover states that surprise.
- **Spatial Composition**: Unexpected layouts, asymmetry, overlap, diagonal flow. Generous negative space or controlled density.
- **Backgrounds & Visual Details**: Gradient meshes, noise textures, geometric patterns, layered transparencies, dramatic shadows, decorative borders.

NEVER use generic AI-generated aesthetics. Interpret creatively and make unexpected choices. Match implementation complexity to the aesthetic vision.

## Project-Specific Guardrails (LZ Sports)

When working on this repository, ALWAYS follow these rules:

1. **Theme Consistency First** — If the page is in a dark shell, all form controls must also be dark-themed. Never leave default Element Plus light backgrounds inside dark cards. Always set page-level design tokens (`--el-*`) at container/card level before component-level tweaks.

2. **Contrast Calibration (No Harsh White)** — Avoid pure white text for body and form labels on dark backgrounds. Use a 3-tier text system: primary muted-white, secondary gray-blue, placeholder deeper gray. Keep accent color vivid (orange) while reducing non-essential text brightness.

3. **Step/Status Pages Need Custom Completion States** — Do not rely on default `el-result` in dark scenes without restyling. Build custom completion blocks with explicit icon/title/subtitle/summary hierarchy.

4. **Layout Must Match Information Density** — Avoid forcing data-heavy pages into raw tables when narrative cards/timelines are clearer. Prefer "overview stats + filter controls + card stream" for notification-like content.

5. **Element Plus Override Strategy** — Use scoped wrappers (e.g., `.setup-form`) and `::deep` with explicit selector boundaries. For conflicting defaults, use targeted `!important` only on critical fields. Validate hover/focus/error/disabled states, not just resting state. See `.cursor/rules/frontend-dark-theme-tokens.mdc` for the canonical dark theme spec.

## Frontend QA Checklist (Run Before Finishing)

- Check page in all major states: default/loading/empty/error/completed
- Check visual consistency with related pages
- Check for "light patch" artifacts in dark theme (inputs, table cells, pagination, chips, dialogs)
- Check text readability (especially labels, placeholders, helper text)
- Check mobile breakpoint layout and control spacing
- Run lint and build after substantive style/layout changes

## Continuous Skill Update Rule

After each frontend redesign task, append a short "Design Retrospective" entry with:
- **Issue observed**
- **Root cause**
- **Fix applied**
- **Reusable rule** (how to prevent recurrence)

Keep each retrospective concise (4 bullets max).

## Design Retrospective Log

### 2026-04 Init + Notifications refresh
- **Issue**: Dark pages had bright/white default form blocks and low-quality completion-state readability.
- **Root cause**: Element Plus defaults were partially overriding dark card styles.
- **Fix**: Added container-level `--el-*` tokens, scoped `::deep` overrides, custom finish-state module; replaced plain notification table with hub layout.
- **Rule**: In dark themes, define tokens first, then component overrides; never ship default light components inside dark containers.

### 2026-04 Public scores visual polish
- **Issue**: Public scores page hierarchy was flat, with weak section distinction.
- **Root cause**: Layout lacked summary landmarks and card-level contrast.
- **Fix**: Upgraded to hero + metrics + card-stream structure, improved dark token layering.
- **Rule**: Data listing pages should include a compact metric band and explicit card boundaries.

### 2026-04 Public scores light-theme mismatch
- **Issue**: Public scores page looked detached in light shell because styles were hard-forced to dark.
- **Root cause**: Dark-only class and fixed dark color values without theme branching.
- **Fix**: Removed forced dark class; introduced semantic tokens with light defaults and `html.dark` overrides.
- **Rule**: For shared public pages, default to light tokens and gate dark variants through global theme selector.

### 2026-04 Public scores hierarchy refinement
- **Issue**: Page still looked like stacked generic blocks with weak status guidance.
- **Root cause**: Filter area lacked context/state affordance.
- **Fix**: Added status chip and overview cards, stronger hero metadata tags and responsive layout.
- **Rule**: For data-public pages, keep a three-layer structure: hero intent, filter+state feedback, then data stream cards.

### 2026-04 Select dropdown refinement
- **Issue**: Event select and dropdown looked like default Element Plus, visually detached.
- **Root cause**: Dropdown popper/options kept stock radius, spacing, and selection affordances.
- **Fix**: Added custom `popper-class`, redesigned wrapper focus/hover states, restyled option states in both light and dark.
- **Rule**: For key filters, style input shell and popper as one component system.

### 2026-04 Reusable select extraction
- **Issue**: Page-level select styling became large and hard to reuse.
- **Root cause**: Smart select visuals were implemented directly inside one page file.
- **Fix**: Extracted a reusable `SmartSelect` component with unified trigger/panel theming.
- **Rule**: Treat heavily customized Element Plus controls as shared design primitives.

### 2026-04 Multi-page SmartSelect rollout
- **Issue**: Other pages still mixed old Element Plus selects, causing visual inconsistency.
- **Root cause**: Component extraction was done, but migration was not propagated.
- **Fix**: Replaced selects across multiple pages (`score/index`, `user/index`) with `SmartSelect`.
- **Rule**: After extracting shared UI primitives, execute staged rollout by feature cluster.

### 2026-04 Full select migration completion
- **Issue**: Remaining admin/profile screens still used raw `el-select`, including remote and slot-rich dropdowns.
- **Root cause**: Initial `SmartSelect` only covered simple option arrays.
- **Fix**: Extended `SmartSelect` with default-slot passthrough and broader model typing, then fully replaced all page-level `el-select`.
- **Rule**: Shared wrapper components must support both simple-data mode and advanced slot mode before declaring migration complete.

### 2026-04 Legacy select CSS cleanup
- **Issue**: After migration, several pages still kept old select-specific overrides.
- **Root cause**: Original page styles were not removed during functional replacement.
- **Fix**: Deleted obsolete page-level select overrides and consolidated option sizing inside `SmartSelect`.
- **Rule**: After component migration, always run a second cleanup pass to remove dead selectors.

### 2026-04 Public scores list-first flow
- **Issue**: Public scores interaction depended on a top dropdown, which felt like a form filter.
- **Root cause**: Primary navigation object was hidden inside a select control.
- **Fix**: Replaced event dropdown with clickable event cards and added item-tab list for second-level navigation.
- **Rule**: For sequential lookup tasks (A -> B -> details), expose each level as visible list controls.

## Additional Resources

- Usage examples: [examples.md](examples.md)
