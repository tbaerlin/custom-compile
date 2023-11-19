/*
 * KeysystemMappings.java
 *
 * Created on 21.01.14 14:10
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export.mdpex;

import de.marketmaker.istar.domain.KeysystemEnum;

/**
 * @author oflege
 */
class KeysystemMappings {
    private static final KeysystemEnum[] KEYSYSTEMS_BY_MDP_ID = new KeysystemEnum[796];

    static {
        KEYSYSTEMS_BY_MDP_ID[1] = KeysystemEnum.ISO;
        KEYSYSTEMS_BY_MDP_ID[2] = KeysystemEnum.VWDFEED;
        KEYSYSTEMS_BY_MDP_ID[7] = KeysystemEnum.MMWKN;
        KEYSYSTEMS_BY_MDP_ID[13] = KeysystemEnum.DP_TEAM;
        KEYSYSTEMS_BY_MDP_ID[174] = KeysystemEnum.MM;
        KEYSYSTEMS_BY_MDP_ID[795] = KeysystemEnum.WM;
    }

    static KeysystemEnum getEnum(int i) {
        return (i >= 0 && i < KEYSYSTEMS_BY_MDP_ID.length) ? KEYSYSTEMS_BY_MDP_ID[i] : null;
    }
}
