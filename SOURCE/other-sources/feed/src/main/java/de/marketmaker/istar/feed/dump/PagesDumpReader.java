/*
 * PagesDumpReader.java
 *
 * Created on 13.12.13 08:32
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.dump;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.springframework.util.FileCopyUtils;

import de.marketmaker.istar.common.io.ByteBufferOutputStream;
import de.marketmaker.istar.feed.FeedRecord;
import de.marketmaker.istar.feed.builder.PageBuilder;
import de.marketmaker.istar.feed.mdps.MdpsPageParser;
import de.marketmaker.istar.feed.mdps.SimpleMdpsRecordSource;
import de.marketmaker.istar.feed.pages.PageData;
import de.marketmaker.istar.feed.pages.PageDbDao;

/**
 * Sample class that reads pages files as they are dumped by the MdpsFeedDumper
 * @author oflege
 */
public class PagesDumpReader {
    private static FileFilter FF = pathname -> pathname.getName().matches("xfeed_pages.*bin");

    private final MdpsPageParser pp = new MdpsPageParser();

    private final ByteBufferOutputStream bbos = new ByteBufferOutputStream(1 << 24);

    private int offset;

    private int length;

    public PagesDumpReader() throws Exception {
        pp.setFeedBuilders(setUpPageBuilder());
    }

    private PageBuilder setUpPageBuilder() throws Exception {
        PageBuilder pb = new PageBuilder();
        pb.setPageDao(new PageDbDao() {
            @Override
            public void store(PageData page) {
                System.out.println(page);
            }
        });
        pb.afterPropertiesSet();
        return pb;
    }

    private void parse(File f) throws Exception {
        final ByteBuffer bb = copyToBuffer(f);
        SimpleMdpsRecordSource rs = new SimpleMdpsRecordSource(bb);

        while (bb.hasRemaining()) {
            offset = bb.position();
            FeedRecord feedRecord = rs.getFeedRecord();
            length = bb.position() - offset;
            pp.parse(feedRecord);
        }
    }

    private ByteBuffer copyToBuffer(File f) throws IOException {
        if (FF.accept(f)) {
            bbos.reset();
            FileCopyUtils.copy(new InflaterInputStream(new FileInputStream(f),
                    new Inflater(true), 8 * 1024), bbos);
            return bbos.toBuffer();
        }
        return ByteBuffer.wrap(FileCopyUtils.copyToByteArray(f));
    }

    public static void main(String[] args) throws Exception {
        PagesDumpReader r = new PagesDumpReader();
        for (String arg: args) {
            File f = new File(arg);
            if (f.isFile()) {
                r.parse(f);
            }
            else if (f.isDirectory()) {
                File[] files = f.listFiles(FF);
                for (File file : files) {
                    r.parse(file);
                }
            }
        }
    }
}
