/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

/**
 * HTML and text-truncation helpers.
 */
public class HtmlUtilities {

    private static final Pattern mLinkPattern = Pattern.compile("<a href=.*?>",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_B_TAG_PATTERN = Pattern.compile(
            "&lt;b&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_B_TAG_PATTERN = Pattern.compile(
            "&lt;/b&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_I_TAG_PATTERN = Pattern.compile(
            "&lt;i&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_I_TAG_PATTERN = Pattern.compile(
            "&lt;/i&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_BLOCKQUOTE_TAG_PATTERN = Pattern
            .compile("&lt;blockquote&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_BLOCKQUOTE_TAG_PATTERN = Pattern
            .compile("&lt;/blockquote&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern BR_TAG_PATTERN = Pattern.compile(
            "&lt;br */*&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_P_TAG_PATTERN = Pattern.compile(
            "&lt;p&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_P_TAG_PATTERN = Pattern.compile(
            "&lt;/p&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_PRE_TAG_PATTERN = Pattern.compile(
            "&lt;pre&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_PRE_TAG_PATTERN = Pattern.compile(
            "&lt;/pre&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_UL_TAG_PATTERN = Pattern.compile(
            "&lt;ul&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_UL_TAG_PATTERN = Pattern.compile(
            "&lt;/ul&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_OL_TAG_PATTERN = Pattern.compile(
            "&lt;ol&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_OL_TAG_PATTERN = Pattern.compile(
            "&lt;/ol&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_LI_TAG_PATTERN = Pattern.compile(
            "&lt;li&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_LI_TAG_PATTERN = Pattern.compile(
            "&lt;/li&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_A_TAG_PATTERN = Pattern.compile(
            "&lt;/a&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_A_TAG_PATTERN = Pattern.compile(
            "&lt;a\\s+href\\s*=.*?&gt;", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern A_HREF_PATTERN = Pattern.compile(
            "&lt;a\\s+href\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s\"'=<>`]+))\\s*&gt;",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern SUBSET_LINK_PATTERN = Pattern.compile(
            "(?:https?://|mailto:)[^\\x00-\\x20\\x7f]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern QUOTE_PATTERN = Pattern.compile("&quot;",
            Pattern.CASE_INSENSITIVE);

    // ------------------------------------------------------------------------
    /**
     * Escape, but do not replace HTML. The default behaviour is to escape
     * ampersands.
     */
    public static String escapeHTML(String s) {
        return escapeHTML(s, true);
    }

    // ------------------------------------------------------------------------
    /**
     * Escape, but do not replace HTML.
     *
     * @param escapeAmpersand
     *            Optionally escape ampersands (&amp;).
     */
    public static String escapeHTML(String s, boolean escapeAmpersand) {
        // got to do amp's first so we don't double escape
        if (escapeAmpersand) {
            s = StringUtils.replace(s, "&", "&amp;");
        }
        s = StringUtils.replace(s, "&nbsp;", " ");
        s = StringUtils.replace(s, "\"", "&quot;");
        s = StringUtils.replace(s, "<", "&lt;");
        s = StringUtils.replace(s, ">", "&gt;");
        return s;
    }

    public static String unescapeHTML(String str) {
        return StringEscapeUtils.unescapeHtml4(str);
    }

    // ------------------------------------------------------------------------
    /**
     * Remove occurrences of html, defined as any text between the characters
     * "&lt;" and "&gt;". Replace any HTML tags with a space.
     */
    public static String removeHTML(String str) {
        return removeHTML(str, true);
    }

    /**
     * Remove occurrences of html, defined as any text between the characters
     * "&lt;" and "&gt;". Optionally replace HTML tags with a space.
     *
     * @param str
     * @param addSpace
     * @return
     */
    public static String removeHTML(String str, boolean addSpace) {
        if (str == null) {
            return "";
        }
        StringBuilder ret = new StringBuilder(str.length());
        int start = 0;
        int beginTag = str.indexOf('<');
        int endTag = 0;
        if (beginTag == -1) {
            return str;
        }

        while (beginTag >= start) {
            if (beginTag > 0) {
                ret.append(str.substring(start, beginTag));

                // replace each tag with a space (looks better)
                if (addSpace) {
                    ret.append(" ");
                }
            }
            endTag = str.indexOf('>', beginTag);

            // if endTag found move "cursor" forward
            if (endTag > -1) {
                start = endTag + 1;
                beginTag = str.indexOf('<', start);
            }
            // if no endTag found, get rest of str and break
            else {
                ret.append(str.substring(beginTag));
                break;
            }
        }
        // append everything after the last endTag
        if (endTag > -1 && endTag + 1 < str.length()) {
            ret.append(str.substring(endTag + 1));
        }
        return ret.toString().trim();
    }

    // ------------------------------------------------------------------------
    /**
     * Run both removeHTML and escapeHTML on a string.
     *
     * @param s
     *            String to be run through removeHTML and escapeHTML.
     * @return String with HTML removed and HTML special characters escaped.
     */
    public static String removeAndEscapeHTML(String s) {
        if (s == null) {
            return "";
        } else {
            return escapeHTML(removeHTML(s));
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Autoformat.
     */
    public static String autoformat(String s) {
        return StringUtils.replace(s, "\n", "<br />");
    }

    /**
     * Code (stolen from Pebble) to add rel="nofollow" string to all links in
     * HTML.
     */
    public static String addNofollow(String html) {
        if (html == null || html.length() == 0) {
            return html;
        }
        Matcher m = mLinkPattern.matcher(html);
        StringBuilder buf = new StringBuilder();
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            String link = html.substring(start, end);
            buf.append(html.substring(0, start));
            if (link.contains("rel=\"nofollow\"")) {
                buf.append(link.substring(0, link.length() - 1)
                        + " rel=\"nofollow\">");
            } else {
                buf.append(link);
            }
            html = html.substring(end, html.length());
            m = mLinkPattern.matcher(html);
        }
        buf.append(html);
        return buf.toString();
    }

    /**
     * Strips HTML and truncates.
     */
    public static String truncate(String str, int lower, int upper,
            String appendToEnd) {
        // strip markup from the string
        String str2 = removeHTML(str, false);

        // quickly adjust the upper if it is set lower than 'lower'
        if (upper < lower) {
            upper = lower;
        }

        // now determine if the string fits within the upper limit
        // if it does, go straight to return, do not pass 'go' and collect $200
        if (str2.length() > upper) {
            // the magic location int
            int loc;

            // first we determine where the next space appears after lower
            loc = str2.lastIndexOf(' ', upper);

            // now we'll see if the location is greater than the lower limit
            if (loc >= lower) {
                // yes it was, so we'll cut it off here
                str2 = str2.substring(0, loc);
            } else {
                // no it wasnt, so we'll cut it off at the upper limit
                str2 = str2.substring(0, upper);
            }

            // the string was truncated, so we append the appendToEnd String
            str2 = str2 + appendToEnd;
        }

        return str2;
    }

    /**
     * This method based on code from the String taglib at Apache Jakarta:
     * http:/
     * /cvs.apache.org/viewcvs/jakarta-taglibs/string/src/org/apache/taglibs
     * /string/util/StringW.java?rev=1.16&content-type=text/vnd.viewcvs-markup
     * Copyright (c) 1999 The Apache Software Foundation. Author:
     * timster@mac.com
     *
     * @param str
     * @param lower
     * @param upper
     * @param appendToEnd
     * @return
     */
    public static String truncateNicely(String str, int lower, int upper,
            String appendToEnd) {
        // strip markup from the string
        String str2 = removeHTML(str, false);
        boolean diff = (str2.length() < str.length());

        // quickly adjust the upper if it is set lower than 'lower'
        if (upper < lower) {
            upper = lower;
        }

        // now determine if the string fits within the upper limit
        // if it does, go straight to return, do not pass 'go' and collect $200
        if (str2.length() > upper) {
            // the magic location int
            int loc;

            // first we determine where the next space appears after lower
            loc = str2.lastIndexOf(' ', upper);

            // now we'll see if the location is greater than the lower limit
            if (loc >= lower) {
                // yes it was, so we'll cut it off here
                str2 = str2.substring(0, loc);
            } else {
                // no it wasnt, so we'll cut it off at the upper limit
                str2 = str2.substring(0, upper);
                loc = upper;
            }

            // HTML was removed from original str
            if (diff) {

                // location of last space in truncated string
                loc = str2.lastIndexOf(' ', loc);

                // get last "word" in truncated string (add 1 to loc to
                // eliminate space
                String str3 = str2.substring(loc + 1);

                // find this fragment in original str, from 'loc' position
                loc = str.indexOf(str3, loc) + str3.length();

                // get truncated string from original str, given new 'loc'
                str2 = str.substring(0, loc);

                // get all the HTML from original str after loc
                str3 = extractHTML(str.substring(loc));

                // remove any tags which generate visible HTML
                // This call is unecessary, all HTML has already been stripped
                // str3 = removeVisibleHTMLTags(str3);

                // append the appendToEnd String and
                // add extracted HTML back onto truncated string
                str = str2 + appendToEnd + str3;
            } else {
                // the string was truncated, so we append the appendToEnd String
                str = str2 + appendToEnd;
            }

        }

        return str;
    }

    public static String truncateText(String str, int lower, int upper,
            String appendToEnd) {
        // strip markup from the string
        String str2 = removeHTML(str, false);

        // quickly adjust the upper if it is set lower than 'lower'
        if (upper < lower) {
            upper = lower;
        }

        // now determine if the string fits within the upper limit
        // if it does, go straight to return, do not pass 'go' and collect $200
        if (str2.length() > upper) {
            // the magic location int
            int loc;

            // first we determine where the next space appears after lower
            loc = str2.lastIndexOf(' ', upper);

            // now we'll see if the location is greater than the lower limit
            if (loc >= lower) {
                // yes it was, so we'll cut it off here
                str2 = str2.substring(0, loc);
            } else {
                // no it wasnt, so we'll cut it off at the upper limit
                str2 = str2.substring(0, upper);
            }
            // the string was truncated, so we append the appendToEnd String
            str = str2 + appendToEnd;
        }
        return str;
    }

    /**
     * @param str
     * @return
     */
    private static String stripLineBreaks(String str) {
        // TODO: use a string buffer, ignore case !
        str = str.replace("<br>", "");
        str = str.replace("<br/>", "");
        str = str.replace("<br />", "");
        str = str.replace("<p></p>", "");
        str = str.replace("<p/>", "");
        str = str.replace("<p />", "");
        return str;
    }

    /**
     * Need need to get rid of any user-visible HTML tags once all text has been
     * removed such as &lt;BR&gt;. This sounds like a better approach than
     * removing all HTML tags and taking the chance to leave some tags
     * un-closed.
     *
     * WARNING: this method has serious performance problems a
     *
     * @author Alexis Moussine-Pouchkine
     *         (alexis.moussine-pouchkine@france.sun.com)
     * @author Lance Lavandowska
     * @param str
     *            the String object to modify
     * @return the new String object without the HTML "visible" tags
     */
    private static String removeVisibleHTMLTags(String str) {
        str = stripLineBreaks(str);
        StringBuilder result = new StringBuilder(str);
        StringBuilder lcresult = new StringBuilder(str.toLowerCase());

        // <img should take care of smileys, others to add?
        String[] visibleTags = { "<img" };
        int stringIndex;
        for (int j = 0; j < visibleTags.length; j++) {
            while ((stringIndex = lcresult.indexOf(visibleTags[j])) != -1) {
                if (visibleTags[j].endsWith(">")) {
                    result.delete(stringIndex,
                            stringIndex + visibleTags[j].length());
                    lcresult.delete(stringIndex,
                            stringIndex + visibleTags[j].length());
                } else {
                    // need to delete everything up until next closing '>', for
                    // <img for instance
                    int endIndex = result.indexOf(">", stringIndex);
                    if (endIndex > -1) {
                        // only delete it if we find the end! If we don't the
                        // HTML may be messed up, but we
                        // can't safely delete anything.
                        result.delete(stringIndex, endIndex + 1);
                        lcresult.delete(stringIndex, endIndex + 1);
                    }
                }
            }
        }

        // TODO: This code is buggy by nature. It doesn't deal with nesting of
        // tags properly.
        // remove certain elements with open & close tags, more available?
        String[] openCloseTags = { "li", "a", "div", "h1", "h2", "h3", "h4" };
        for (int j = 0; j < openCloseTags.length; j++) {
            // could this be better done with a regular expression?
            String closeTag = "</" + openCloseTags[j] + ">";
            int lastStringIndex = 0;
            while ((stringIndex = lcresult.indexOf("<" + openCloseTags[j],
                    lastStringIndex)) > -1) {
                lastStringIndex = stringIndex;
                // Try to find the matching closing tag (ignores possible
                // nesting!)
                int endIndex = lcresult.indexOf(closeTag, stringIndex);
                if (endIndex > -1) {
                    // If we found it delete it.
                    result.delete(stringIndex, endIndex + closeTag.length());
                    lcresult.delete(stringIndex, endIndex + closeTag.length());
                } else {
                    // Try to see if it is a self-closed empty content tag, i.e.
                    // closed with />.
                    endIndex = lcresult.indexOf(">", stringIndex);
                    int nextStart = lcresult.indexOf("<", stringIndex + 1);
                    if (endIndex > stringIndex
                            && lcresult.charAt(endIndex - 1) == '/'
                            && (endIndex < nextStart || nextStart == -1)) {
                        // Looks like it, so remove it.
                        result.delete(stringIndex, endIndex + 1);
                        lcresult.delete(stringIndex, endIndex + 1);
                    }
                }
            }
        }

        return result.toString();
    }

    /**
     * Extract (keep) JUST the HTML from the String.
     *
     * @param str
     * @return
     */
    public static String extractHTML(String str) {
        if (str == null) {
            return "";
        }
        StringBuilder ret = new StringBuilder(str.length());
        int start = 0;
        int beginTag = str.indexOf('<');
        int endTag = 0;
        if (beginTag == -1) {
            return str;
        }

        while (beginTag >= start) {
            endTag = str.indexOf('>', beginTag);

            // if endTag found, keep tag
            if (endTag > -1) {
                ret.append(str.substring(beginTag, endTag + 1));

                // move start forward and find another tag
                start = endTag + 1;
                beginTag = str.indexOf('<', start);
            }
            // if no endTag found, break
            else {
                break;
            }
        }
        return ret.toString();
    }

    /**
     * Transforms the given String into a subset of HTML displayable on a web
     * page. The subset includes &lt;b&gt;, &lt;i&gt;, &lt;p&gt;, &lt;br&gt;,
     * &lt;pre&gt; and &lt;a href&gt; (and their corresponding end tags).
     *
     * @param s
     *            the String to transform
     * @return the transformed String
     */
    public static String transformToHTMLSubset(String s) {

        if (s == null) {
            return null;
        }

        s = replace(s, OPENING_B_TAG_PATTERN, "<b>");
        s = replace(s, CLOSING_B_TAG_PATTERN, "</b>");
        s = replace(s, OPENING_I_TAG_PATTERN, "<i>");
        s = replace(s, CLOSING_I_TAG_PATTERN, "</i>");
        s = replace(s, OPENING_BLOCKQUOTE_TAG_PATTERN, "<blockquote>");
        s = replace(s, CLOSING_BLOCKQUOTE_TAG_PATTERN, "</blockquote>");
        s = replace(s, BR_TAG_PATTERN, "<br />");
        s = replace(s, OPENING_P_TAG_PATTERN, "<p>");
        s = replace(s, CLOSING_P_TAG_PATTERN, "</p>");
        s = replace(s, OPENING_PRE_TAG_PATTERN, "<pre>");
        s = replace(s, CLOSING_PRE_TAG_PATTERN, "</pre>");
        s = replace(s, OPENING_UL_TAG_PATTERN, "<ul>");
        s = replace(s, CLOSING_UL_TAG_PATTERN, "</ul>");
        s = replace(s, OPENING_OL_TAG_PATTERN, "<ol>");
        s = replace(s, CLOSING_OL_TAG_PATTERN, "</ol>");
        s = replace(s, OPENING_LI_TAG_PATTERN, "<li>");
        s = replace(s, CLOSING_LI_TAG_PATTERN, "</li>");
        s = replace(s, QUOTE_PATTERN, "\"");

        // Normalize supported links while retaining the surrounding text.
        s = replace(s, CLOSING_A_TAG_PATTERN, "</a>");
        Matcher m = OPENING_A_TAG_PATTERN.matcher(s);
        StringBuilder result = new StringBuilder(s.length());
        int end = 0;
        while (m.find()) {
            result.append(restoreSubsetEntities(s.substring(end, m.start())));
            Matcher hrefMatcher = A_HREF_PATTERN.matcher(m.group());
            String link = "<a>";
            if (hrefMatcher.matches()) {
                String href = hrefMatcher.group(1);
                if (href == null) {
                    href = hrefMatcher.group(2);
                }
                if (href == null) {
                    href = hrefMatcher.group(3);
                }
                href = StringEscapeUtils.unescapeHtml4(restoreSubsetEntities(href));
                if (SUBSET_LINK_PATTERN.matcher(href).matches()) {
                    link = "<a href=\"" + StringEscapeUtils.escapeHtml4(href) + "\">";
                }
            }
            result.append(link);
            end = m.end();
        }
        result.append(restoreSubsetEntities(s.substring(end)));
        return result.toString();
    }

    private static String restoreSubsetEntities(String s) {
        s = s.replace("&amp;lt;", "&lt;");
        s = s.replace("&amp;gt;", "&gt;");
        s = s.replace("&amp;#", "&#");
        return s;
    }

    private static String replace(String string, Pattern pattern,
            String replacement) {
        Matcher m = pattern.matcher(string);
        return m.replaceAll(replacement);
    }

}
