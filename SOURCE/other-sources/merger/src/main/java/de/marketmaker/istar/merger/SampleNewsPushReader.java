/*
 * BarNews.java
 *
 * Created on 23.03.2007 17:38:25
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SampleNewsPushReader {

    public static void main(String[] args) throws Exception {
        final Socket socket = new Socket("istartest.market-maker.de", 19999);
        final InputStream is = new BufferedInputStream(socket.getInputStream());

        final SAXBuilder builder = new SAXBuilder();

        // read 10 news
        for (int i = 0; i < 10; i++) {
            final ByteBuffer bbNumBytes = ByteBuffer.wrap(readBytes(is, 4));
            final int newsLength = bbNumBytes.getInt();

            final byte[] text = readBytes(is, newsLength);
            final String s = new String(text, "UTF-8");
            final Document document = builder.build(new StringReader(s));
            final Element root = document.getRootElement();
            final String headline = root.getChild("element").getChildTextTrim("headline");

            System.out.println("headline: " + headline);
        }

        socket.close();
    }

    private static byte[] readBytes(InputStream is, int numBytes) throws IOException {
        final byte[] bytes = new byte[numBytes];
        int offset = 0;
        int numRead;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }
        return bytes;
    }
}
