---
name: frontend-design
description: Create distinctive, production-grade frontend interfaces with high design quality. Use this skill when the user asks to build web components, pages, artifacts, posters, or applications (examples include websites, landing pages, dashboards, React components, HTML/CSS layouts, or when styling/beautifying any web UI). Generates creative, polished code and UI design that avoids generic AI aesthetics.
license: Complete terms in LICENSE.txt
---

This skill guides creation of distinctive, production-grade frontend interfaces that avoid generic "AI slop" aesthetics. Implement real working code with exceptional attention to aesthetic details and creative choices.

The user provides frontend requirements: a component, page, application, or interface to build. They may include context about the purpose, audience, or technical constraints.

## Design Thinking

Before coding, understand the context and commit to a BOLD aesthetic direction:
- **Purpose**: What problem does this interface solve? Who uses it?
- **Tone**: Pick an extreme: brutally minimal, maximalist chaos, retro-futuristic, organic/natural, luxury/refined, playful/toy-like, editorial/magazine, brutalist/raw, art deco/geometric, soft/pastel, industrial/utilitarian, etc. There are so many flavors to choose from. Use these for inspiration but design one that is true to the aesthetic direction.
- **Constraints**: Technical requirements (framework, performance, accessibility).
- **Differentiation**: What makes this UNFORGETTABLE? What's the one thing someone will remember?

**CRITICAL**: Choose a clear conceptual direction and execute it with precision. Bold maximalism and refined minimalism both work - the key is intentionality, not intensity.

Then implement working code (HTML/CSS/JS, React, Vue, etc.) that is:
- Production-grade and functional
- Visually striking and memorable
- Cohesive with a clear aesthetic point-of-view
- Meticulously refined in every detail

## Frontend Aesthetics Guidelines

Focus on:
- **Typography**: Choose fonts that are beautiful, unique, and interesting. Avoid generic fonts like Arial and Inter; opt instead for distinctive choices that elevate the frontend's aesthetics; unexpected, characterful font choices. Pair a distinctive display font with a refined body font.
- **Color & Theme**: Commit to a cohesive aesthetic. Use CSS variables for consistency. Dominant colors with sharp accents outperform timid, evenly-distributed palettes.
- **Motion**: Use animations for effects and micro-interactions. Prioritize CSS-only solutions for HTML. Use Motion library for React when available. Focus on high-impact moments: one well-orchestrated page load with staggered reveals (animation-delay) creates more delight than scattered micro-interactions. Use scroll-triggering and hover states that surprise.
- **Spatial Composition**: Unexpected layouts. Asymmetry. Overlap. Diagonal flow. Grid-breaking elements. Generous negative space OR controlled density.
- **Backgrounds & Visual Details**: Create atmosphere and depth rather than defaulting to solid colors. Add contextual effects and textures that match the overall aesthetic. Apply creative forms like gradient meshes, noise textures, geometric patterns, layered transparencies, dramatic shadows, decorative borders, custom cursors, and grain overlays.

NEVER use generic AI-generated aesthetics like overused font families (Inter, Roboto, Arial, system fonts), cliched color schemes (particularly purple gradients on white backgrounds), predictable layouts and component patterns, and cookie-cutter design that lacks context-specific character.

Interpret creatively and make unexpected choices that feel genuinely designed for the context. No design should be the same. Vary between light and dark themes, different fonts, different aesthetics. NEVER converge on common choices (Space Grotesk, for example) across generations.

**IMPORTANT**: Match implementation complexity to the aesthetic vision. Maximalist designs need elaborate code with extensive animations and effects. Minimalist or refined designs need restraint, precision, and careful attention to spacing, typography, and subtle details. Elegance comes from executing the vision well.

Remember: Claude is capable of extraordinary creative work. Don't hold back, show what can truly be created when thinking outside the box and committing fully to a distinctive vision.

## Project-Specific Guardrails (LZ Sports)

