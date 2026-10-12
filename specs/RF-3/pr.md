# Split HTML helpers out of Utilities (RF-3)

## Summary

Moves the HTML and text-truncation helpers out of `Utilities` into a new `HtmlUtilities` class in the same package. Every moved public method stays on `Utilities` as a `@Deprecated` one-line delegate, so no caller, plugin or Velocity template changes. No behaviour change.

## Details

- Spec / request: `specs/RF-3/request.md`
- Plan: `specs/RF-3/plan.md`
- Modules: util-config
- Files: `Utilities.java` (+45/-485), `HtmlUtilities.java` (new), `UtilitiesCharacterisationTest.java` (new)

## Verification

- `mvn -q verify` → green
- Tests added: `UtilitiesCharacterisationTest` (48 tests, calling through `Utilities`, written and passing before any production change)
- Review (`specs/RF-3/review.md`): 0 must-fix, 3 consider. Moved bodies and all 24 regex constants identical to the originals; 40 public signatures on `Utilities` intact.

## Risks and follow-ups

- `removeVisibleHTMLTags` and `stripLineBreaks` are dead code, moved as-is. A follow-up ticket can delete them.
- Existing quirks are preserved on purpose and pinned by tests: `addNofollow` condition is inverted, `truncateNicely(null)` throws an NPE, `truncate` with a null suffix appends "null". These are candidates for separate defect tickets.
- Delegate javadocs are shortened; the original descriptions live on `HtmlUtilities`.
- Callers are not migrated, as the request says.
