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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import jakarta.activation.FileTypeMap;
import jakarta.activation.MimetypesFileTypeMap;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.RegexUtil;
import org.apache.roller.util.RollerConstants;

/**
 * General purpose utilities, not for use in templates.
 */
public class Utilities {
    /** The <code>Log</code> instance for this class. */
    private static final Log mLogger = LogFactory.getLog(Utilities.class);

    public static final String TAG_SPLIT_CHARS = " ,\n\r\f\t";

    // ------------------------------------------------------------------------
    /** Strip jsessionid off of a URL */
    public static String stripJsessionId(String url) {
        // Strip off jsessionid found in referer URL
        int startPos = url.indexOf(";jsessionid=");
        if (startPos != -1) {
            int endPos = url.indexOf('?', startPos);
            if (endPos == -1) {
                url = url.substring(0, startPos);
            } else {
                url = url.substring(0, startPos)
                        + url.substring(endPos, url.length());
            }
        }
        return url;
    }

    // ------------------------------------------------------------------------
    /**
     * @deprecated Use {@link HtmlUtilities#escapeHTML(String)}
     */
    @Deprecated
    public static String escapeHTML(String s) {
        return HtmlUtilities.escapeHTML(s);
    }

    // ------------------------------------------------------------------------
    /**
     * @deprecated Use {@link HtmlUtilities#escapeHTML(String, boolean)}
     */
    @Deprecated
    public static String escapeHTML(String s, boolean escapeAmpersand) {
        return HtmlUtilities.escapeHTML(s, escapeAmpersand);
    }

    /**
     * @deprecated Use {@link HtmlUtilities#unescapeHTML(String)}
     */
    @Deprecated
    public static String unescapeHTML(String str) {
        return HtmlUtilities.unescapeHTML(str);
    }

    // ------------------------------------------------------------------------
    /**
     * @deprecated Use {@link HtmlUtilities#removeHTML(String)}
     */
    @Deprecated
    public static String removeHTML(String str) {
        return HtmlUtilities.removeHTML(str);
    }

    /**
     * @deprecated Use {@link HtmlUtilities#removeHTML(String, boolean)}
     */
    @Deprecated
    public static String removeHTML(String str, boolean addSpace) {
        return HtmlUtilities.removeHTML(str, addSpace);
    }

    // ------------------------------------------------------------------------
    /**
     * @deprecated Use {@link HtmlUtilities#removeAndEscapeHTML(String)}
     */
    @Deprecated
    public static String removeAndEscapeHTML(String s) {
        return HtmlUtilities.removeAndEscapeHTML(s);
    }

    // ------------------------------------------------------------------------
    /**
     * @deprecated Use {@link HtmlUtilities#autoformat(String)}
     */
    @Deprecated
    public static String autoformat(String s) {
        return HtmlUtilities.autoformat(s);
    }

    /**
     * @deprecated Use {@link HtmlUtilities#addNofollow(String)}
     */
    @Deprecated
    public static String addNofollow(String html) {
        return HtmlUtilities.addNofollow(html);
    }

    // ------------------------------------------------------------------------
    /**
     * Replaces occurences of non-alphanumeric characters with an underscore.
     */
    public static String replaceNonAlphanumeric(String str) {
        return replaceNonAlphanumeric(str, '_');
    }

    // ------------------------------------------------------------------------
    /**
     * Replaces occurrences of non-alphanumeric characters with a supplied char.
     */
    public static String replaceNonAlphanumeric(String str, char subst) {
        StringBuilder ret = new StringBuilder(str.length());
        char[] testChars = str.toCharArray();
        for (int i = 0; i < testChars.length; i++) {
            if (Character.isLetterOrDigit(testChars[i])) {
                ret.append(testChars[i]);
            } else {
                ret.append(subst);
            }
        }
        return ret.toString();
    }

    // ------------------------------------------------------------------------
    /**
     * Remove occurences of non-alphanumeric characters.
     */
    public static String removeNonAlphanumeric(String str) {
        StringBuilder ret = new StringBuilder(str.length());
        char[] testChars = str.toCharArray();
        for (int i = 0; i < testChars.length; i++) {
            // MR: Allow periods in page links
            if (Character.isLetterOrDigit(testChars[i]) || testChars[i] == '.') {
                ret.append(testChars[i]);
            }
        }
        return ret.toString();
    }

