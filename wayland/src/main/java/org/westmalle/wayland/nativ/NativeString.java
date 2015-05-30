package org.westmalle.wayland.nativ;

/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.util.Objects;


/**
 * Provides a temporary allocation of an immutable C string
 * (<code>const char*</code> or <code>const wchar_t*</code>) for use when
 * converting a Java String into a native memory function argument.
 *
 * @author Todd Fast, todd.fast@sun.com
 * @author twall@users.sf.net
 */
public class NativeString implements CharSequence, Comparable {

    static final String WIDE_STRING = "--WIDE-STRING--";

    private final Pointer pointer;
    private final String  encoding;

    /**
     * Create a native string (NUL-terminated array of <code>char</code>).<p>
     * Uses the encoding returned by {@link Native#getDefaultStringEncoding()}.
     */
    public NativeString(final String string) {
        this(string,
             Native.getDefaultStringEncoding());
    }

    /**
     * Create a native string as a NUL-terminated array of <code>wchar_t</code>
     * (if <code>wide</code> is true) or <code>char</code>.<p>
     * If not <code>wide</code>, the encoding is obtained from {@link
     * Native#getDefaultStringEncoding()}.
     *
     * @param string value to write to native memory
     * @param wide   whether to store the String as <code>wchar_t</code>
     */
    public NativeString(final String string,
                        final boolean wide) {
        this(string,
             wide ? WIDE_STRING : Native.getDefaultStringEncoding());
    }

    /**
     * Create a native string as a NUL-terminated array of
     * <code>wchar_t</code>.
     */
    public NativeString(final WString string) {
        this(string.toString(),
             WIDE_STRING);
    }

    /**
     * Create a native string (NUL-terminated array of <code>char</code>),
     * using the requested encoding.
     */
    public NativeString(final String string,
                        final String encoding) {
        if (string == null) {
            throw new NullPointerException("String must not be null");
        }
        // Allocate the memory to hold the string.  Note, we have to
        // make this 1 element longer in order to accommodate the terminating
        // NUL (which is generated in Pointer.setString()).
        this.encoding = encoding;
        if (Objects.equals(this.encoding,
                           WIDE_STRING)) {
            final int len = (string.length() + 1) * Native.WCHAR_SIZE;
            this.pointer = new Memory(len);
            this.pointer.setWideString(0,
                                       string);
        }
        else {
            final byte[] data = getBytes(string,
                                         encoding);
            this.pointer = new Memory(data.length + 1);
            this.pointer.write(0,
                               data,
                               0,
                               data.length);
            this.pointer.setByte(data.length,
                                 (byte) 0);
        }
    }

    /**
     * Return a byte array corresponding to the given String, using the given
     * encoding.  If the encoding is not found default to the platform native
     * encoding.
     */
    private byte[] getBytes(final String s,
                            final String encoding) {
        if (encoding != null) {
            try {
                return s.getBytes(encoding);
            }
            catch (final UnsupportedEncodingException e) {
                System.err.println("JNA Warning: Encoding '"
                                   + encoding + "' is unsupported");
            }
        }
        System.err.println("JNA Warning: Encoding with fallback "
                           + System.getProperty("file.encoding"));
        return s.getBytes();
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public boolean equals(final Object other) {
        return other instanceof CharSequence && compareTo(other) == 0;
    }

    @Nonnull
    public String toString() {
        final boolean wide = Objects.equals(this.encoding,
                                            WIDE_STRING);
        return wide ? this.pointer.getWideString(0) : this.pointer.getString(0,
                                                                             this.encoding);
    }

    public Pointer getPointer() {
        return this.pointer;
    }

    public char charAt(final int index) {
        return toString().charAt(index);
    }

    public int length() {
        return toString().length();
    }

    public CharSequence subSequence(final int start,
                                    final int end) {
        return CharBuffer.wrap(toString())
                         .subSequence(start,
                                      end);
    }

    public int compareTo(@Nonnull final Object other) {

        return toString().compareTo(other.toString());
    }
}
