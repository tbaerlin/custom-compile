/*
 * ImgChartKennzahlenCommand.java
 *
 * Created on 28.08.2006 16:30:59
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.RestrictedSet;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ImgChartKennzahlenCommand extends BaseImgSymbolCommand {

    private String typ;

    public ImgChartKennzahlenCommand() {
        super(300, 200, null);
    }

    @NotNull
    @RestrictedSet("umsatz,gewinn,dividende,mitarbeiter")
    public String getTyp() {
        return typ;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }

    public StringBuilder appendParameters(StringBuilder sb) {
        super.appendParameters(sb);
        sb.append("&typ=").append(getTyp());
        return sb;
    }
}
