/*
 * WmIssuerReader.java
 *
 * Created on 24.04.14 07:35
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.gisresearch;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.apache.commons.lang3.mutable.MutableInt;

import de.marketmaker.istar.merger.provider.protobuf.ProtobufDataReader;
import de.marketmaker.istar.merger.provider.protobuf.WmDataProtos;

import static de.marketmaker.istar.instrument.export.GisResearchContentFlagsPostProcessor.encodeIidAndFlag;

/**
 * Temporary hack to create a file that can be used to add content flags for gis research availability.
 *
 * @author oflege
 */
class GisResearchContentFlagsWriter extends ProtobufDataReader {

    private final Int2IntMap issuerNumbersToFlags = new Int2IntOpenHashMap();

    private int num;

    private Int2ObjectMap<IntList> issuerNumbersToIids;

    GisResearchContentFlagsWriter(Map<String, MutableInt> contentFlags) {
        super(WmDataProtos.WmMasterData.getDescriptor());

        for (Map.Entry<String, MutableInt> e : contentFlags.entrySet()) {
            this.issuerNumbersToFlags.put(Integer.parseInt(e.getKey()), e.getValue().intValue());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.issuerNumbersToIids = readAllRecords();
    }

    void writeContentFlags(File f) throws IOException {
        final long[] elements = getAsLongArray();

        final ByteBuffer bb = ByteBuffer.allocate(elements.length * 8);
        bb.asLongBuffer().put(elements);

        if (f.canRead() && !f.delete()) {
            throw new IOException("failed to delete " + f.getAbsolutePath());
        }

        try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
            raf.getChannel().write(bb);
        }

        this.logger.info("<writeContentFlags> wrote " + elements.length + " entries");
    }

    private long[] getAsLongArray() {
        LongList ll = new LongArrayList(num);
        for (IntIterator it = issuerNumbersToIids.keySet().iterator(); it.hasNext(); ) {
            int issuerNumber = it.nextInt();
            int flags = this.issuerNumbersToFlags.get(issuerNumber);
            IntList iids = issuerNumbersToIids.get(issuerNumber);
            for (IntIterator it2 = iids.iterator(); it2.hasNext(); ) {
                ll.add(encodeIidAndFlag(it2.nextInt(), flags));
            }
        }
        final long[] result = ll.toLongArray();
        Arrays.sort(result);
        return result;
    }

    private Int2ObjectMap<IntList> readAllRecords() throws com.google.protobuf.InvalidProtocolBufferException {
        final Int2ObjectMap<IntList> gd245ToIids = new Int2ObjectOpenHashMap<>();
        final Int2ObjectMap<IntList> gd240ToIids = new Int2ObjectOpenHashMap<>();


        Arrays.sort(keysAndOffsets[1]);

        for (long keyAndOffset : keysAndOffsets[1]) {
            byte[] bytes = getSerialized(0L, keyAndOffset);
            WmDataProtos.WmMasterData.Builder builder = WmDataProtos.WmMasterData.newBuilder();
            builder.mergeFrom(bytes);
            WmDataProtos.WmMasterData wm = builder.buildPartial();
            if (wm.hasGd245() && this.issuerNumbersToFlags.containsKey(wm.getGd245())) {
                add(gd245ToIids, wm.getGd245(), wm.getIid());
            }
            if (wm.hasGd240() && this.issuerNumbersToFlags.containsKey(wm.getGd240())) {
                add(gd240ToIids, wm.getGd240(), wm.getIid());
            }
        }

        // override entries in gd240, in effect preferring gd245 over gd240
        gd240ToIids.putAll(gd245ToIids);
        return gd240ToIids;
    }

    private void add(Int2ObjectMap<IntList> map, int issuerNumber, long iid) {
        IntList iids = map.get(issuerNumber);
        if (iids == null) {
            map.put(issuerNumber, iids = new IntArrayList());
        }
        iids.add((int) iid);
        this.num++;
    }

    public static void main(String[] args) throws Exception {
        HashMap<String, MutableInt> m = new HashMap<>();
        m.put("353098", new MutableInt(0b1100));
        m.put("208403", new MutableInt(0b0001));
        GisResearchContentFlagsWriter r = new GisResearchContentFlagsWriter(m);
        r.setFile(new File(args[0]));
        r.afterPropertiesSet();
        r.writeContentFlags(new File(args[1]));
    }
}
