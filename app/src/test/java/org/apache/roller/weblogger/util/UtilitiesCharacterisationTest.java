/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Characterisation tests pinning the CURRENT behaviour of the HTML and
 * text-truncation helpers on {@link Utilities}, quirks included. They call
 * through Utilities so they also guard any delegates. Do not modify them
 * during the refactor; if one fails, behaviour changed.
 */
public class UtilitiesCharacterisationTest {

    // ---- escapeHTML -------------------------------------------------------

    @Test
    public void escapeHtmlEscapesAmpersandQuotesAndAngleBrackets() {
        assertEquals("a &amp; b &lt;c&gt; &quot;d&quot;",
                Utilities.escapeHTML("a & b <c> \"d\""));
    }

    @Test
    public void escapeHtmlWithoutAmpersandLeavesAmpersandsAlone() {
        assertEquals("a & b &lt;c&gt; &quot;d&quot; &amp;",
                Utilities.escapeHTML("a & b <c> \"d\" &amp;", false));
    }

    @Test
    public void escapeHtmlNbspHandling() {
        // with ampersand escaping the entity is mangled before nbsp is seen
        assertEquals("a&amp;nbsp;b", Utilities.escapeHTML("a&nbsp;b"));
        // without it, &nbsp; becomes a plain space
        assertEquals("a b", Utilities.escapeHTML("a&nbsp;b", false));
    }

    @Test
    public void escapeHtmlNullAndEmpty() {
        assertNull(Utilities.escapeHTML(null));
        assertNull(Utilities.escapeHTML(null, false));
        assertEquals("", Utilities.escapeHTML(""));
        assertEquals("plain text", Utilities.escapeHTML("plain text"));
    }

    // ---- unescapeHTML -----------------------------------------------------

    @Test
    public void unescapeHtmlDecodesHtml4Entities() {
        assertEquals("<b> & \"x\" A   é",
                Utilities.unescapeHTML("&lt;b&gt; &amp; &quot;x&quot; &#65; &nbsp; &eacute;"));
    }

    @Test
    public void unescapeHtmlLeavesUnknownEntitiesAndHandlesNullEmpty() {
        assertEquals("&foo; &", Utilities.unescapeHTML("&foo; &"));
        assertEquals("", Utilities.unescapeHTML(""));
        assertNull(Utilities.unescapeHTML(null));
    }

    @Test
    public void unescapeHtmlDecodesOnlyOneLevel() {
        assertEquals("&lt;", Utilities.unescapeHTML("&amp;lt;"));
    }

    // ---- removeHTML -------------------------------------------------------

    @Test
    public void removeHtmlReplacesTagsWithSpaceByDefault() {
        assertEquals("hi  there", Utilities.removeHTML("<b>hi</b> there"));
        assertEquals("hi  there", Utilities.removeHTML("<b>hi</b> there", true));
    }

    @Test
    public void removeHtmlWithoutSpaceJoinsText() {
        assertEquals("hi there", Utilities.removeHTML("<b>hi</b> there", false));
        assertEquals("ab", Utilities.removeHTML("a<br>b", false));
        assertEquals("a b", Utilities.removeHTML("a<br>b", true));
    }

    @Test
    public void removeHtmlNullAndEmptyReturnEmptyString() {
        assertEquals("", Utilities.removeHTML(null));
        assertEquals("", Utilities.removeHTML(null, false));
        assertEquals("", Utilities.removeHTML(""));
    }

    @Test
    public void removeHtmlWithoutTagsReturnsInputUntrimmed() {
        assertEquals("  abc  ", Utilities.removeHTML("  abc  "));
    }

    @Test
    public void removeHtmlTrimsWhenTagsPresent() {
        assertEquals("x", Utilities.removeHTML("  <b>x</b>  ", false));
    }

    @Test
    public void removeHtmlUnclosedTagIsKept() {
        assertEquals("a  < b", Utilities.removeHTML("a < b"));
        assertEquals("a < b", Utilities.removeHTML("a < b", false));
    }

    @Test
    public void removeHtmlEmptyTagAndLeadingTag() {
        assertEquals("", Utilities.removeHTML("<>"));
        assertEquals("text", Utilities.removeHTML("<p>text", false));
        assertEquals("text", Utilities.removeHTML("text<p>", false));
    }

    @Test
    public void removeHtmlKeepsEntitiesUntouched() {
        assertEquals("a &amp; b &lt;", Utilities.removeHTML("a &amp; b &lt;<i></i>", false));
    }

    // ---- removeAndEscapeHTML ----------------------------------------------

    @Test
    public void removeAndEscapeHtmlStripsThenEscapes() {
        assertEquals("a &amp; b", Utilities.removeAndEscapeHTML("<b>a & b</b>"));
        assertEquals("&quot;q&quot;  &lt;", Utilities.removeAndEscapeHTML("\"q\" <"));
    }

    @Test
    public void removeAndEscapeHtmlNullAndEmpty() {
        assertEquals("", Utilities.removeAndEscapeHTML(null));
        assertEquals("", Utilities.removeAndEscapeHTML(""));
    }

