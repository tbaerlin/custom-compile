/*
 * FieldSave.java
 *
 * Created on 23.08.12 16:12
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.feed.mdps.MdpsFeedUtils.decodeTime;

/**
 * @author oflege
 */
public class FieldSaveBezahlt {

    static final int ID_ADF_BEZAHLT = VwdFieldOrder.ORDER_ADF_BEZAHLT;

    static final int ID_ADF_HANDELSDATUM = VwdFieldOrder.ORDER_ADF_HANDELSDATUM;

    static final int ID_ADF_BEZAHLT_ZEIT = VwdFieldOrder.ORDER_ADF_BEZAHLT_ZEIT;

    static final int ID_MMF_BOERSENZEIT = VwdFieldOrder.ORDER_MMF_BOERSENZEIT;

    static final int ID_MMF_BEZAHLT_DATUM = VwdFieldOrder.ORDER_MMF_BEZAHLT_DATUM;

    static final int ADF_MAX_ID = Math.max(ID_ADF_BEZAHLT,
            Math.max(ID_ADF_BEZAHLT_ZEIT, ID_ADF_HANDELSDATUM));

    static final int MMF_MAX_ID = Math.max(ID_MMF_BEZAHLT_DATUM, ID_MMF_BOERSENZEIT);

    private int handelsdatum;

    private int bezahltZeit;

    private long bezahlt;

    private int mmfBoersenzeit;

    private int mmfBezahltDatum;

    public static FieldData create(FieldData existing, FieldData update) {
        return new FieldSaveBezahlt(existing, update).asUpdate();
    }

    private FieldSaveBezahlt(FieldData existing, FieldData update) {
        initFromExisting(existing);
        initFromUpdate(update);
    }

    private void initFromExisting(FieldData existing) {
        for (int id = existing.readNext(); id != 0 && id <= MMF_MAX_ID; id = existing.readNext()) {
            switch (id) {
                case ID_MMF_BEZAHLT_DATUM:
                    this.mmfBezahltDatum = existing.getInt();
                    break;
                case ID_MMF_BOERSENZEIT:
                    this.mmfBoersenzeit = existing.getInt();
                    break;
                default:
                    existing.skipCurrent();
                    break;
            }
        }
    }

    private void initFromUpdate(FieldData update) {
        for (int id = update.readNext(); id != 0 && id <= ADF_MAX_ID; id = update.readNext()) {
            switch (id) {
                case ID_ADF_BEZAHLT:
                    this.bezahlt = FieldDataUtil.getPrice(update);
                    break;
                case ID_ADF_BEZAHLT_ZEIT:
                    this.bezahltZeit = update.getInt();
                    break;
                case ID_ADF_HANDELSDATUM:
                    this.handelsdatum = update.getInt();
                    break;
                default:
                    update.skipCurrent();
                    break;
            }
        }
    }

    private FieldData asUpdate() {
        if (((int) this.bezahlt) == 0 || this.handelsdatum == 0) {
            return null;
        }
        if (this.handelsdatum < this.mmfBezahltDatum) {
            return null;
        }
        if (this.handelsdatum == this.mmfBezahltDatum
                && decodeTime(this.bezahltZeit) < decodeTime(this.mmfBoersenzeit)) {
            return null;
        }
        return new UpdateBuilder().build();
    }

    private class UpdateBuilder extends FieldDataBuilder {
        private UpdateBuilder() {
            super(16);
            putTimeFid(ID_MMF_BOERSENZEIT);
            bb.putInt(bezahltZeit);
            putIntFid(ID_MMF_BEZAHLT_DATUM);
            bb.putInt(handelsdatum);
            bb.flip();
        }

        private FieldData build() {
            return new BufferFieldData(this.bb);
        }
    }
}
