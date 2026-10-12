# Review: RF-3 Split HTML helpers out of Utilities

**Verdict:** approve
**Diff:** 2 main files (`Utilities.java` modified, `HtmlUtilities.java` new, 583 lines) plus the new test `UtilitiesCharacterisationTest.java`, against `336532b`. `Utilities.java` is +45 / −485. No pom, protected-path or other file changes (`git status`: only these three plus `specs/RF-3/`).
**Build:** `mvn -q -o -pl app -am test -Dtest='org/apache/roller/util/**,org/apache/roller/testing/**,org/apache/roller/weblogger/util/**' -Dsurefire.failIfNoSpecifiedTests=false` → green (re-run by the reviewer; no failures). The full `mvn -q verify` was not re-run here. `status.md` logs it green at 07:30.

## Summary for approval card

Moves the 11 HTML and truncation helpers, with their private helpers and regex constants, from `Utilities` into the new `HtmlUtilities`. Every public `Utilities` method stays as a `@Deprecated` one-line delegate.
Verified by reading each moved body against its original at `336532b` (identical apart from one planned qualifier change) and by checking all 40 public signatures. The 48 characterisation tests plus `UtilitiesTest` are green.
Decision for the human: accept the dead helpers `removeVisibleHTMLTags` and `stripLineBreaks` living on as unused private code in `HtmlUtilities`, or schedule their deletion as a follow-up.

## Must-fix

None.

## Consider

- `HtmlUtilities.java:377-468` — `stripLineBreaks` and `removeVisibleHTMLTags` are unreachable private code. The commented-out call at `:324-325` is dead too. The plan deliberately kept them verbatim (plan.md "Decisions"). Raise a follow-up ticket to delete them. Moving them to a new file only makes the dead code harder to notice.
- `specs/RF-3/plan.md` — step 6 is still unchecked and the Coverage "Result" cell is empty. `status.md` records the full verify as green, so this is bookkeeping only. Tick the box and fill in the result before G2.
- `Utilities.java:81-141` etc. — the delegate javadocs lose the original descriptions (for example "Escape, but do not replace HTML"). The plan says to shorten them, and the descriptions live on `HtmlUtilities`. The `@deprecated` tags make the redirect clear. No action needed unless Velocity or IDE users rely on the old text.

## Checks performed (accidental behaviour change)

- **Method bodies.** I compared each of these against the original (`Utilities.java:132-267`, `:582-862`, `:986-1051` at the base commit): `escapeHTML` x2, `unescapeHTML`, `removeHTML` x2, `removeAndEscapeHTML`, `autoformat`, `addNofollow`, `truncate`, `truncateNicely`, `truncateText`, `extractHTML`, `transformToHTMLSubset`, `restoreSubsetEntities`, `replace`, `stripLineBreaks` and `removeVisibleHTMLTags`.
  - They are statement-for-statement identical. Only the javadoc trailing whitespace differs.
  - Exception types and null handling are preserved: `truncateNicely(null)` still throws an NPE at `str.length()`, `truncate` still appends the literal "null" for a null suffix, and `removeHTML(null)` still returns "".
  - `addNofollow` still has the inverted `rel="nofollow"` condition (`HtmlUtilities.java:209`), which the characterisation test pins.
- **Regex constants.** All 24 pattern constants (`HtmlUtilities.java:32-78`) match `Utilities.java:62-108` exactly: the patterns, the flags (`CASE_INSENSITIVE`, `DOTALL` on `OPENING_A_TAG_PATTERN`) and the escaping. They are `private static final`, as in the original.
- **Internal call change.** `removeAndEscapeHTML` now calls `escapeHTML(removeHTML(s))` (`HtmlUtilities.java:182`) instead of `Utilities.escapeHTML(Utilities.removeHTML(s))` (original `:229`). The behaviour is the same, because `Utilities.escapeHTML` is now a straight delegate to `HtmlUtilities.escapeHTML`. Static methods cannot be overridden, so there is no dispatch difference. It also avoids a delegate round trip.
- **Public API of `Utilities`.** `public static` declarations went from 40 to 40 (`TAG_SPLIT_CHARS` and `stripJsessionId` count among them). I checked each name and parameter list, including both `escapeHTML`, both `removeHTML`, both `replaceNonAlphanumeric`, both `copyInputToOutput` and `truncateText`, and none is missing or changed. Order is also unchanged, apart from `truncateText` sitting directly after `truncateNicely`, which is its original position.
- **Delegates.** Each moved method is `@Deprecated` with a `@deprecated` javadoc tag and a body of exactly `return HtmlUtilities.<name>(<args>);`, with the parameter names forwarded unchanged. The class stays public with its implicit constructor, so reflective and Velocity use is unaffected.
- **Scope and boundaries.**
  - Removed imports (`java.util.regex.*`, `StringEscapeUtils`) have no remaining use in `Utilities.java` (grep clean). `StringUtils`, `mLogger` and `TAG_SPLIT_CHARS` are kept.
  - `HtmlUtilities` imports only `java.util.regex`, commons-lang3 and commons-text, all already used in this module. There is no new dependency, no cross-module import and no schema or pom change.
  - Callers are not migrated. The remaining `Utilities` code does not call the moved methods, so there are no self-deprecation warnings.
- **Tests.** The new `UtilitiesCharacterisationTest` has 48 `@Test` methods and matches the verified-green count in `characterisation.md`. The `UtilitiesTest` diff is empty, so it is unchanged. No test was edited during the refactor.

## Coverage

Refactor track, so there are no ACs. Behaviour is pinned by `UtilitiesCharacterisationTest` (48 tests, unchanged since step 0) and `UtilitiesTest` (unchanged). Both pass against the refactored code through the `Utilities` delegates.

Not observable by tests, per `characterisation.md` "Not covered": the dead private helpers and the `@Deprecated` annotations. I covered these by reading the code.
