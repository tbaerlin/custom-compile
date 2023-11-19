/*
 * BackendUpdateLogger.java
 *
 * Created on 26.10.2005 09:28:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.mcast.MulticastReceiverImpl;
import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.TimeFormatter;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.feed.connect.OrderedMulticastReceiver;
import de.marketmaker.istar.ratios.BackendUpdateReceiver;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.backend.Constants;

import static de.marketmaker.istar.domain.instrument.InstrumentTypeEnum.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BackendUpdateLogger implements InitializingBean, DisposableBean, BackendUpdateReceiver {

    private final Map<InstrumentTypeEnum, int[]> fieldidsByType
            = new EnumMap<>(InstrumentTypeEnum.class);

    private final Map<InstrumentTypeEnum, int[]> fieldidIndexByType
            = new EnumMap<>(InstrumentTypeEnum.class);

    private final Map<InstrumentTypeEnum, Writer> writerByType
            = new EnumMap<>(InstrumentTypeEnum.class);

    private HashSet<Long> iids = new HashSet<>();

    public void setIids(Collection<Long> iids) {
        this.iids.clear();
        this.iids.addAll(iids);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (InstrumentTypeEnum type : new InstrumentTypeEnum[]{BND, CER, FND, STK, WNT, FUT, OPT, IND, CUR, ZNS}) {
            final int[] fieldids = getFields(type);

            this.fieldidsByType.put(type, fieldids);

            final int[] fieldidIndex = new int[RatioFieldDescription.getMaxFieldId() + 1];
            for (int i = 0; i < fieldids.length; i++) {
                fieldidIndex[fieldids[i]] = i;
            }
            this.fieldidIndexByType.put(type, fieldidIndex);

            this.writerByType.put(type, new PrintWriter(System.out));
        }

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (final Writer writer : writerByType.values()) {
                    try {
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 10 * 1000, 1000);
    }

    private int[] getFields(InstrumentTypeEnum type) {
        final List<RatioFieldDescription.Field> fields = new ArrayList<>();

        for (int i = 0; i < RatioFieldDescription.getMaxFieldId(); i++) {
            final RatioFieldDescription.Field f = RatioFieldDescription.getFieldById(i);
            if (f == null) {
                continue;
            }
            if (f.isApplicableFor(type)) {
                fields.add(f);
            }
        }

        fields.sort((o1, o2) -> o1.name().compareTo(o2.name()));

        final int[] fieldids = new int[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            fieldids[i] = fields.get(i).id();
        }

        return fieldids;
    }

    @Override
    public void destroy() throws Exception {
        for (final Writer writer : writerByType.values()) {
            writer.close();
        }
    }

    @Override
    public void update(byte[] bytes) {
        update(ByteBuffer.wrap(bytes));
    }

    public void update(ByteBuffer buffer) {
        try {
            final int blength = buffer.remaining();
            final InstrumentTypeEnum type = InstrumentTypeEnum.valueOf(buffer.getInt());
            final long instrumentid = buffer.getLong();
            if (!this.iids.isEmpty() && !this.iids.contains(instrumentid)) {
                return;
            }
            final long quoteid = buffer.getLong();

            final int[] fieldids = this.fieldidsByType.get(type);
            final int[] fieldidIndex = this.fieldidIndexByType.get(type);

            final String[] values = new String[fieldids.length + 4];

            values[0] = "type: " + type.name();
            values[1] = "iid: " + Long.toString(instrumentid);
            values[2] = "qid: " + Long.toString(quoteid);
            values[3] = "message length: " + Integer.toString(blength);

            while (buffer.hasRemaining()) {
                final int fieldid = buffer.getShort();
                final RatioFieldDescription.Field field = RatioFieldDescription.getFieldById(fieldid);
                String prefix = field.name() + ": ";

                final int index = fieldidIndex[field.id()] + 4;

                switch (field.type()) {
                    case BOOLEAN:
                        values[index] = prefix + buffer.get();
                        break;
                    case DECIMAL:
                        values[index] = prefix + getDouble(buffer);
                        break;
                    case NUMBER:
                    case TIMESTAMP:
                        values[index] = prefix + getLong(buffer);
                        break;
                    case ENUMSET:
                        values[index] = prefix + getBitSet(fieldid, buffer);
                        break;
                    case DATE:
                        values[index] = prefix + buffer.getInt();
                        break;
                    case TIME:
                        values[index] = prefix + TimeFormatter.formatSecondsInDay(buffer.getInt());
                        break;
                    case STRING:
                        int localeIndex = -1;
                        if (field.isLocalized()) {
                            localeIndex = buffer.get();
                        }
                        final int length = Math.max(0, buffer.getShort());
                        String val = (length == 0)
                                ? null : ByteString.readWithLengthFrom(buffer, length).toString();
                        if (localeIndex >= 0) {
                            if (values[index] != null) {
                                values[index] = values[index] + ", " + val;
                            }
                            else {
                                values[index] = prefix + val;
                            }
                            values[index] = values[index]
                                    + "(" + field.getLocales()[localeIndex].toString() + ")";
                        }
                        else {
                            values[index] = prefix + val;
                        }
                        break;
                }
            }

            final Writer writer = this.writerByType.get(type);
            writer.write("###############################################\n");
            for (final String value : values) {
                if (value != null) {
                    writer.write(value + "\r\n");
                }
            }
            writer.write("\r\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getDouble(ByteBuffer buffer) {
        final long l = buffer.getLong();
        return l == Long.MIN_VALUE ? "n/a" : Double.toString((double) l / Constants.SCALE_FOR_DECIMAL);
    }

    private String getLong(ByteBuffer buffer) {
        final long l = buffer.getLong();
        return l == Long.MIN_VALUE ? "n/a" : Long.toString(l);
    }

    private String getBitSet(int fieldId, ByteBuffer buffer) {
        final BitSet bits = RatioEnumSet.read(buffer);
        return bits.isEmpty() ? "n/a" : RatioEnumSetFactory.fromBits(fieldId, bits);
    }

    public static void main(String[] args) throws Exception {
        final BackendUpdateLogger logger = new BackendUpdateLogger();
//        logger.setStream(System.out);
        logger.afterPropertiesSet();

        final MulticastReceiverImpl receiver = new MulticastReceiverImpl();
        receiver.setGroupname("224.0.0.0");
        receiver.setPort(62626);

        final OrderedMulticastReceiver omr = new OrderedMulticastReceiver();
        omr.setReceiver(receiver);
        omr.afterPropertiesSet();

        new Thread(() -> {
            try {
                logger.update(omr.receive());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