    // ------------------------------------------------------------------------
    /** Convert string array to string with delimeters. */
    public static String stringArrayToString(String[] stringArray, String delim) {
        StringBuilder bldr = new StringBuilder();
        for (int i = 0; i < stringArray.length; i++) {
            if (bldr.length() > 0) {
                bldr.append(delim).append(stringArray[i]);
            } else {
                bldr.append(stringArray[i]);
            }
        }
        return bldr.toString();
    }

    // ------------------------------------------------------------------------
    /** Convert string array to string with delimeters. */
    public static String stringListToString(List<String> stringList,
            String delim) {
        StringBuilder bldr = new StringBuilder();
        for (String s : stringList) {
            if (bldr.length() > 0) {
                bldr.append(delim).append(s);
            } else {
                bldr.append(s);
            }
        }
        return bldr.toString();
    }

    // --------------------------------------------------------------------------
    /** Convert string with delimeters to string array. */
    public static String[] stringToStringArray(String instr, String delim) {
        return StringUtils.split(instr, delim);
    }

    // --------------------------------------------------------------------------
    /** Convert string with delimiters to string list.
     */
    public static List<String> stringToStringList(String instr, String delim) {
        List<String> stringList = new ArrayList<>();
        String[] str = StringUtils.split(instr, delim);
        stringList.addAll(Arrays.asList(str));
        return stringList;
    }

    // --------------------------------------------------------------------------
    /** Convert string to integer array. */
    public static int[] stringToIntArray(String instr, String delim) {
        String[] str = StringUtils.split(instr, delim);
        int intArray[] = new int[str.length];
        int i = 0;
        for (String string : str) {
            int nInt = Integer.parseInt(string);
            intArray[i++] = nInt;
        }
        return intArray;
    }

    // -------------------------------------------------------------------
    /** Convert integer array to a string. */
    public static String intArrayToString(int[] intArray) {
        StringBuilder bldr = new StringBuilder();
        for (int s : intArray) {
            if (bldr.length() > 0) {
                bldr.append(",").append(Integer.toString(s));
            } else {
                bldr.append(Integer.toString(s));
            }
        }
        return bldr.toString();
    }

    // ------------------------------------------------------------------------
    public static void copyFile(File from, File to) throws IOException {
        InputStream in;
        OutputStream out;

        try {
            in = new FileInputStream(from);
        } catch (IOException ex) {
            throw new IOException("Utilities.copyFile: opening input stream '"
                    + from.getPath() + "', " + ex.getMessage());
        }

        try {
            out = new FileOutputStream(to);
        } catch (Exception ex) {
            try {
                in.close();
            } catch (IOException ex1) {
            }
            throw new IOException("Utilities.copyFile: opening output stream '"
                    + to.getPath() + "', " + ex.getMessage());
        }

        copyInputToOutput(in, out, from.length());
    }

