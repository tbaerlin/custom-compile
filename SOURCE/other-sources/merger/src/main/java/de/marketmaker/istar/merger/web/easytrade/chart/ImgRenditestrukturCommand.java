/*
 * ImgRenditestrukturCommand.java
 *
 * Created on 28.08.2006 16:31:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ImgRenditestrukturCommand extends BaseImgCommand {

    private String[] countryCodes;
    
    public ImgRenditestrukturCommand() {
        super(300, 200);
    }

    public String[] getCountryCodes() {
        return this.countryCodes;
    }

    public void setCountryCodes(String[] countryCodes) {
        this.countryCodes = separate(countryCodes);
    }

    @Override
    public StringBuilder appendParameters(StringBuilder sb) {
        super.appendParameters(sb);
        appendParameters(sb, this.countryCodes, "countryCodes");
        return sb;
    }
}
