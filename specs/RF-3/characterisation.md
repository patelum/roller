# Characterisation: RF-3 Split HTML helpers out of Utilities

STATUS: VERIFIED. The orchestrator ran the util-config scoped build (`build.verify_module`) on 2026-10-12: `UtilitiesCharacterisationTest` 48 tests, 0 failures, 0 errors, against unmodified production code (base 336532b).

Suggested command (config.yaml `build.verify_module`, util-config scope):
`mvn -q -pl app -am test -Dtest='org/apache/roller/weblogger/util/**' -Dsurefire.failIfNoSpecifiedTests=false`

## Surface pinned

All via `Utilities.*` (`app/src/main/java/org/apache/roller/weblogger/util/Utilities.java`).

| Behaviour | Test | Existing or new |
| --- | --- | --- |
| escapeHTML(s), escapeHTML(s, bool): escaping, nbsp quirk, null/empty | `UtilitiesCharacterisationTest#escapeHtml*` | new |
| unescapeHTML: HTML4 entities, unknown entity, one level, null | `#unescapeHtml*` | new |
| removeHTML(s), removeHTML(s, bool): space/no space, null -> "", no tags -> untrimmed, unclosed tag, `<>`, trim | `#removeHtml*` | new (basic case also in `UtilitiesTest#testRemoveHTML`) |
| removeAndEscapeHTML: strip then escape, null -> "" | `#removeAndEscapeHtml*` | new |
| autoformat: `\n` -> `<br />`, `\r\n`, null, empty | `#autoformat*` | new |
| addNofollow: null/empty, link without rel unchanged, rel duplicated, non-href-first anchors, multiple links | `#addNofollow*` | new |
| extractHTML: tags only, no tags, null -> "", unclosed tag dropped | `#extractHtml*` | new (basic in `UtilitiesTest#testExtractHTML`) |
| truncate: cut at space / at upper, upper<lower, strips HTML always, boundary, null str, null suffix | `#truncate*` | new |
| truncateText: original returned when it fits (HTML kept), stripped when cut, null -> null | `#truncateText*` | new |
| truncateNicely: plain, fits, tag re-insertion, null -> NPE | `#truncateNicely*` | new (two cases also in `UtilitiesTest#testTruncateNicely1/2`) |
| transformToHTMLSubset: allowed tags case-insensitive, br forms, disallowed tags stay escaped, `&quot;`, double-escaped entities, link variants, null/empty | `#transformSubset*` | new (links/formatting also in `UtilitiesTest#testHtmlSubset*`) |

Private helpers reached only through the above: `restoreSubsetEntities` and `replace` (via transformToHTMLSubset), `mLinkPattern` (addNofollow), the `*_TAG_PATTERN`, `A_HREF_PATTERN`, `SUBSET_LINK_PATTERN`, `QUOTE_PATTERN` constants.

## Private helper exclusivity (for the move)

Verified by grep of Utilities.java:
- `replace` (:1047): called only from transformToHTMLSubset. Exclusive, safe to move.
- `restoreSubsetEntities` (:1040): called only from transformToHTMLSubset. Exclusive.
- `stripLineBreaks` (:734): called only from `removeVisibleHTMLTags`. Exclusive.
- `removeVisibleHTMLTags` (:760): DEAD CODE. Its only call site is commented out (:682). Exclusive, may move or be deleted; unreachable so not testable.
- Pattern constants `mLinkPattern` and the `*_TAG_PATTERN`/`A_HREF`/`SUBSET_LINK`/`QUOTE` constants (:62-108): used only by addNofollow and transformToHTMLSubset. Exclusive.
- Shared with out-of-scope methods: none of the private helpers. Not shared and not to be moved: `mLogger` (:58, used by encodePassword at :523) and `TAG_SPLIT_CHARS` (:60). replaceNonAlphanumeric/removeNonAlphanumeric use no private helper from this set.
- Public cross-calls inside scope: removeAndEscapeHTML -> removeHTML, escapeHTML; truncate*/truncateNicely -> removeHTML; truncateNicely -> extractHTML. Moving them together keeps these intra-class.

## Test classes (do not modify during the refactor)

- `/home/claude/roller/app/src/test/java/org/apache/roller/weblogger/util/UtilitiesCharacterisationTest.java`
- Existing, also relevant: `/home/claude/roller/app/src/test/java/org/apache/roller/weblogger/util/UtilitiesTest.java`

## Looks like a bug (flagged, not fixed)

- addNofollow adds `rel="nofollow"` only to links that ALREADY contain it (duplicating it) and leaves all other links unchanged; the condition is inverted. The commented-out test in UtilitiesTest expects the opposite. — `Utilities.java:256`
- addNofollow only matches anchors written `<a href=...>` (href first, one space); `<a class=".." href=..>` is skipped. — `Utilities.java:62`
- truncateNicely(null, ...) throws NullPointerException (str.length()), while truncate(null) returns "" and truncateText(null) returns null. Inconsistent null handling. — `Utilities.java:635`
- truncate() always strips HTML, truncateText() returns the original (HTML intact) when it fits but stripped text when cut. — `Utilities.java:594`, `:725`
- truncate with a null appendToEnd appends the literal "null". — `Utilities.java:611`
- escapeHTML(s, true) turns `&nbsp;` into `&amp;nbsp;` because the ampersand pass runs first; only the false variant yields a space. — `Utilities.java:146-148`
- removeHTML returns input untrimmed when it contains no `<`, but trimmed otherwise. — `Utilities.java:184`
- removeHTML does not require a matching `>` after `<`; `a < b` text is kept with a doubled space. — `Utilities.java:205`
- removeVisibleHTMLTags/stripLineBreaks are unreachable dead code. — `Utilities.java:682`, `:734`, `:760`

## Not covered

- removeVisibleHTMLTags and stripLineBreaks: private and unreachable (only call is commented out); cannot be tested through the public surface.
- Deprecated-delegate annotations themselves: not observable by behaviour tests.
- Concurrency / very large inputs / performance (the javadoc warns removeVisibleHTMLTags is slow): out of scope.
- replaceNonAlphanumeric/removeNonAlphanumeric and other Utilities methods: not in request.md.
