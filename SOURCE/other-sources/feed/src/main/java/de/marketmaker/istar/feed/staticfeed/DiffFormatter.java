/*
 * DiffFormatterMdp.java
 *
 * Created on 19.10.12 13:15
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.staticfeed;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.AsciiBytes;
import de.marketmaker.istar.feed.mdps.MdpsTypeMappings;
import de.marketmaker.istar.feed.ordered.OrderedSnapRecord;

import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.ID_BIG_SOURCE_ID;

/**
 * Formats feed updates as
 * @author oflege
 */
public class DiffFormatter {
    static final char DELETE = 'D';

    static final char UPDATE = 'U';

    static final char RECAP = 'R';

    static final char EXPIRED = 'X';

    public interface LineHandler {
        void append(String line);
    }

    private static final char SEP = ';';

    private final StringBuilder sb = new StringBuilder(4096);

    private boolean forward;

    private LineHandler handler;

    private final AsciiBytes ascii = new AsciiBytes();

    public void setHandler(LineHandler handler) {
        this.handler = handler;
    }

    public void init(char type, ByteString vwdcode, int vwdKeyType, int sourceId) {
        this.forward = (type == DELETE);
        this.sb.setLength(0);
        this.sb.append(type).append(SEP);
        appendQuotedString(vwdcode.toString());
        sb.append(SEP).append(getMdpsTypeSuffix(vwdKeyType));
        if (sourceId >= 0) {
            sb.append(SEP).append(ID_BIG_SOURCE_ID).append('=').append(sourceId);
        }
    }

    private String getMdpsTypeSuffix(int vwdKeyType) {
        final String result = MdpsTypeMappings.getMdpsKeyTypeByVwdType(vwdKeyType);
        return (result != null) ? result : "OT";
    }

    public void intChanged(int fid, int value) {
        addFid(fid);
        // TODO: Can MDP handle uint32 values? If yes, the single line below needs to be changed to:
        // =======
        // if (VwdFieldDescription.getField(fid).type() == VwdFieldDescription.Type.UINT) {
        //    this.sb.append(Integer.toUnsignedLong(value));
        // } else {
        //    this.sb.append(value);
        // }
        // =======
        this.sb.append(value);
    }

    public void priceChanged(int fid, long value) {
        addFid(fid);
        this.ascii.setPrice((int) value, (int) (value >> 32));
        this.ascii.appendTo(this.sb);
    }

    public void stringChanged(int fid, byte[] value) {
        stringChanged(fid, OrderedSnapRecord.toString(value));
    }

    public void stringChanged(int fid, String value) {
        addFid(fid);
        appendQuotedString(value);
    }

    private void appendQuotedString(String s) {
        this.sb.append('"');
        int from = 0;
        int p = s.indexOf('"');
        while (p >= 0) {
            this.sb.append(s, from, p).append("\"\"");
            from = p + 1;
            p = s.indexOf('"', p + 1);
        }
        this.sb.append(s, from, s.length());
        this.sb.append('"');
    }

    public void fieldDeleted(int fid) {
        addFid(fid);
    }

    private void addFid(int fid) {
        this.sb.append(SEP).append(fid).append((fid == ID_BIG_SOURCE_ID) ? "H=" : "=");
        this.forward = true;
    }

    public void finish(boolean isDiff) {
        if (this.forward || isDiff) {
            this.handler.append(this.sb.toString());
        }
    }
}