When working on this repository, ALWAYS follow these rules to avoid repeated design regressions:

1. **Theme Consistency First**
   - If the page is in a dark shell, all form controls (inputs, radio, select, buttons, tables, result/empty states) must also be dark-themed.
   - Never leave default Element Plus light backgrounds inside dark cards.
   - Always set page-level design tokens (`--el-*`) at container/card level before component-level tweaks.

2. **Contrast Calibration (No Harsh White)**
   - Avoid pure white text for body and form labels on dark backgrounds.
   - Use a 3-tier text system: primary muted-white, secondary gray-blue, placeholder deeper gray.
   - Keep accent color vivid (orange) while reducing non-essential text brightness.

3. **Step/Status Pages Need Custom Completion States**
   - Do not rely on default `el-result` in dark scenes without restyling.
   - Build custom completion blocks with explicit icon/title/subtitle/summary hierarchy.
   - Ensure “success/completed/inactive” states are visually unambiguous.

4. **Layout Must Match Information Density**
   - Avoid forcing data-heavy pages into raw tables when narrative cards/timelines are clearer.
   - Prefer “overview stats + filter controls + card stream” for notification-like content.
   - Add clear empty state and responsive collapse behavior by default.

5. **Element Plus Override Strategy**
   - Use scoped wrappers (e.g., `.setup-form`) and `::deep` with explicit selector boundaries.
   - For conflicting defaults, use targeted `!important` only on critical fields (background, border, text).
   - Validate hover/focus/error/disabled states, not just resting state.

## Frontend QA Checklist (Run Before Finishing)

- Check page in all major states: default/loading/empty/error/completed.
- Check visual consistency with related pages (home/login/dashboard style language).
- Check for “light patch” artifacts in dark theme (inputs, table cells, pagination, chips, dialogs).
- Check text readability (especially labels, placeholders, helper text).
- Check mobile breakpoint layout and control spacing.
- Run lint and build after substantive style/layout changes.

## Continuous Skill Update Rule

After each frontend redesign task in this repo, append a short “Design Retrospective” entry to this skill with:

- **Issue observed** (what looked wrong)
- **Root cause** (why it happened: token leakage, default styles, hierarchy mismatch, etc.)
- **Fix applied** (specific pattern/selector/layout decision)
- **Reusable rule** (how to prevent recurrence)

Keep each retrospective concise (4 bullets max). Do this automatically at the end of future frontend design tasks.

## Cursor Rule Integration (Persistent Memory)

This repo also uses a Cursor project rule to keep the “container-level tokens + dark theme overrides” standard consistent:

- Rule file: `.cursor/rules/frontend-dark-theme-tokens.mdc`
- Scope: `lz_sports_frontend/src/**/*`

When working on matching files, follow that rule automatically.

## Design Retrospective Log

### 2026-04 Init + Notifications refresh
- **Issue observed**: Dark pages had bright/white default form blocks and low-quality completion-state readability.
- **Root cause**: Element Plus defaults were partially overriding dark card styles; hierarchy relied on default components not tuned for dark UI.
- **Fix applied**: Added container-level `--el-*` tokens, scoped `::deep` overrides for form controls/states, and custom finish-state module for step page; replaced plain notification table with hub layout (hero + stats + filters + message stream).
- **Reusable rule**: In dark themes, define tokens first, then component overrides; never ship default light components inside dark containers.

### 2026-04 Public scores visual polish
- **Issue observed**: Public scores page hierarchy was flat, with weak section distinction between header, filters, and score groups.
- **Root cause**: Layout lacked summary landmarks and card-level contrast; dark-mode Element Plus states were only partially refined.
- **Fix applied**: Upgraded to hero + metrics + card-stream structure, improved dark token layering, and added focused overrides for select/table hover/focus states.
- **Reusable rule**: Data listing pages should include a compact metric band and explicit card boundaries before table rendering to preserve scanability in dark themes.

