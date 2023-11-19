/*
 * MainInput.java
 *
 * Created on 26.05.2015 10:21
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.ShellMMTypeUtil;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMClassIndex;
import de.marketmaker.iview.pmxml.MMPortfolioVersion;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author mdick
 */
@NonNLS
public interface MainInput {
    /**
     * @return The mm value. Should never return null.
     */
    MM getMM();
    String getId();
    MMClassIndex getMMClassIndex();
    @SuppressWarnings("unused")
    String getOwnerId();

    String getObjectIdToGoOnError();
    ShellMMType getObjectTypeToGoOnError();

    String getName();
    String getIconKey();

    ShellMMInfo asShellMMInfo();

    final class Factory {
        static MainInput get(MM mm) {
            if(mm instanceof ShellMMInfo) {
                return new ShellMMInfoMainInput((ShellMMInfo) mm);
            }
            if(mm instanceof MMPortfolioVersion) {
                return new PortfolioVersionMainInput((MMPortfolioVersion) mm);
            }
            return null;
        }

        private static final class ShellMMInfoMainInput implements MainInput {
            private final ShellMMInfo mm;
            private final MMClassIndex mmClassIndex;

            public ShellMMInfoMainInput(ShellMMInfo mm) {
                this.mm = mm;
                this.mmClassIndex = ShellMMTypeUtil.toMMClassIndex(mm.getTyp());
                if(this.mmClassIndex == null) {
                    throw new IllegalArgumentException("Conversion from ShellMMType '" + mm + "' to MMClassIndex is not defined.");
                }
            }

            @Override
            public MM getMM() {
                return this.mm;
            }

            @Override
            public String getId() {
                return this.mm.getId();
            }

            @Override
            public String getOwnerId() {
                return null;
            }

            @Override
            public ShellMMInfo asShellMMInfo() {
                return this.mm;
            }

            public ShellMMType getObjectTypeToGoOnError() {
                return this.mm.getTyp();
            }

            @Override
            public String getObjectIdToGoOnError() {
                return this.mm.getId();
            }

            @Override
            public MMClassIndex getMMClassIndex() {
                return this.mmClassIndex;
            }

            @Override
            public String getName() {
                return this.mm.getBezeichnung();
            }

            @Override
            public String getIconKey() {
                return ShellMMTypeUtil.getIconKey(this.mmClassIndex);
            }
        }

        private static final class PortfolioVersionMainInput implements MainInput {
            private final MMPortfolioVersion mm;

            public PortfolioVersionMainInput(MMPortfolioVersion mm) {
                this.mm = mm;
            }

            @Override
            public MM getMM() {
                return this.mm;
            }

            @Override
            public String getId() {
                return this.mm.getId();
            }

            @Override
            public String getOwnerId() {
                return this.mm.getPortfolioId();
            }

            @Override
            public ShellMMInfo asShellMMInfo() {
                return null;
            }

            @Override
            public MMClassIndex getMMClassIndex() {
                return MMClassIndex.CI_TMM_PORTFOLIO_VERSION;
            }

            @Override
            public String getName() {
                return "PortfolioVersion { id=" + this.mm.getId() + ", portfolioId=" + this.mm.getPortfolioId() + " }";
            }

            @Override
            public String getObjectIdToGoOnError() {
                return this.mm.getPortfolioId();
            }

            @Override
            public ShellMMType getObjectTypeToGoOnError() {
                return ShellMMType.ST_PORTFOLIO;
            }

            @Override
            public String getIconKey() {
                return ShellMMTypeUtil.getIconKey(ShellMMType.ST_PORTFOLIO);
            }
        }
    }
}
