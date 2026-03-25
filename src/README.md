This document defines the internal conventions for working on this project.

The project has been under refactoring since January 2026.
The current goal is to complete the refactor and establish a stable API layer before May.

### Conventions

1. **Adapters**
   Adapters are responsible for bridging Farmer's Delight with SD/TFC systems.
   Their role is to make FD (vanilla based) understand TFC-based logic.

2. **TFC Integration**
   TFC-related logic can be referenced directly within the `common` package when appropriate.
   Avoid unnecessary abstraction layers.

3. **Mixins**
   Mixins should remain as minimal and focused as possible.
   Avoid embedding complex logic directly in mixins.

4. **Mixin Extras**
   Use Mixin Extras when it improves clarity or reduces complexity.
   Do not avoid it unnecessarily.

5. **FD Integration Strategy**
   The current design relies on invasive mixin-based modifications to Farmer's Delight internals.
   Do not refactor away from this approach unless there is a clear and justified improvement.