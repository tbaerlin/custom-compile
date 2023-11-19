/*
 * IqsMessageDumper.java
 *
 * Created on 18.10.13 09:48
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.iqs;

import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Arrays;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.HexDump;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

import static de.marketmaker.istar.mdps.iqs.Constants.*;
import static de.marketmaker.istar.mdps.iqs.Constants.DATA_REQUEST_TYPE_RECAP_AND_UPDATES;
import static de.marketmaker.istar.mdps.iqs.Constants.FID_OBJECTNAME;

/**
 * Helper to format a message in human readable form
 * @author oflege
 */
public class IqsMessageDumper implements StreamingMessageParser.Handler {

    private final PrintWriter pw;

    private byte[] msg;

    private boolean inHeader;

    public IqsMessageDumper(PrintWriter pw) {
        this.pw = pw;
    }

    @Override
    public void fail() {
        pw.flush();
        throw new RuntimeException();
    }

    @Override
    public void start(byte[] msg) {
        this.msg = msg;
        this.inHeader = true;
        HexDump.writeHex(this.pw, msg);
    }

    @Override
    public void startBody() {
        pw.println("<B");
        this.inHeader = false;
    }

    @Override
    public void startEnvelope() {
        pw.println("<E");
        this.inHeader = true;
    }

    @Override
    public void end() {
        pw.println("--");
        pw.flush();
    }

    @Override
    public void field(int fid, int start, int end) {
        final String value = new String(msg, start, end - start, SnapRecord.CP_1252);
        if (this.inHeader) {
            String name = Constants.getFieldname(fid);
            String valueName = getValueName(fid, value);

            if (valueName != null) {
                pw.printf("[H] %-24s %4d: %s [%s]%n", name, fid, value, valueName);
            }
            else {
                pw.printf("[H] %-24s %4d: %s%n", name, fid, value);
            }
        }
        else {
            final VwdFieldDescription.Field f = VwdFieldDescription.getField(fid);
            String name = (f != null) ? f.name() : "";
            pw.printf("    %-24s %4d: %s%n", name, fid, value);
        }
    }

    private String getValueName(int fid, String value) {
        switch (fid) {
            case FID_MESSAGE_TYPE:
                return Constants.getRequestType(Integer.parseInt(value));
            case FID_DATA_STATUS:
                return Constants.getDataStatus(value);
            default:
                return null;
        }
    }

    public static void main(String[] args) {
        final IqsMessageDumper d = new IqsMessageDumper(new PrintWriter(System.out));

        final ByteBuffer bb = new IqsMessageBuilder(128).prepare(MSG_DATA_RESPONSE)
                .header(FID_SERVICEID, SERVICE_ID_PRICE)
                .header(FID_DATA_RESPONSE_TYPE, DATA_RESPONSE_TYPE_UPDATE)
                .header(FID_DATA_RESPONSE_FLAG, DATA_RESPONSE_FLAG_RECAP_COMPLETE) // ???
                .header(FID_DATA_TYPE, DATA_TYPE_FIELD)
                .header(FID_DATA_REQUEST_TYPE, DATA_REQUEST_TYPE_RECAP_AND_UPDATES)
                .header(FID_OBJECTNAME, new ByteString("710000.ETR"))
                .body()
                .openEnvelope()
                .header(FID_PERMISSION, "19Z")
                .body()
                .header(82, "13.456")
                .header(281, "01.01.2010")
                .closeEnvelope()
                .build();
        final byte[] msg = Arrays.copyOf(bb.array(), bb.limit());


        new StreamingMessageParser(d).parse(msg);
    }
}
