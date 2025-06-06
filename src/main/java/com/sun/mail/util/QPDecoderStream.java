/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.mail.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * This class implements a QP Decoder. It is implemented as
 * a FilterInputStream, so one can just wrap this class around
 * any input stream and read bytes from this filter. The decoding
 * is done as the bytes are read out.
 *
 * @author John Mani
 */

public class QPDecoderStream extends FilterInputStream {
    protected byte[] ba = new byte[2];
    protected int spaces = 0;

    /**
     * Create a Quoted Printable decoder that decodes the specified
     * input stream.
     *
     * @param in the input stream
     */
    public QPDecoderStream(InputStream in) {
        super(new PushbackInputStream(in, 2)); // pushback of size=2
    }

    /**
     * Read the next decoded byte from this input stream. The byte
     * is returned as an <code>int</code> in the range <code>0</code>
     * to <code>255</code>. If no byte is available because the end of
     * the stream has been reached, the value <code>-1</code> is returned.
     * This method blocks until input data is available, the end of the
     * stream is detected, or an exception is thrown.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     * stream is reached.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        if (spaces > 0) {
            // We have cached space characters, return one
            spaces--;
            return ' ';
        }

        int c = in.read();

        if (c == ' ') {
            // Got space, keep reading till we get a non-space char
            while ((c = in.read()) == ' ')
                spaces++;

            if (c == '\r' || c == '\n' || c == -1)
                // If the non-space char is CR/LF/EOF, the spaces we got
                // so far is junk introduced during transport. Junk 'em.
                spaces = 0;
            else {
                // The non-space char is NOT CR/LF, the spaces are valid.
                ((PushbackInputStream) in).unread(c);
                c = ' ';
            }
            return c; // return either <SPACE> or <CR/LF>
        } else if (c == '=') {
            // QP Encoded atom. Decode the next two bytes
            int a = in.read();

            if (a == '\n') {
                /* Hmm ... not really confirming QP encoding, but lets
                 * allow this as a LF terminated encoded line .. and
                 * consider this a soft linebreak and recurse to fetch
                 * the next char.
                 */
                return read();
            } else if (a == '\r') {
                // Expecting LF. This forms a soft linebreak to be ignored.
                int b = in.read();
                if (b != '\n')
                    /* Not really confirming QP encoding, but
                     * lets allow this as well.
                     */
                    ((PushbackInputStream) in).unread(b);
                return read();
            } else if (a == -1) {
                // Not valid QP encoding, but we be nice and tolerant here !
                return -1;
            } else {
                ba[0] = (byte) a;
                ba[1] = (byte) in.read();
                try {
                    return ASCIIUtility.parseInt(ba, 0, 2, 16);
                } catch (NumberFormatException nex) {
		    /*
		    System.err.println(
		     	"Illegal characters in QP encoded stream: " + 
		     	ASCIIUtility.toString(ba, 0, 2)
		    );
		    */

                    ((PushbackInputStream) in).unread(ba);
                    return c;
                }
            }
        }
        return c;
    }

    /**
     * Reads up to <code>len</code> decoded bytes of data from this input stream
     * into an array of bytes. This method blocks until some input is
     * available.
     * <p>
     *
     * @param buf the buffer into which the data is read.
     * @param off the start offset of the data.
     * @param len the maximum number of bytes read.
     * @return the total number of bytes read into the buffer, or
     * <code>-1</code> if there is no more data because the end of
     * the stream has been reached.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        int i, c;
        for (i = 0; i < len; i++) {
            if ((c = read()) == -1) {
                if (i == 0) // At end of stream, so we should
                    i = -1; // return -1 , NOT 0.
                break;
            }
            buf[off + i] = (byte) c;
        }
        return i;
    }

    /**
     * Skips over and discards n bytes of data from this stream.
     */
    @Override
    public long skip(long n) throws IOException {
        long skipped = 0;
        while (n-- > 0 && read() >= 0)
            skipped++;
        return skipped;
    }

    /**
     * Tests if this input stream supports marks. Currently this class
     * does not support marks
     */
    @Override
    public boolean markSupported() {
        return false;
    }

    /**
     * Returns the number of bytes that can be read from this input
     * stream without blocking. The QP algorithm does not permit
     * a priori knowledge of the number of bytes after decoding, so
     * this method just invokes the <code>available</code> method
     * of the original input stream.
     */
    @Override
    public int available() throws IOException {
        // This is bogus ! We don't really know how much
        // bytes are available *after* decoding
        return in.available();
    }

    /**** begin TEST program
     public static void main(String argv[]) throws Exception {
     FileInputStream infile = new FileInputStream(argv[0]);
     QPDecoderStream decoder = new QPDecoderStream(infile);
     int c;

     while ((c = decoder.read()) != -1)
     System.out.print((char)c);
     System.out.println();
     }
     *** end TEST program ****/
}