    // ---- autoformat -------------------------------------------------------

    @Test
    public void autoformatReplacesNewlinesWithBr() {
        assertEquals("a<br />b<br />", Utilities.autoformat("a\nb\n"));
        assertEquals("a\r<br />b", Utilities.autoformat("a\r\nb"));
        assertEquals("no newline", Utilities.autoformat("no newline"));
    }

    @Test
    public void autoformatNullAndEmpty() {
        assertNull(Utilities.autoformat(null));
        assertEquals("", Utilities.autoformat(""));
    }

    // ---- addNofollow ------------------------------------------------------

    @Test
    public void addNofollowNullAndEmptyReturnedAsIs() {
        assertNull(Utilities.addNofollow(null));
        assertEquals("", Utilities.addNofollow(""));
    }

    @Test
    public void addNofollowLeavesLinkWithoutRelUnchanged() {
        // looks like an inverted condition: see characterisation.md
        String html = "<p>text <a href=\"http://example.com\">link</a>";
        assertEquals(html, Utilities.addNofollow(html));
        String upper = "<p>text <A href=\"http://example.com\">link</a>";
        assertEquals(upper, Utilities.addNofollow(upper));
    }

    @Test
    public void addNofollowDuplicatesRelWhenAlreadyPresent() {
        assertEquals("<a href=\"x\" rel=\"nofollow\" rel=\"nofollow\">l</a>",
                Utilities.addNofollow("<a href=\"x\" rel=\"nofollow\">l</a>"));
    }

    @Test
    public void addNofollowOnlyMatchesAnchorsStartingWithHref() {
        String html = "<a class=\"c\" href=\"x\">l</a>";
        assertEquals(html, Utilities.addNofollow(html));
        assertEquals("no links here", Utilities.addNofollow("no links here"));
    }

    @Test
    public void addNofollowHandlesMultipleLinks() {
        assertEquals("<a href=\"1\">a</a> <a href=\"2\" rel=\"nofollow\" rel=\"nofollow\">b</a> <a href=\"3\">c</a>",
                Utilities.addNofollow("<a href=\"1\">a</a> <a href=\"2\" rel=\"nofollow\">b</a> <a href=\"3\">c</a>"));
    }

    // ---- extractHTML ------------------------------------------------------

    @Test
    public void extractHtmlKeepsOnlyTags() {
        assertEquals("<b></b>", Utilities.extractHTML("a<b>c</b>d"));
        assertEquals("<a href=\"x\"></a>", Utilities.extractHTML("<a href=\"x\">link</a>"));
    }

    @Test
    public void extractHtmlNoTagsReturnsInputUnchanged() {
        assertEquals("abc", Utilities.extractHTML("abc"));
        assertEquals("", Utilities.extractHTML(""));
    }

    @Test
    public void extractHtmlNullReturnsEmptyString() {
        assertEquals("", Utilities.extractHTML(null));
    }

    @Test
    public void extractHtmlUnclosedTagDropped() {
        assertEquals("", Utilities.extractHTML("a<b"));
        assertEquals("<b>", Utilities.extractHTML("a<b>c<d"));
    }

    // ---- truncate ---------------------------------------------------------

    @Test
    public void truncateCutsAtLastSpaceWithinUpper() {
        assertEquals("hello world...", Utilities.truncate("hello world foo bar", 5, 11, "..."));
    }

    @Test
    public void truncateCutsAtUpperWhenNoSpaceAfterLower() {
        assertEquals("abcde..", Utilities.truncate("abcdefghij", 2, 5, ".."));
    }

    @Test
    public void truncateRaisesUpperToLower() {
        assertEquals("abcdef!", Utilities.truncate("abcdefghij", 6, 3, "!"));
    }

    @Test
    public void truncateStripsHtmlEvenWhenNotTruncated() {
        assertEquals("hi", Utilities.truncate("<b>hi</b>", 1, 10, ".."));
        assertEquals("hello...", Utilities.truncate("<b>hello</b> world foo", 5, 8, "..."));
    }

    @Test
    public void truncateBoundaryLengthEqualToUpperIsNotTruncated() {
        assertEquals("abcde", Utilities.truncate("abcde", 2, 5, ".."));
        assertEquals("abcde..", Utilities.truncate("abcdef", 2, 5, ".."));
    }

    @Test
    public void truncateNullAndNullSuffix() {
        assertEquals("", Utilities.truncate(null, 1, 5, "..."));
        assertEquals("abcdenull", Utilities.truncate("abcdefghij", 2, 5, null));
    }

    // ---- truncateText -----------------------------------------------------

    @Test
    public void truncateTextReturnsOriginalWithHtmlWhenNotTruncated() {
        assertEquals("<b>hi</b>", Utilities.truncateText("<b>hi</b>", 1, 10, ".."));
    }

