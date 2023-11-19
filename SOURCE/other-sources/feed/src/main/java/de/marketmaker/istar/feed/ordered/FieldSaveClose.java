/*
 * FieldSave.java
 *
 * Created on 23.08.12 16:12
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

/**
 * @author oflege
 */
public class FieldSaveClose {

    static final int ID_ADF_SCHLUSS_VORTAG
            = VwdFieldOrder.getOrder(VwdFieldDescription.ADF_Schluss_Vortag.id());

    static final int ID_ADF_RUECKNAHME_VORTAG
            = VwdFieldOrder.getOrder(VwdFieldDescription.ADF_Ruecknahme_Vortag.id());

    static final int ID_ADF_NAV_VORTAG
            = VwdFieldOrder.getOrder(VwdFieldDescription.ADF_NAV_Vortag.id());

    static final int ID_ADF_SCHLUSS_VORTAGESDATUM
            = VwdFieldOrder.getOrder(VwdFieldDescription.ADF_Schluss_Vortagesdatum.id());

    private static final int ID_MMF_SCHLUSS_VORVORTAGESDATUM
            = VwdFieldOrder.getOrder(VwdFieldDescription.MMF_Schluss_Vorvortagesdatum.id());

    private static final int ID_MMF_SCHLUSS_VORVORTAG
            = VwdFieldOrder.getOrder(VwdFieldDescription.MMF_Schluss_Vorvortag.id());

    private static final int ID_MMF_NAV_VORVORTAG
            = VwdFieldOrder.getOrder(VwdFieldDescription.MMF_NAV_Vorvortag.id());

    private static final int MAX_ID = 1 +
            Math.max(ID_ADF_SCHLUSS_VORTAG,
                    Math.max(ID_ADF_SCHLUSS_VORTAGESDATUM,
                            Math.max(ID_ADF_RUECKNAHME_VORTAG, ID_ADF_NAV_VORTAG)));


    private int schlussVortagesdatum;

    private long schlussVortag;

    private long ruecknahmeVortag;

    private long navVortag;

    public static FieldData create(FieldData existing, FieldData update) {
        final FieldSaveClose save = new FieldSaveClose(existing);
        final int updatedValue = getSchlussVortagesdatum(update);
        if (updatedValue != 0 && save.getSchlussVortagesdatum() != 0
                && save.getSchlussVortagesdatum() != updatedValue) {
            return save.asUpdate();
        }
        return null;
    }

    private static int getSchlussVortagesdatum(FieldData data) {
        int id = data.readNext();
        while (id != 0 && id < ID_ADF_SCHLUSS_VORTAGESDATUM) {
            data.skipCurrent();
            id = data.readNext();
        }
        return (id == ID_ADF_SCHLUSS_VORTAGESDATUM) ? data.getInt() : 0;
    }

    private FieldSaveClose(FieldData data) {
        for (int id = data.readNext(); id != 0 && id < MAX_ID; id = data.readNext()) {
            if (id == ID_ADF_SCHLUSS_VORTAGESDATUM) {
                this.schlussVortagesdatum = data.getInt();
            }
            else if (id == ID_ADF_SCHLUSS_VORTAG) {
                this.schlussVortag = getPrice(data);
            }
            else if (id == ID_ADF_RUECKNAHME_VORTAG) {
                this.ruecknahmeVortag = getPrice(data);
            }
            else if (id == ID_ADF_NAV_VORTAG) {
                this.navVortag = getPrice(data);
            }
            else {
                data.skipCurrent();
            }
        }
    }

    public int getSchlussVortagesdatum() {
        return this.schlussVortagesdatum;
    }

    public FieldData asUpdate() {
        return new UpdateBuilder().build();
    }

    private long getPrice(FieldData data) {
        return FieldDataUtil.getPrice(data);
    }

    private class UpdateBuilder extends FieldDataBuilder {
        private UpdateBuilder() {
            super(24);
            putIntFid(ID_MMF_SCHLUSS_VORVORTAGESDATUM);
            bb.putInt(schlussVortagesdatum);
            if (schlussVortag != 0 || ruecknahmeVortag != 0) {
                putPriceFid(ID_MMF_SCHLUSS_VORVORTAG);
                putPrice((ruecknahmeVortag != 0) ? ruecknahmeVortag : schlussVortag);
            }
            if (navVortag != 0) {
                putPriceFid(ID_MMF_NAV_VORVORTAG);
                putPrice(navVortag);
            }

            bb.flip();
        }

        private FieldData build() {
            return new BufferFieldData(this.bb);
        }
    }
}
