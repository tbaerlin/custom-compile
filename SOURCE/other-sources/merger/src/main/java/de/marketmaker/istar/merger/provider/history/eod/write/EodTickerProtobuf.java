/*
 * TickHistoryPersisterJMXClient.java
 *
 * Created on 26.07.12 14:47
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.write;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.history.HistoryUtil;
import de.marketmaker.istar.merger.provider.history.eod.EodUtil;
import de.marketmaker.istar.merger.provider.protobuf.EodHistoryProtos;
import de.marketmaker.istar.merger.provider.protobuf.ProtobufDataStream;

/**
 * @author zzhao
 */
class EodTickerProtobuf implements EodTicker {

    private static final Logger log = LoggerFactory.getLogger(EodTickerProtobuf.class);

    private static final List<Descriptors.FieldDescriptor> FIELDS;

    static {
        final List<Descriptors.FieldDescriptor> list =
                EodHistoryProtos.EodPrice.getDescriptor().getFields();
        FIELDS = new ArrayList<>(list.size() - 2);
        for (Descriptors.FieldDescriptor fd : list) {
            if (fd.getName().startsWith("adf_")) {
                FIELDS.add(fd);
            }
        }
    }

    private final ProtobufDataStream stream;

    private final Type type;

    private final EodHistoryProtos.EodPrice.Builder builder;

    private final int productionDate;

    private int lastDate = 0;

    private static final EnumSet<Type> productionDateRelevantTypes = EnumSet.of(
            Type.EOD_A,
            Type.EOD_C,
            Type.EOD_S
    );

    public EodTickerProtobuf(File file) throws IOException {
        this.type = Type.fromName(file.getName());
        if (productionDateRelevantTypes.contains(this.type)) {
            this.productionDate = DateUtil.toYyyyMmDd(EodUtil.getDateFromProtobuf(file.getName()));
        }
        else {
            this.productionDate = Integer.MAX_VALUE;
        }
        this.stream = new ProtobufDataStream(file);
        this.builder = EodHistoryProtos.EodPrice.newBuilder();
    }

    public static void main(String[] args) throws IOException {
        if (null == args || args.length < 1) {
            System.err.println("Usage: eod_file [output_file]");
            System.exit(1);
        }
        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("press enter: ");
        br.readLine();
        try (
                final EodTicker ticker = new EodTickerProtobuf(new File(args[0]));
                final PrintStream printStream = args.length == 2 ?
                        new PrintStream(new FileOutputStream(args[1])) :
                        System.out;

        ) {
            final TimeTaker tt = new TimeTaker();
            while (ticker.hasNext()) {
                final EodTick next = ticker.next();
                if (null != next) {
                    printStream.println(next);
                }
            }
            System.out.println("took: " + tt);
        }
    }

    Type getType() {
        return type;
    }

    @Override
    public boolean hasNext() {
        return this.stream.hasNext();
    }

    @Override
    public EodTick next() {
        return buildEodPrice();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void close() throws IOException {
        this.stream.close();
    }

    private final EodTick tick = new EodTick();

    private EodTick buildEodPrice() {
        this.builder.clear();
        try {
            this.stream.mergeNext(this.builder);
            final EodHistoryProtos.EodPrice eodPrice = builder.build();
            final int date;
            if (this.lastDate == 0) {
                if (!eodPrice.hasDateVar()) {
                    throw new IllegalStateException("the first eod price must have full date yyyyMMdd");
                }
                date = eodPrice.getDateVar();
            }
            else {
                if (!eodPrice.hasDateVar()) {
                    date = this.lastDate;
                }
                else {
                    date = this.lastDate + eodPrice.getDateVar();
                }
            }
            this.lastDate = date;
            if (date > this.productionDate) {
                if (log.isDebugEnabled()) {
                    log.debug("<buildEodPrice> ignore future price for quote {} at {}",
                            eodPrice.getQuoteId(), date);
                }
                return null;
            }

            this.tick.reset(eodPrice.getQuoteId(), date);
            for (Descriptors.FieldDescriptor field : FIELDS) {
                if (eodPrice.hasField(field)) {
                    this.tick.withField(getFieldNum(field),
                            ((ByteString) eodPrice.getField(field)).toByteArray());
                }
            }
            if (Type.EOD_A == this.type && this.tick.isEmpty()) {
                return null;
            }
            else {
                // in case of EOD_I and EOD_C dealing with field/price deletion
                return this.tick;
            }
        } catch (Exception e) {
            log.error("<buildEodPrice> failed reading from stream", e);
            return null;
        }
    }

    private byte getFieldNum(Descriptors.FieldDescriptor field) {
        final int fieldNum = field.getNumber() - 2;
        HistoryUtil.ensureUnsignedByte(fieldNum);
        return (byte) fieldNum;
    }
}
