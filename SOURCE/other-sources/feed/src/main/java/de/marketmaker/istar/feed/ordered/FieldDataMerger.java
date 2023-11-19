/*
 * FieldDataBuilder.java
 *
 * Created on 11.07.12 10:57
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.util.Arrays;

import net.jcip.annotations.NotThreadSafe;

/**
 * Provides {@link #merge(FieldData, FieldData)} method to merge updates from
 * one {@link FieldData} into another. Since Field-Ids are ordered, merging can be done
 * in lock-step. Also supports removing fields, use {@link #remove(FieldData, FieldData)}.
 *
 * @author oflege
 */
@NotThreadSafe
public class FieldDataMerger extends FieldDataBuilder {

    public FieldDataMerger() {
        super(8192);
    }

    public FieldDataMerger(int size) {
        super(size);
    }

    /**
     * Applies all updates in update to existing, that is, after the merge, all the fields in
     * update will be present in existing (or in the returned byte[]) and have the same value.
     * @param existing update target
     * @param update update source
     * @return null iff merge could be done in place, otherwise the complete merged encoded
     *         FieldData
     */
    public byte[] merge(FieldData existing, FieldData update) {
        this.lastOrder = 0;
        int existingId = existing.readNext();
        int updateId = update.readNext();
        while (existingId != 0 && updateId != 0) {
            if (existingId == updateId) {
                if (!existing.mergeFieldFrom(update)) {
                    return mergeInBuffer(existing, update);
                }
                this.lastOrder = existingId;
                updateId = update.readNext();
                existingId = existing.readNext();
            }
            else if (updateId < existingId) {
                return mergeInBuffer(existing, update);
            }
            else {
                this.lastOrder = existingId;
                existing.skipCurrent();
                existingId = existing.readNext();
            }
        }
        if (updateId != 0) {
            return appendToBuffer(existing.getAsByteArray(), update);
        }
        return null;
    }

    /**
     * Appends fd's remaining fields after the encoded fields in data
     * @param data encoded FieldData bytes
     * @param fd to be appended
     * @return encoded FieldData
     */
    private byte[] appendToBuffer(byte[] data, FieldData fd) {
        this.bb.clear();
        this.bb.put(data);
        return appendToBuffer(fd);
    }

    private byte[] appendToBuffer(FieldData fd) {
        for (int id = fd.getId(); id != 0; id = fd.readNext()) {
            addFieldToBuffer(id, fd);
        }
        return copyBuffer();
    }

    /**
     * Add the current field in fd to <tt>this.bb</tt>
     * @param id fd's current id
     * @param fd data to be added
     */
    private void addFieldToBuffer(int id, FieldData fd) {
        switch (fd.getType()) {
            case FieldData.TYPE_INT:
                putIntFid(id);
                this.bb.putInt(fd.getInt());
                break;
            case FieldData.TYPE_TIME:
                putTimeFid(id);
                this.bb.putInt(fd.getInt());
                break;
            case FieldData.TYPE_PRICE:
                putPriceFid(id);
                this.bb.putInt(fd.getInt());
                this.bb.put(fd.getByte());
                break;
            case FieldData.TYPE_STRING:
                putStringFid(id);
                putStopBitEncoded(fd.getLength());
                this.bb.put(fd.getBytes());
                break;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * merges existing and update in <tt>this.bb</tt>; up to the current field, updates
     * have already been merged from update into existing, so only the remaining fields need
     * to be processed
     * @param existing update target
     * @param update update source
     * @return merged FieldData
     */
    private byte[] mergeInBuffer(FieldData existing, FieldData update) {
        this.bb.clear();
        this.bb.put(existing.getAsByteArrayBeforeCurrent());

        int existingId = existing.getId();
        int updateId = update.getId();

        while (existingId != 0 || updateId != 0) {
            if (existingId == 0) {
                return appendToBuffer(update);
            }
            if (updateId == 0) {
                return appendToBuffer(existing);
            }
            while (existingId != 0 && existingId < updateId) {
                addFieldToBuffer(existingId, existing);
                existingId = existing.readNext();
            }
            while (updateId != 0 && updateId <= existingId) {
                addFieldToBuffer(updateId, update);
                updateId = update.readNext();
                if (this.lastOrder == existingId) {
                    existing.skipCurrent();
                    existingId = existing.readNext();
                }
            }
        }
        return copyBuffer();
    }

    /**
     * Removes all fields present in update from existing; field values in update are ignored.
     * @param existing update target
     * @param update update source
     * @return if any field in existing was removed, the new field data w/o those field(s),
     *         null otherwise.
     */
    public byte[] remove(FieldData existing, FieldData update) {
        this.lastOrder = 0;
        int existingId = existing.readNext();
        int updateId = update.readNext();
        while (existingId != 0 && updateId != 0) {
            if (existingId < updateId) {
                this.lastOrder = existingId;
                existing.skipCurrent();
                existingId = existing.readNext();
            }
            else if (updateId < existingId) {
                update.skipCurrent();
                updateId = update.readNext();
            }
            else if (existingId == updateId) {
                return removeInBuffer(existing, update);
            }
        }
        return null;
    }

    private byte[] removeInBuffer(FieldData existing, FieldData update) {
        this.bb.clear();
        this.bb.put(existing.getAsByteArrayBeforeCurrent());

        int existingId = existing.getId();
        int updateId = update.getId();
        while (existingId != 0 || updateId != 0) {
            if (updateId == 0) {
                return appendToBuffer(existing);
            }
            while (existingId != 0 && existingId < updateId) {
                addFieldToBuffer(existingId, existing);
                existingId = existing.readNext();
            }
            if (updateId == existingId) {
                existing.skipCurrent();
                existingId = existing.readNext();
            }
            update.skipCurrent();
            updateId = update.readNext();
        }
        return copyBuffer();
    }

    private byte[] copyBuffer() {
        return Arrays.copyOf(this.bb.array(), this.bb.position());
    }
}
