/*
 * MdpexSymbolReader.java
 *
 * Created on 21.01.14 15:13
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export.mdpex;

import java.util.Set;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domainimpl.ItemWithNamesDp2;

/**
 * @author oflege
 */
abstract class MdpexSymbolReader extends MdpexExportReader {

    private Set<KeysystemEnum> allowedEnums;

    protected MdpexSymbolReader(Set<KeysystemEnum> allowedEnums) {
        this.allowedEnums = allowedEnums;
    }

    abstract ItemWithNamesDp2 getItem();

    @Override
    protected void handleRow() {
        KeysystemEnum keysystem = KeysystemMappings.getEnum(getInt("KEYSYSTEM"));
        if (keysystem == null || !this.allowedEnums.contains(keysystem)) {
            return;
        }
        ItemWithNamesDp2 item = getItem();
        if (item == null || item.getId() == 0L) {
            return;
        }
        item.setSymbol(keysystem, get("SYMBOL"));
    }
}
