/*
 * MdpexExportReader.java
 *
 * Created on 21.01.14 14:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export.mdpex;

import de.marketmaker.istar.common.xml.IstarMdpExportReader;

/**
 * @author oflege
 */
abstract class MdpexExportReader extends IstarMdpExportReader<Long> {

    private static final String MDPCN = "MDPCN";

    private long mdpcn;

    @Override
    protected MdpSaxReader createMdpSaxReader() {
        return new MdpSaxReader() {
            @Override
            protected void endNonRowElement(String uri, String localName, String qName) {
                if (MDPCN.equals(qName)) {
                    mdpcn = getCurrentLong();
                }
            }
        };
    }

    @Override
    protected Long getResult() {
        return mdpcn;
    }
}
