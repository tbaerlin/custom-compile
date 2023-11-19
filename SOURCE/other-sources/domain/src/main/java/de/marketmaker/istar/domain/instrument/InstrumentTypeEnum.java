/*
 * InstrumentTypeEnum.java
 *
 * Created on 17.09.2004 10:30:30
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.instrument;

import java.util.Locale;

import org.springframework.context.MessageSource;

import de.marketmaker.istar.common.spring.MessageSourceFactory;
import de.marketmaker.istar.domain.ItemWithNames;
import de.marketmaker.istar.domain.Language;


/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public enum InstrumentTypeEnum implements ItemWithNames {
    BND(1),
    CER(2),
    CUR(3),
    FND(4),
    FUT(5),
    GNS(6),
    IND(7),
    MER(8),
    MK(9),
    NON(10),
    OPT(11),
    STK(12),
    UND(13),
    WNT(14),
    ZNS(15),  // used for CDS
    WEA(16),
    BZG(17),
    SPR(18),
    IMO(421),
    NOT(321)
    ;
    
    private final int id;

    private static final InstrumentTypeEnum[] TYPES_BY_ID;

    private static final MessageSource MESSAGES
            = MessageSourceFactory.create(InstrumentTypeEnum.class);

    static {
        int max = 0;
        for (final InstrumentTypeEnum typeEnum : values()) {
            max = Math.max(typeEnum.id, max);
        }
        TYPES_BY_ID = new InstrumentTypeEnum[max + 1];
        for (final InstrumentTypeEnum typeEnum : values()) {
            TYPES_BY_ID[typeEnum.id] = typeEnum;
        }
    }

    InstrumentTypeEnum(int id) {
        this.id = id;
    }

    public static InstrumentTypeEnum valueOf(int id) {
        return TYPES_BY_ID[id];
    }

    public int getId() {
        return id;
    }

    @Override
    public String getName(Language language) {
        return MESSAGES.getMessage(name(), null, language.getLocale());
    }

    @Override
    public String getNameOrDefault(Language language) {
        String name = getName(language);
        if(name == null) {
            name = getName(Language.en);
            if(name == null) {
                name = getName(Language.de);
            }
        }

        return name;
    }

    public String getDescription() {
        return getName(Language.de);
    }

    public String getDescription(final Language language) {
        return getName(language);
    }

    public String toString() {
        return name();
    }

    public static void main(String[] args) {
        System.out.println(InstrumentTypeEnum.STK.getDescription());
        System.out.println(MESSAGES.getMessage(CUR.name(), null, Locale.GERMAN));
    }
}
