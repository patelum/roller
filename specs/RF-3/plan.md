# Plan: RF-3 Split HTML helpers out of Utilities

Scope: pure move. New `HtmlUtilities` in `org.apache.roller.weblogger.util` (same package, same logical module `util-config`). Every public method of `Utilities` keeps its exact signature and becomes a `@Deprecated` one-line delegate. No caller migration, no new dependency, no pom/schema change, no protected path touched, characterisation tests (`UtilitiesCharacterisationTest`, `UtilitiesTest`) are not edited.

Decisions (closed):
- **Dead helpers `removeVisibleHTMLTags` and `stripLineBreaks`** (`Utilities.java:734`, `:760`; only call is commented out at `:682`): move verbatim to `HtmlUtilities` as `private static`, together with the commented-out call line inside `truncateNicely`. Rationale: "no behaviour change" and the request's "private helpers only they use" are satisfied literally; deleting dead code is a separate decision for a later ticket. Consequence: javac may not warn (private unused methods are not linted by `-Xlint:all`, root `pom.xml:68`); acceptable.
- **Shared constants:** move all pattern constants (`Utilities.java:62-108`) as `private static final` to `HtmlUtilities`; they are used only by `addNofollow` and `transformToHTMLSubset` (characterisation.md "Private helper exclusivity"). `mLogger` (`:58`) and `TAG_SPLIT_CHARS` (`:60`) stay in `Utilities`.
- **Private helpers `replace`, `restoreSubsetEntities`:** move with `transformToHTMLSubset` (exclusive to it, `:1040`, `:1047`).
- **Intra-class calls:** `removeAndEscapeHTML`, `truncate*`, `truncateNicely` call `removeHTML`/`escapeHTML`/`extractHTML`; in `HtmlUtilities` they call each other directly (rewrite `Utilities.escapeHTML(...)` at `:229` to `escapeHTML(...)`). No call in the remaining `Utilities` code uses the moved methods (checked: only moved methods call each other).
- **Deprecation form:** each delegate gets `@Deprecated` plus a javadoc `@deprecated Use {@link HtmlUtilities#name}` tag (needed so `-Xlint:all` `dep-ann` stays quiet; lint is warnings-only, no `-Werror`, `pom.xml:68`). Original javadoc text is kept on `HtmlUtilities`; the delegate javadoc is shortened.
- **Delegate body:** exactly `return HtmlUtilities.sameName(args);` for each overload.

Verify command for every step (`build.verify_module`, module `util-config`):
`mvn -q -pl app -am test -Dtest='org/apache/roller/util/**,org/apache/roller/testing/**,org/apache/roller/weblogger/util/**' -Dsurefire.failIfNoSpecifiedTests=false`

Baseline note: the net was verified green on the untouched tree (step 0). Steps 2-4 stay separate (decision: keep, for easier diagnosis).

## Steps

- [x] **0. Baseline the safety net (no edits)** — DONE by orchestrator at 07:01: scoped build green on unmodified production code, `UtilitiesCharacterisationTest` 48 tests, 0 failures. No assertion corrections were needed; the characterisation test is frozen from here on.
  - Files: none
  - Module: util-config
  - Done when: module build green on unmodified production code; characterisation net confirmed against real behaviour.

- [x] **1. Create `HtmlUtilities` with copied code (Utilities untouched)**
  - Change: Add `public class HtmlUtilities` (ASF licence header, javadoc "HTML and text-truncation helpers") containing verbatim copies of: `escapeHTML` (x2), `unescapeHTML`, `removeHTML` (x2), `removeAndEscapeHTML`, `autoformat`, `addNofollow`, `truncate`, `truncateNicely`, `truncateText`, `extractHTML`, `transformToHTMLSubset`; private `restoreSubsetEntities`, `replace`, `stripLineBreaks`, `removeVisibleHTMLTags`; and the private pattern constants `Utilities.java:62-108`. Only edit: `Utilities.escapeHTML/removeHTML` qualifiers in `removeAndEscapeHTML` become unqualified. Imports needed: `java.util.regex.Matcher`, `java.util.regex.Pattern`, `org.apache.commons.text.StringEscapeUtils`, `org.apache.commons.lang3.StringUtils` (all already used by the module, no new library). Add private constructor? No: `Utilities` has none and templates/reflective use is possible; keep the implicit public constructor for parity.
  - Files: `app/src/main/java/org/apache/roller/weblogger/util/HtmlUtilities.java` (new)
  - Module: util-config
  - Done when: module builds and all existing util tests still pass (duplicate code only, no behavioural path changed).

