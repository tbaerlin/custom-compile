/*
 * InstrumentTypeEnum.java
 *
 * Created on 26.09.2008 15:39:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.data;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * This class is a copy of de.marketmaker.istar.domain.instrument.InstrumentTypeEnum.
 * InstrumentTypeEnumTest checks, if the two enums are equal.
 * @author Ulrich Maurer
 */
@SuppressWarnings({"GwtInconsistentSerializableClass"})
public enum InstrumentTypeEnum {
    BND(1, I18n.I.typeBond()),
    CER(2, I18n.I.typeCertificate()),
    CUR(3, I18n.I.typeCurrency()),
    FND(4, I18n.I.typeFund()),
    FUT(5, I18n.I.typeFuture()),
    GNS(6, I18n.I.typeBonusShare()),
    IND(7, I18n.I.typeIndex()),
    MER(8, I18n.I.typeCommodity()),
    MK(9, I18n.I.typeEconomyCycleDate()),
    NON(10, I18n.I.typeUnknown()),
    OPT(11, I18n.I.typeOption()),
    STK(12, I18n.I.typeStock()),
    UND(13, I18n.I.typeUnderlying()),
    WNT(14, I18n.I.typeWarrant()),
    ZNS(15, I18n.I.typeInterestRate()),
    WEA(16, I18n.I.typeWeather()),
    BZG(17, I18n.I.subscriptionRights()),
    SPR(18, I18n.I.spread()),
    IMO(421, I18n.I.realestate()),
    NOT(321, I18n.I.unknown())
    ;


    private final int id;
    private final String description;
    private final static InstrumentTypeEnum[] s_typesById;

    static {
        int max = 0;
        for (final InstrumentTypeEnum typeEnum : values()) {
            max = Math.max(typeEnum.id, max);
        }
        s_typesById = new InstrumentTypeEnum[max + 1];
        for (final InstrumentTypeEnum typeEnum : values()) {
            s_typesById[typeEnum.id] = typeEnum;
        }
    }

    InstrumentTypeEnum(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public static InstrumentTypeEnum valueOf(int id) {
        return s_typesById[id];
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        return name();
    }

    public static InstrumentTypeEnum fromToken(String token) {
        final String type = token.substring(2);
        final InstrumentTypeEnum[] values = InstrumentTypeEnum.values();
        for (InstrumentTypeEnum value : values) {
            if (value.toString().equals(type)) {
                return value;
            }
        }
        return null;
    }

    public static String getDescription(String type, String defaultValue) {
        if (type == null) {
            return defaultValue;
        }
        for (InstrumentTypeEnum value : InstrumentTypeEnum.values()) {
            if (value.toString().equals(type)) {
                return value.getDescription();
            }
        }
        return defaultValue;
    }
}