    /**
     * Reads an inputstream into a string
     */
    public static String streamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = in.readLine()) != null) {
            sb.append(line);
            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------------
    /**
     * Utility method to copy an input stream to an output stream. Wraps both
     * streams in buffers. Ensures right numbers of bytes copied.
     */
    public static void copyInputToOutput(InputStream input,
            OutputStream output, long byteCount) throws IOException {
        int bytes;
        long length;

        BufferedInputStream in = new BufferedInputStream(input);
        BufferedOutputStream out = new BufferedOutputStream(output);

        byte[] buffer;
        buffer = new byte[RollerConstants.EIGHT_KB_IN_BYTES];

        for (length = byteCount; length > 0;) {
            bytes = (int) (length > RollerConstants.EIGHT_KB_IN_BYTES ? RollerConstants.EIGHT_KB_IN_BYTES : length);

            try {
                bytes = in.read(buffer, 0, bytes);
            } catch (IOException ex) {
                try {
                    in.close();
                    out.close();
                } catch (IOException ex1) {
                }
                throw new IOException("Reading input stream, "
                        + ex.getMessage());
            }

            if (bytes < 0) {
                break;
            }

            length -= bytes;

            try {
                out.write(buffer, 0, bytes);
            } catch (IOException ex) {
                try {
                    in.close();
                    out.close();
                } catch (IOException ex1) {
                }
                throw new IOException("Writing output stream, "
                        + ex.getMessage());
            }
        }

        try {
            in.close();
            out.close();
        } catch (IOException ex) {
            throw new IOException("Closing file streams, " + ex.getMessage());
        }
    }

    // ------------------------------------------------------------------------
    public static void copyInputToOutput(InputStream input, OutputStream output)
            throws IOException {
        BufferedInputStream in = new BufferedInputStream(input);
        BufferedOutputStream out = new BufferedOutputStream(output);
        byte buffer[] = new byte[RollerConstants.EIGHT_KB_IN_BYTES];
        for (int count = 0; count != -1;) {
            count = in.read(buffer, 0, RollerConstants.EIGHT_KB_IN_BYTES);
            if (count != -1) {
                out.write(buffer, 0, count);
            }
        }

        try {
            in.close();
            out.close();
        } catch (IOException ex) {
            throw new IOException("Closing file streams, " + ex.getMessage());
        }
    }

    /**
     * Encode a string using algorithm specified in web.xml and return the
     * resulting encrypted password. If exception, the plain credentials string
     * is returned
     * 
     * @param password
     *            Password or other credentials to use in authenticating this
     *            username
     * @param algorithm
     *            Algorithm used to do the digest
     * 
     * @return encypted password based on the algorithm.
     */
    public static String encodePassword(String password, String algorithm) {
        byte[] unencodedPassword = password.getBytes();

        MessageDigest md;

        try {
            // first create an instance, given the provider
            md = MessageDigest.getInstance(algorithm);
        } catch (Exception e) {
            mLogger.error("Exception: " + e);
            return password;
        }

        md.reset();

        // call the update method one or more times
        // (useful when you don't know the size of your data, eg. stream)
        md.update(unencodedPassword);

        // now calculate the hash
        byte[] encodedPassword = md.digest();

        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < encodedPassword.length; i++) {
            if ((encodedPassword[i] & 0xff) < 0x10) {
                buf.append("0");
            }

            buf.append(Long.toString(encodedPassword[i] & 0xff, 16));
        }

        return buf.toString();
    }

    /**
     * Encode a string using Base64 encoding. Used when storing passwords as
     * cookies.
     * 
     * This is weak encoding in that anyone can use the decodeString routine to
     * reverse the encoding.
     * 
     * @param str
     *            String to encode
     * @return String
     * @throws IOException
     */
    public static String encodeString(String str) throws IOException {
        String encodedStr = new String(Base64.encodeBase64(str.getBytes()));
        return (encodedStr.trim());
    }

    /**
     * Decode a string using Base64 encoding.
     * 
     * @param str
     *            String to decode
     * @return decoded string
     * @throws IOException
     */
    public static String decodeString(String str) throws IOException {
        String value = new String(Base64.decodeBase64(str.getBytes()));
        return (value);
    }

    /**
     * @deprecated Use {@link HtmlUtilities#truncate(String, int, int, String)}
     */
    @Deprecated
    public static String truncate(String str, int lower, int upper,
            String appendToEnd) {
        return HtmlUtilities.truncate(str, lower, upper, appendToEnd);
    }

    /**
     * @deprecated Use
     *             {@link HtmlUtilities#truncateNicely(String, int, int, String)}
     */
    @Deprecated
    public static String truncateNicely(String str, int lower, int upper,
            String appendToEnd) {
        return HtmlUtilities.truncateNicely(str, lower, upper, appendToEnd);
    }

    /**
     * @deprecated Use
     *             {@link HtmlUtilities#truncateText(String, int, int, String)}
     */
    @Deprecated
    public static String truncateText(String str, int lower, int upper,
            String appendToEnd) {
        return HtmlUtilities.truncateText(str, lower, upper, appendToEnd);
    }

    /**
     * @deprecated Use {@link HtmlUtilities#extractHTML(String)}
     */
    @Deprecated
    public static String extractHTML(String str) {
        return HtmlUtilities.extractHTML(str);
    }

    public static String hexEncode(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        return RegexUtil.encode(str);
    }

    public static String encodeEmail(String str) {
        return str != null ? RegexUtil.encodeEmail(str) : null;
    }

    /**
     * @param string
     * @return
     */
    public static int stringToInt(String string) {
        try {
            return Integer.valueOf(string);
        } catch (NumberFormatException e) {
            mLogger.debug("Invalid Integer:" + string);
        }
        return 0;
    }

    /**
     * Convert a byte array into a Base64 string (as used in mime formats)
     */
    public static String toBase64(byte[] aValue) {

        final String strBase64Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

        int byte1;
        int byte2;
        int byte3;
        int iByteLen = aValue.length;
        StringBuilder tt = new StringBuilder();

        for (int i = 0; i < iByteLen; i += 3) {
            boolean bByte2 = (i + 1) < iByteLen;
            boolean bByte3 = (i + 2) < iByteLen;
            byte1 = aValue[i] & 0xFF;
            byte2 = (bByte2) ? (aValue[i + 1] & 0xFF) : 0;
            byte3 = (bByte3) ? (aValue[i + 2] & 0xFF) : 0;

            tt.append(strBase64Chars.charAt(byte1 / 4));
            tt.append(strBase64Chars
                    .charAt((byte2 / 16) + ((byte1 & 0x3) * 16)));
            tt.append(((bByte2) ? strBase64Chars.charAt((byte3 / 64)
                    + ((byte2 & 0xF) * 4)) : '='));
            tt.append(((bByte3) ? strBase64Chars.charAt(byte3 & 0x3F) : '='));
        }

        return tt.toString();
    }

    /**
     * Removes characters that are not allowed in a tag name: whitespace,
     * control characters, comma, and the HTML-significant characters
     * <code>" &amp; ' &lt; &gt; `</code>. Templates can then print tag names
     * as they are.
     *
     * @param tag the tag name
     * @return the tag name without invalid characters
     */
    public static String stripInvalidTagCharacters(String tag) {
        if (tag == null) {
            throw new NullPointerException();
        }

        StringBuilder sb = new StringBuilder();
        char[] charArray = tag.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];

            switch (c) {
            case '"':
            case '&':
            case '\'':
            case ',':
            case '<':
            case '>':
            case '`':
                continue;
            default:
                break;
            }

            if ((33 <= c && c <= 126) || Character.isUnicodeIdentifierPart(c)
                    || Character.isUnicodeIdentifierStart(c)) {
                sb.append(charArray[i]);
            }
        }
        return sb.toString();
    }

    public static String normalizeTag(String tag, Locale locale) {
        tag = Utilities.stripInvalidTagCharacters(tag);
        return locale == null ? tag.toLowerCase() : tag.toLowerCase(locale);
    }

    /**
     * @param tags String holding space separated list of tags
     * @return List of strings, one string for each tag
     */
    public static List<String> splitStringAsTags(String tags) {
        String[] tagsarr = StringUtils.split(tags, TAG_SPLIT_CHARS);
        if (tagsarr == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(tagsarr);
    }

    /**
     * @deprecated Use {@link HtmlUtilities#transformToHTMLSubset(String)}
     */
    @Deprecated
    public static String transformToHTMLSubset(String s) {
        return HtmlUtilities.transformToHTMLSubset(s);
    }

    public static String getContentTypeFromFileName(String fileName) {

        FileTypeMap map = FileTypeMap.getDefaultFileTypeMap();

        // TODO: figure out why PNG is missing from Java MIME types
        if (map instanceof MimetypesFileTypeMap) {
            try {
                ((MimetypesFileTypeMap) map).addMimeTypes("image/png png PNG");
            } catch (Exception ignored) {
            }
        }

        return map.getContentType(fileName);
    }

    /**
     * Validate the form of an email address.
     * 
     * <P>
     * Return <tt>true</tt> only if
     * <ul>
     * <li> <tt>aEmailAddress</tt> can successfully construct an
     * {@link javax.mail.internet.InternetAddress}
     * <li>when parsed with "@" as delimiter, <tt>aEmailAddress</tt> contains
     * two tokens which satisfy
     * </ul>
     * <P>
     * The second condition arises since local email addresses, simply of the
     * form "<tt>albert</tt>", for example, are valid for
     * {@link javax.mail.internet.InternetAddress}, but almost always undesired.
     */
    public static boolean isValidEmailAddress(String aEmailAddress) {
        if (aEmailAddress == null) {
            return false;
        }
        boolean result = true;
        try {
            // See if its valid
            new InternetAddress(aEmailAddress);
            if (!hasNameAndDomain(aEmailAddress)) {
                result = false;
            }
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    /**
     * Checks for name and domain.
     * 
     * @param aEmailAddress
     *            the a email address
     * @return true, if successful
     */
    private static boolean hasNameAndDomain(String aEmailAddress) {
        String[] tokens = aEmailAddress.split("@");
        return tokens.length == 2 && StringUtils.isNotEmpty(tokens[0])
                && StringUtils.isNotEmpty(tokens[1]);
    }

}
