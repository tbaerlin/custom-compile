/*
 * PmSearchTestMock.java
 *
 * Created on 04.01.13 15:27
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.mock;

import com.google.gwt.i18n.client.NumberFormat;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.ShellObjectSearchResponse;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

/**
 * @author Markus Dick
 */
@NonNLS
final class PmSearchTestMock {
    public static ShellObjectSearchResponse doSomething(final String searchString) {
        final NumberFormat isinNf = NumberFormat.getFormat("'DE'0000000000");
        final NumberFormat wknNf = NumberFormat.getFormat("0000000");

        final ShellObjectSearchResponse result = new ShellObjectSearchResponse();
        final List<ShellMMInfo> shellObjects = result.getObjects();

        ShellMMInfo info = new ShellMMInfo();
        info.setBezeichnung("vwd");
        info.setISIN("DE0005204705");
        info.setNumber("5204705");
        info.setIsQuote(false);
        info.setIsTrash(false);
        info.setTyp(ShellMMType.ST_AKTIE);
        shellObjects.add(info);

        for(int i = 1; i < 30001; i++) {

            final ShellMMType type;

            switch(i%9) {
                case 0:
                    type = ShellMMType.ST_AKTIE;
                    break;
                case 1:
                    type = ShellMMType.ST_FOND;
                    break;
                case 2:
                    type = ShellMMType.ST_INDEX;
                    break;
                case 3:
                    type = ShellMMType.ST_ANLEIHE;
                    break;
                case 4:
                    type = ShellMMType.ST_CERTIFICATE;
                    break;
                case 5:
                    type = ShellMMType.ST_OPTION;
                    break;
                case 6:
                    type = ShellMMType.ST_FUTURE;
                    break;
                case 7:
                    type = ShellMMType.ST_GENUSS;
                    break;
                case 8:
                    type = ShellMMType.ST_BEZUGSRECHT;
                    break;
                default:
                    type = ShellMMType.ST_AKTIE;
            }

            info = new ShellMMInfo();
            info.setBezeichnung(searchString + " " + type.name().substring(3) + " " + i);
            info.setISIN(isinNf.format(4200000 + i));
            info.setNumber(wknNf.format(4200000 + i));
            info.setIsQuote(false);
            info.setIsTrash(false);
            info.setTyp(type);

            shellObjects.add(info);
        }

        return result;
    }
}
