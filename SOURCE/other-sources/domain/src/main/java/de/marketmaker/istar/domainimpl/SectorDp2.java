/*
* SectorDp2.java
*
* Created on 18.08.2006 13:25:58
*
* Copyright (c) market maker Software AG. All Rights Reserved.
*/

package de.marketmaker.istar.domainimpl;

import java.io.Serializable;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.Sector;

/**
 * @author Martin Wilke
 */

public class SectorDp2 extends ItemWithNamesDp2 implements Sector, Serializable {
    static final long serialVersionUID = 8911809649928289766L;

    private String name;

    public SectorDp2() {
    }

    public SectorDp2(long id, String name) {
        super(id);
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getSymbolDpTeam() {
        return getSymbol(KeysystemEnum.DP_TEAM);
    }
}