    @Test
    public void truncateTextReturnsStrippedTextWhenTruncated() {
        assertEquals("hello...", Utilities.truncateText("<b>hello</b> world foo", 5, 8, "..."));
        assertEquals("abcde..", Utilities.truncateText("abcdefghij", 2, 5, ".."));
        assertEquals("abcdef!", Utilities.truncateText("abcdefghij", 6, 3, "!"));
    }

    @Test
    public void truncateTextNullReturnsNullWhenNothingToTruncate() {
        assertNull(Utilities.truncateText(null, 0, 5, "..."));
    }

    // ---- truncateNicely ---------------------------------------------------

    @Test
    public void truncateNicelyPlainTextCutsAtWordOrUpper() {
        assertEquals("blah blah blah", Utilities.truncateNicely("blah blah blah blah blah", 11, 15, ""));
        assertEquals("abcde..", Utilities.truncateNicely("abcdefghij", 2, 5, ".."));
        assertEquals("abcdef!", Utilities.truncateNicely("abcdefghij", 6, 3, "!"));
    }

    @Test
    public void truncateNicelyReturnsOriginalWhenFits() {
        assertEquals("<b>hi</b>", Utilities.truncateNicely("<b>hi</b>", 1, 10, ".."));
        assertEquals("", Utilities.truncateNicely("", 0, 5, ".."));
    }

    @Test
    public void truncateNicelyReinsertsClosingTags() {
        assertEquals("<b>abcde..</b>", Utilities.truncateNicely("<b>abcdefghij</b>", 2, 5, ".."));
        assertEquals("<p><b>blah1 blah2</b> <i>blah3</i></p>",
                Utilities.truncateNicely("<p><b>blah1 blah2</b> <i>blah3 blah4 blah5</i></p>", 15, 20, ""));
    }

    @Test
    public void truncateNicelyNullThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Utilities.truncateNicely(null, 1, 5, ".."));
    }

    // ---- transformToHTMLSubset --------------------------------------------

    @Test
    public void transformSubsetRestoresAllowedTagsCaseInsensitively() {
        assertEquals("<b>x</b><i>y</i><blockquote>q</blockquote><p>p</p><pre>c</pre>",
                Utilities.transformToHTMLSubset(
                        "&lt;B&gt;x&lt;/b&gt;&lt;I&gt;y&lt;/I&gt;&lt;BLOCKQUOTE&gt;q&lt;/blockquote&gt;"
                        + "&lt;p&gt;p&lt;/P&gt;&lt;pre&gt;c&lt;/PRE&gt;"));
        assertEquals("<ul><li>a</li></ul><ol><li>b</li></ol>",
                Utilities.transformToHTMLSubset(
                        "&lt;ul&gt;&lt;li&gt;a&lt;/li&gt;&lt;/ul&gt;&lt;ol&gt;&lt;li&gt;b&lt;/li&gt;&lt;/ol&gt;"));
    }

    @Test
    public void transformSubsetNormalisesBr() {
        assertEquals("<br /><br /><br /><br />",
                Utilities.transformToHTMLSubset("&lt;br&gt;&lt;br/&gt;&lt;BR /&gt;&lt;br  /&gt;"));
    }

    @Test
    public void transformSubsetKeepsDisallowedTagsEscaped() {
        assertEquals("&lt;script&gt;x&lt;/script&gt; &lt;a&gt;",
                Utilities.transformToHTMLSubset("&lt;script&gt;x&lt;/script&gt; &lt;a&gt;"));
        assertEquals("&lt;p class=\"c\"&gt;",
                Utilities.transformToHTMLSubset("&lt;p class=&quot;c&quot;&gt;"));
    }

    @Test
    public void transformSubsetUnescapesQuoteEntity() {
        assertEquals("say \"hi\"", Utilities.transformToHTMLSubset("say &quot;hi&QUOT;"));
    }

    @Test
    public void transformSubsetRestoresDoubleEscapedEntities() {
        assertEquals("&lt; &gt; &#65;", Utilities.transformToHTMLSubset("&amp;lt; &amp;gt; &amp;#65;"));
    }

    @Test
    public void transformSubsetLinkVariants() {
        assertEquals("<a href=\"http://x\">y</a>",
                Utilities.transformToHTMLSubset("&lt;a href=&quot;http://x&quot;&gt;y&lt;/a&gt;"));
        assertEquals("<a>y</a>", Utilities.transformToHTMLSubset("&lt;a href=&gt;y&lt;/a&gt;"));
        assertEquals("<a>y</a>",
                Utilities.transformToHTMLSubset("&lt;a href=&quot;javascript:alert(1)&quot;&gt;y&lt;/a&gt;"));
        assertEquals("&lt;a href=\"http://x\"",
                Utilities.transformToHTMLSubset("&lt;a href=&quot;http://x&quot;"));
    }

    @Test
    public void transformSubsetNullAndEmpty() {
        assertNull(Utilities.transformToHTMLSubset(null));
        assertEquals("", Utilities.transformToHTMLSubset(""));
        assertEquals("plain", Utilities.transformToHTMLSubset("plain"));
    }
}