- [x] **2. Delegate group A: escaping, stripping, simple formatting**
  - Change: In `Utilities`, replace bodies with `@Deprecated` one-line delegates for `escapeHTML(String)`, `escapeHTML(String, boolean)`, `unescapeHTML`, `removeHTML(String)`, `removeHTML(String, boolean)`, `removeAndEscapeHTML`, `autoformat`, `addNofollow`, `extractHTML`. Signatures and order unchanged. Do not yet delete constants/helpers.
  - Files: `app/src/main/java/org/apache/roller/weblogger/util/Utilities.java`
  - Module: util-config
  - Done when: module green; `UtilitiesCharacterisationTest` (`escapeHtml*`, `unescapeHtml*`, `removeHtml*`, `removeAndEscapeHtml*`, `autoformat*`, `addNofollow*`, `extractHtml*`) and `UtilitiesTest` pass unchanged.

- [x] **3. Delegate group B: truncation**
  - Change: Convert `truncate`, `truncateNicely`, `truncateText` in `Utilities` to `@Deprecated` one-line delegates (keep the NPE-on-null behaviour of `truncateNicely` and the "null" suffix quirk: they come for free because logic is identical in `HtmlUtilities`).
  - Files: `app/src/main/java/org/apache/roller/weblogger/util/Utilities.java`
  - Module: util-config
  - Done when: module green; `UtilitiesCharacterisationTest#truncate*`, `#truncateText*`, `#truncateNicely*` and `UtilitiesTest#testTruncateNicely1/2` pass unchanged.

- [x] **4. Delegate group C: HTML subset transform**
  - Change: Convert `transformToHTMLSubset` in `Utilities` to a `@Deprecated` one-line delegate.
  - Files: `app/src/main/java/org/apache/roller/weblogger/util/Utilities.java`
  - Module: util-config
  - Done when: module green; `UtilitiesCharacterisationTest#transformSubset*` and `UtilitiesTest#testHtmlSubset*` pass unchanged.

- [x] **5. Remove now-unreferenced code from `Utilities`**
  - Change: Delete from `Utilities` the pattern constants (`:62-108`), `restoreSubsetEntities`, `replace`, `stripLineBreaks`, `removeVisibleHTMLTags`, and imports that become unused (`java.util.regex.Matcher`, `java.util.regex.Pattern`, `org.apache.commons.text.StringEscapeUtils`; keep `StringUtils`, still used by `stringToStringArray`, `hexEncode`, `splitStringAsTags` etc., and keep `mLogger`/`TAG_SPLIT_CHARS`). Before deleting, grep the file to confirm each removed symbol has no remaining reference. Public API of `Utilities` stays identical (compare `javap -public` or a diff of public signatures before/after).
  - Files: `app/src/main/java/org/apache/roller/weblogger/util/Utilities.java`
  - Module: util-config
  - Done when: module green with no compile errors from missing symbols; `Utilities.java` has no `Pattern`/`Matcher` usage; public method list unchanged.

- [x] **6. Final gate** (full `mvn -q verify` green, 2026-10-12)
  - Change: Run full `mvn -q verify` (required before calling a refactor done, AGENTS.md rule 3). Confirm other modules compile against the unchanged `Utilities` signatures (callers, Velocity templates and plugins untouched). Confirm `git status` shows only `HtmlUtilities.java` (new) and `Utilities.java` (modified) in main code; no pom, protected-path or test-file changes (except a step 0 assertion fix, if any). Do not commit.
  - Files: none
  - Module: whole build (`build.verify`)
  - Done when: `mvn -q verify` succeeds.

## Risks and notes

- Riskiest step is 1 (largest copy). A transcription slip would be caught only after step 2-4 switch delegation, so each group re-runs the net; diffing the moved method bodies against the original (`git diff --no-index` of extracted ranges) is recommended in step 1.
- Steps 2-4 are ordered cheap-to-expensive-to-diagnose: group C last because it has the most intricate regex logic and the most characterisation cases.
- Step 0 depends on Bash availability; the net was unverified when written (characterisation.md line 3).
- Step count is 7 (0-6), within the 3-8 guidance; steps 2-4 may be merged if the reviewer prefers fewer builds.
- Known quirks (inverted `addNofollow` condition, `escapeHTML` nbsp, `truncateNicely(null)` NPE, etc., characterisation.md "Looks like a bug") are deliberately preserved.

## Coverage

Refactor track: no ACs. Behaviour is pinned by `UtilitiesCharacterisationTest` and `UtilitiesTest`, which must stay green and unmodified in steps 1-6.

| AC | Step(s) | Test | Result |
| --- | --- | --- | --- |
| Behaviour unchanged (all moved methods) | 1-6 | `UtilitiesCharacterisationTest`, `UtilitiesTest` | pass (full verify green) |
