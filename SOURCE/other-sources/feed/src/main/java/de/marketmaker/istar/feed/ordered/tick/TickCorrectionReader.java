/*
 * TickCorrectionReader.java
 *
 * Created on 21.07.14 13:11
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.mcast.MulticastSender;
import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.DateTimeProvider;
import de.marketmaker.istar.feed.connect.BufferWriter;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.FieldDataBuilder;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFeedConstants;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.feed.FeedUpdateFlags.FLAG_TICK_CORRECTION_DELETE;
import static de.marketmaker.istar.feed.FeedUpdateFlags.FLAG_TICK_CORRECTION_INSERT;

/**
 * @author oflege
 */
@ManagedResource
public class TickCorrectionReader {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private BufferWriter writer;

    public void setWriter(BufferWriter writer) {
        this.writer = writer;
    }

    @ManagedOperation(description = "read corrections from file, add them to feed")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "filename", description = "file with records"),
    })
    public synchronized String read(String filename) {
        File f = new File(filename);
        if (!f.canRead()) {
            return "no such file " + f.getAbsolutePath();
        }

        TickCli.LineParser parser = new TickCli.LineParser();
        ByteBuffer bb = BufferFieldData.asBuffer(new byte[MulticastSender.MULTICAST_PACKET_SIZE - 16]);
        ByteBuffer blob = BufferFieldData.asBuffer(new byte[MulticastSender.MULTICAST_PACKET_SIZE - 16]);

        int lineNo = 0;
        try {
            List<String> lines = Files.lines(f.toPath(), StandardCharsets.UTF_8)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith("#")) {
                    send(bb, blob);
                    bb.position(2);
                    String vkeyStr = line.substring(1).trim();
                    if (!VendorkeyVwd.KEY_PATTERN.matcher(vkeyStr).matches()) {
                        continue;
                    }
                    VendorkeyVwd vkey = VendorkeyVwd.getInstance("1." + vkeyStr);
                    if (vkey == VendorkeyVwd.ERROR) {
                        throw new IllegalArgumentException("Illegal vendorkey '" + vkeyStr + "'");
                    }

                    final ByteString vwdcode = vkey.toVwdcode();
                    bb.put((byte) vwdcode.hashCode());
                    vwdcode.writeTo(bb, ByteString.LENGTH_ENCODING_BYTE);

                    bb.put((byte) 0);
                    bb.put(VwdFeedConstants.MESSAGE_TYPE_CORRECTION);
                    bb.putShort((short) vkey.getType());
                    bb.putShort((short) 0); // flags
                    bb.putInt(DateTimeProvider.Timestamp.encodeTimestamp(System.currentTimeMillis()));
                    blob.clear();
                }
                else {
                    parser.parse(line);
                    if (parser.matchCount > Byte.MAX_VALUE || parser.matchCount < Byte.MIN_VALUE) {
                        throw new IllegalStateException(parser.matchCount
                                + " not in [-128..127] for '" + line + "'");
                    }
                    if ((parser.flags & FLAG_TICK_CORRECTION_INSERT) != 0) {
                        throw new IllegalStateException("corrections cannot start with insert '" + line + "'");
                    }
                    int numInserts = 0;
                    while (i + 1 + numInserts < lines.size() && lines.get(i + 1 + numInserts).startsWith("+")) {
                        numInserts++;
                    }
                    if (numInserts == 0 && (parser.flags & FLAG_TICK_CORRECTION_DELETE) == 0) {
                        this.logger.warn("<read> void correction: no delete and no insert(s) '" + line + "'");
                        continue;
                    }
                    blob.put((byte) parser.matchCount);
                    addParsedLine(parser, blob);
                    blob.put((byte) numInserts);
                    for (int j = 0; j < numInserts; j++) {
                        i++;
                        parser.parse(lines.get(i));
                        addParsedLine(parser, blob);
                    }
                }
            }
            send(bb, blob);

        } catch (Exception e) {
            this.logger.warn("<read> failed in " + f.getAbsolutePath() + ":" + lineNo, e);
            return e.toString();
        }
        return "ok";
    }

    private void send(ByteBuffer bb, ByteBuffer blob) throws IOException {
        if (blob.position() == 0) {
            blob.clear();
            bb.clear();
            return;
        }

        FieldDataBuilder builder = new FieldDataBuilder(bb);
        builder.putStringFid(VwdFieldOrder.getOrder(VwdFieldDescription.ADF_Blob_Content.id()));
        builder.putString(Arrays.copyOfRange(blob.array(), 0, blob.position()));
        blob.clear();

        bb.flip();
        bb.putShort(0, (short) bb.remaining());
        this.writer.write(bb);
        bb.clear();
    }

    private void addParsedLine(TickCli.LineParser parser, ByteBuffer blob) {
        byte[] fields = parser.builder.asArray();
        blob.put((byte) (1 + fields.length));
        blob.put((byte) parser.flags);
        blob.put(fields);
    }

}
