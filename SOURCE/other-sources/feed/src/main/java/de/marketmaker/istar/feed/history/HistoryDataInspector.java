/*
 * HistoryReader.java
 *
 * Created on 20.08.12 14:42
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.TimeTaker;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * @author zzhao
 */
public class HistoryDataInspector<T extends Comparable<T>> implements Closeable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final HistoryUnit unit;

    private final Class<T> clazz;

    private File file;

    private DataFile dataFile;

    private Interval interval;

    private ItemExtractor<T> extractor;

    public HistoryDataInspector(Class<T> clazz, HistoryUnit unit) {
        this.clazz = clazz;
        this.unit = unit;
    }

    public Interval getInterval() {
        return interval;
    }

    public HistoryUnit getUnit() {
        return unit;
    }

    public void setFile(File file) throws IOException {
        final TimeTaker tt = new TimeTaker();
        if (null == file) {
            close();
        }
        else if (!file.equals(this.file)) {
            close();
            this.logger.info("<setFile> {} {}", this.unit, file.getAbsolutePath());
            this.file = file;
            this.dataFile = new DataFile(this.file, true);
            this.interval = this.unit.getInterval(this.file);
            if (this.dataFile.size() <= 0) {
                this.logger.warn("<setFile> empty history file: " + this.file.getAbsolutePath());
                return;
            }
            this.extractor = new ItemExtractor<>(this.clazz, this.dataFile);
        }
        this.logger.info("<setFile> took: {}", tt);
    }

    @Override
    public void close() throws IOException {
        if (null != this.file) {
            this.logger.info("<close> {} {}", this.unit, this.file.getAbsolutePath());
            this.dataFile.close();
            this.dataFile = null;
            this.file = null;
            this.interval = null;
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: history_dir repair_dir");
            System.exit(1);
        }

        final File dir = new File(args[0]);
        final File repairDir = new File(args[1]);
        final File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                try {
                    HistoryUnit.fromExt(pathname);
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        });
        if (null == files) {
            System.exit(0);
        }
        final TreeMap<String, Integer> lengths = new TreeMap<>();
        for (File file : files) {
            final HistoryUnit unit = HistoryUnit.fromExt(file);
            final ArrayList<Block> blocks = new ArrayList<>();
            final List<MyItem> inOrderItems;
            try (
                final HistoryDataInspector<ByteString> reader =
                    new HistoryDataInspector<>(ByteString.class, unit)
            ) {
                reader.setFile(file);
                final Iterator<Item<ByteString>> itemIt = reader.extractor.iterator();
                List<MyItem> myItems = new ArrayList<>();
                int idx = 0;
                while (itemIt.hasNext()) {
                    Item<ByteString> next = itemIt.next();
                    myItems.add(new OneItem(idx, next));
                    idx++;
                }

                boolean outOfOrder;
                do {
                    outOfOrder = false;
                    List<MyItem> tmp = new ArrayList<>(myItems.size());
                    for (int i = 0; i < myItems.size(); i++) {
                        final MyItem myItem = myItems.get(i);
                        if (isOutOfOrder(tmp, myItem)) {
                            final Block block = merge(tmp.remove(tmp.size() - 1), myItem);
                            tmp.add(block);
                            outOfOrder = true;
                        } else {
                            tmp.add(myItem);
                        }
                    }
                    myItems = tmp;
                } while (outOfOrder);

                long offset = reader.extractor.getIndexOffsetStart();
                for (int i = myItems.size() - 1; i >= 0; i--) {
                    final MyItem myItem = myItems.get(i);
                    offset = check(myItem, offset);
                    if (myItem instanceof Block) {
                        blocks.add((Block) myItem);
                    }
                }
                if (offset != 0) {
                    throw new IllegalStateException("data inconsistent");
                }

                final MutableInt theIndex = new MutableInt(0);
                for (int i = 0; i < myItems.size(); i++) {
                    final MyItem myItem = myItems.get(i);
                    if (myItem instanceof OneItem) {
                        final OneItem oneItem = (OneItem) myItem;
                        Assert.isTrue(oneItem.index == theIndex.intValue(), "index inconsistent: "
                            + myItem + ", expected index: " + theIndex.intValue());
                        theIndex.increment();
                    } else {
                        final Block block = (Block) myItem;
                        for (OneItem oneItem : block.myItems) {
                            Assert.isTrue(oneItem.index == theIndex.intValue(),
                                "index inconsistent: "
                                    + myItem + ", expected index: " + theIndex.intValue());
                            theIndex.increment();
                        }
                    }
                }

                inOrderItems = myItems;

                if (!blocks.isEmpty()) {
                    final File repairFile = new File(repairDir, file.getName() + ".c.txt");
                    int len = 0;
                    try (final BufferedWriter bw = new BufferedWriter(new FileWriter(repairFile))) {
                        for (int i = blocks.size() - 1; i >= 0; i--) {
                            bw.write(blocks.get(i).toString());
                            bw.newLine();
                            len += blocks.get(i).length();
                        }
                    }
                    lengths.put(repairFile.getName(), len);
                    try (final DataFile mpc = new DataFile(
                        new File(repairDir, file.getName() + ".cr"),
                        false)) {
                        final ByteBuffer bb = ByteBuffer.allocate(len + 4 + blocks.size() * 12);
                        bb.putInt(blocks.size());

                        final OneLevelBsTree<ByteString> bsTree = new OneLevelBsTree<>(ByteString.class);
                        final List<Item<ByteString>> chgItems = new ArrayList<>();

                        for (MyItem inOrderItem : inOrderItems) {
                            if (inOrderItem instanceof Block) {
                                final Block block = (Block) inOrderItem;
                                final List<Item<ByteString>> list =
                                    block.consolidate(reader.dataFile, bb);
                                chgItems.addAll(list);
                                for (Item<ByteString> bsItem : list) {
                                    bsTree.addItem(bsItem);
                                }
                            } else {
                                bsTree.addItem(((OneItem) inOrderItem).item);
                            }
                        }
                        bb.flip();
                        mpc.write(bb);
                        final ByteBuffer chgItemsBuf = ByteBuffer.allocate(512 * 1024);
                        chgItemsBuf.putInt(chgItems.size());
                        for (Item<ByteString> chgItem : chgItems) {
                            chgItem.getKey().writeTo(chgItemsBuf, ByteString.LENGTH_ENCODING_BYTE);
                        }
                        chgItemsBuf.flip();
                        mpc.write(chgItemsBuf);
                        bsTree.finish(mpc, mpc.size());
                    }
                }
            }
        }
        try (final BufferedWriter bw = new BufferedWriter(new FileWriter(new File(repairDir,
            dir.getName() + "_repair_sizes.txt")))) {
            for (Map.Entry<String, Integer> entry : lengths.entrySet()) {
                bw.write(entry.getKey() + ": " + toMBytes(entry.getValue()));
                bw.newLine();
            }
        }
    }

    private static String toMBytes(int value) {
        return String.valueOf((double) value / (1024 * 1024) + " M");
    }

    private static long check(MyItem myItem, long pivot) {
        if (myItem instanceof OneItem) {
            final OneItem oneItem = (OneItem) myItem;
            Assert.isTrue(oneItem.item.getOffset() + oneItem.item.getLength() == pivot,
                    "data inconsistent: " + myItem + ", pivot: " + pivot);
            return oneItem.item.getOffset();
        }
        else {
            final Block block = (Block) myItem;
            return block.check(pivot);
        }
    }

    private static Block merge(MyItem lastItem, MyItem curItem) {
        if (curItem instanceof OneItem) {
            final OneItem curOneItem = (OneItem) curItem;
            if (lastItem instanceof OneItem) {
                final Block block = new Block(curOneItem);
                block.addItem((OneItem) lastItem);
                return block;
            }
            else {
                final Block lastBlock = (Block) lastItem;
                lastBlock.addItem(curOneItem);
                return lastBlock;
            }
        }
        else {
            final Block curBlock = (Block) curItem;
            if (lastItem instanceof OneItem) {
                curBlock.addItem((OneItem) lastItem);
                return curBlock;
            }
            else {
                final Block lastBlock = (Block) lastItem;
                lastBlock.addBlock(curBlock);
                return lastBlock;
            }
        }
    }

    private static boolean isOutOfOrder(List<MyItem> tmp, MyItem myItem) {
        if (tmp.isEmpty()) {
            return false;
        }
        final MyItem lastItem = tmp.get(tmp.size() - 1);
        if (myItem instanceof OneItem) {
            final OneItem curOneItem = (OneItem) myItem;
            if (lastItem instanceof OneItem) {
                final OneItem lastOneItem = (OneItem) lastItem;
                return curOneItem.item.getKey().compareTo(lastOneItem.item.getKey()) <= 0;
            }
            else {
                final Block lastBlock = (Block) lastItem;
                return curOneItem.item.getKey().compareTo(lastBlock.getLastKey()) <= 0;
            }
        }
        else {
            final Block curBlock = (Block) myItem;
            if (lastItem instanceof OneItem) {
                final OneItem lastOneItem = (OneItem) lastItem;
                return curBlock.getFirstKey().compareTo(lastOneItem.item.getKey()) <= 0;
            }
            else {
                final Block lastBlock = (Block) lastItem;
                return curBlock.getFirstKey().compareTo(lastBlock.getLastKey()) <= 0;
            }
        }
    }

    private static abstract class MyItem {
    }

    private static final class OneItem extends MyItem {

        private final int index;

        private final Item<ByteString> item;

        private OneItem(int idx, Item<ByteString> item) {
            this.index = idx;
            this.item = item;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            OneItem oneItem = (OneItem) o;

            if (index != oneItem.index) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return index;
        }

        @Override
        public String toString() {
            return "{" + this.index + ":" + this.item + "}";
        }
    }

    private static final class Block extends MyItem {

        private final TreeSet<OneItem> myItems = new TreeSet<>(new Comparator<OneItem>() {
            @Override
            public int compare(OneItem o1, OneItem o2) {
                return o1.index - o2.index;
            }
        });

        private final TreeSet<ByteString> itemKeys = new TreeSet<>();

        private static final StringBuilder sb = new StringBuilder();

        private Block(OneItem oneItem) {
            this.myItems.add(oneItem);
            this.itemKeys.add(oneItem.item.getKey());
        }

        private void addItem(OneItem oneItem) {
            if (this.myItems.contains(oneItem)) {
                throw new IllegalArgumentException("item already in list: " + oneItem);
            }
            this.myItems.add(oneItem);
            this.itemKeys.add(oneItem.item.getKey());
        }

        private ByteString getFirstKey() {
            return this.itemKeys.first();
        }

        private ByteString getLastKey() {
            return this.itemKeys.last();
        }

        private long check(long pivot) {
            long offset = pivot;
            final ArrayList<OneItem> oneItems = new ArrayList<>(this.myItems);
            for (int i = oneItems.size() - 1; i >= 0; i--) {
                final OneItem oneItem = oneItems.get(i);
                Assert.isTrue(oneItem.item.getOffset() + oneItem.item.getLength() == offset,
                        "data inconsistent: " + oneItem + ", pivot: " + offset);
                offset = oneItem.item.getOffset();
            }
            return offset;
        }

        private List<Item<ByteString>> consolidate(DataFile dataFile,
                ByteBuffer bb) throws IOException {
            final TreeMap<ByteString, TreeMap<Short, byte[]>> map = new TreeMap<>();
            for (OneItem myItem : myItems) {
                final Item<ByteString> item = myItem.item;
                if (!map.containsKey(item.getKey())) {
                    map.put(item.getKey(), new TreeMap<Short, byte[]>(Collections.reverseOrder()));
                }
                consolidate(dataFile, map.get(item.getKey()), item);
            }

            final ArrayList<Item<ByteString>> list = new ArrayList<>();
            long offset = myItems.first().item.getOffset();
            bb.putLong(offset);
            int len = 0;
            final int lenPos = bb.position();
            bb.putInt(len); // place holder
            for (Map.Entry<ByteString, TreeMap<Short, byte[]>> entry : map.entrySet()) {
                int itemLen = 0;
                for (byte[] bytes : entry.getValue().values()) {
                    bb.put(bytes);
                    itemLen += bytes.length;
                }
                list.add(new Item<>(entry.getKey(), offset, itemLen));
                offset += itemLen;
                len += itemLen;
            }
            bb.putInt(lenPos, len);
            return list;
        }

        private void consolidate(DataFile dataFile, TreeMap<Short, byte[]> dataMap,
                Item<ByteString> item) throws IOException {
            final ByteBuffer bb = ByteBuffer.allocate(item.getLength());
            dataFile.seek(item.getOffset());
            dataFile.read(bb);
            bb.flip();
            while (bb.hasRemaining()) {
                final short days = bb.getShort();
                final short numTicks = bb.getShort();
                final short len = bb.getShort();
                final ByteBuffer buf = ByteBuffer.allocate(len + 6);
                buf.putShort(days);
                buf.putShort(numTicks);
                buf.putShort(len);
                bb.get(buf.array(), 6, len);
                dataMap.put(days, buf.array());
            }
        }

        @Override
        public String toString() {
            sb.setLength(0);
            for (OneItem item : this.myItems) {
                sb.append(item.toString()).append("#");
            }
            return sb.toString();
        }

        public void addBlock(Block block) {
            for (OneItem myItem : block.myItems) {
                addItem(myItem);
            }
        }

        public int length() {
            int ret = 0;
            for (OneItem myItem : myItems) {
                ret += myItem.item.getLength();
            }

            return ret;
        }
    }
}