### 2026-04 Public scores light-theme mismatch
- **Issue observed**: Public scores page looked detached in light shell because styles were hard-forced to dark.
- **Root cause**: Root node included dark-only class and many fixed dark color values without theme branching.
- **Fix applied**: Removed forced dark class; introduced semantic tokens with light defaults and `html.dark` overrides.
- **Reusable rule**: For shared public pages, default to light tokens and gate dark variants through global theme selector only.

### 2026-04 Public scores hierarchy refinement
- **Issue observed**: Even after theme fix, page still looked like stacked generic blocks with weak status guidance.
- **Root cause**: Filter area lacked context/state affordance and page missed a compact information band between hero and data table.
- **Fix applied**: Added status chip and overview cards (event pool/current items/context), plus stronger hero metadata tags and responsive layout tuning.
- **Reusable rule**: For data-public pages, keep a three-layer structure: hero intent, filter+state feedback, then data stream cards.

### 2026-04 Select dropdown refinement
- **Issue observed**: Event select and dropdown looked like default Element Plus, visually detached from page tone.
- **Root cause**: Input wrapper was partially themed, but dropdown popper/options kept stock radius, spacing, and selection affordances.
- **Fix applied**: Added custom `popper-class`, redesigned wrapper focus/hover states, and restyled option hover/selected states in both light and dark.
- **Reusable rule**: For key filters, style input shell and popper as one component system; never theme only the trigger without dropdown states.

### 2026-04 Reusable select extraction
- **Issue observed**: Page-level select styling became large and hard to reuse across screens.
- **Root cause**: Smart select visuals (trigger + dropdown) were implemented directly inside one page file.
- **Fix applied**: Extracted a reusable `SmartSelect` component with unified trigger/panel theming and configurable options mapping.
- **Reusable rule**: Treat heavily customized Element Plus controls as shared design primitives, not one-off page CSS blocks.

### 2026-04 Multi-page SmartSelect rollout
- **Issue observed**: Other management pages still mixed old Element Plus selects, causing visual inconsistency.
- **Root cause**: Component extraction was done, but migration was not propagated to adjacent pages.
- **Fix applied**: Replaced selects in first batch (`score/index`, `user/index`) with `SmartSelect`, including dialog usage.
- **Reusable rule**: After extracting shared UI primitives, execute staged rollout by feature cluster and verify each batch with lint/tests.

### 2026-04 Full select migration completion
- **Issue observed**: Remaining admin/profile screens still used raw `el-select`, including remote and slot-rich dropdowns.
- **Root cause**: Initial `SmartSelect` only covered simple option arrays, blocking complex replacements.
- **Fix applied**: Extended `SmartSelect` with default-slot passthrough and broader model typing, then fully replaced all page-level `el-select`.
- **Reusable rule**: Shared wrapper components must support both simple-data mode and advanced slot mode before declaring migration complete.

### 2026-04 Legacy select CSS cleanup
- **Issue observed**: After migration, several pages still kept old select-specific overrides, increasing CSS noise.
- **Root cause**: Original page styles targeted `el-select` wrappers directly and were not removed during functional replacement.
- **Fix applied**: Deleted obsolete page-level select overrides and consolidated option sizing behavior inside `SmartSelect`.
- **Reusable rule**: After component migration, always run a second cleanup pass to remove dead selectors and centralize style ownership.

### 2026-04 Public scores list-first flow
- **Issue observed**: Public scores interaction depended on a top dropdown, which felt like a form filter rather than an operational browse flow.
- **Root cause**: Primary navigation object (event -> item) was hidden inside a select control instead of explicit list affordances.
- **Fix applied**: Replaced event dropdown with clickable event cards and added item-tab list for second-level navigation before table details.
- **Reusable rule**: For sequential lookup tasks (A -> B -> details), expose each level as visible list controls instead of nested form selects.

## Additional Resources

- Usage examples: [examples.md](examples.md)
- License terms: [LICENSE.txt](LICENSE.txt)
