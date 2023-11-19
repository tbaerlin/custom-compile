/*
 * InvestorPrivacyModeProvider.java
 *
 * Created on 13.05.2015 08:02
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.PrivacyModeAllowedObjectId;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * PrivacyModeProvider impl. for investors as well as portfolios, depots, and accounts.
 * Not applicable for persons, because there it is not clear, which investor portrait should be selected.
 *
 * A PrivacyModeProvider implementation for prospects is directly implemented in {@linkplain ProspectPortraitController#asPrivacyModeProvider()}
 *
 * @author mdick
 */
@NonNLS
public class InvestorPrivacyModeProvider extends AbstractPrivacyModeProvider {
    private final BlockAndTalker<PrivacyModeAllowedObjectId.Talker, List<PrivacyModeAllowedObjectId>, PrivacyModeAllowedObjectId> bat;

    public InvestorPrivacyModeProvider(DmxmlContext context) {
        super(ShellMMType.ST_INHABER);
        this.bat = new BlockAndTalker<>(context, PrivacyModeAllowedObjectId.Talker.createForInvestor());
        // privacy mode flag must be set, because when this controller is bat is requested, the privacy mode has not
        // yet been activated.
        this.bat.setPrivacyModeActive(true);
    }

    @Override
    protected HistoryToken createDepotObjectToken() {
        return InvestorPortraitController.createInvestorToken(this.bat.getDatabaseId());
    }

    @Override
    public Set<String> getObjectIdsAllowedInPrivacyMode() {
        if(!this.bat.getBlock().isResponseOk()) {
            return Collections.emptySet();
        }

        final List<PrivacyModeAllowedObjectId> pmids = this.bat.createResultObject();
        if(pmids == null || pmids.isEmpty()) {
            return Collections.emptySet();
        }

        final HashSet<String> result = new HashSet<>(pmids.size());
        for (PrivacyModeAllowedObjectId pmid : pmids) {
            if(pmid == null || !StringUtil.hasText(pmid.getId())) {
                continue;
            }
            result.add(pmid.getId());
        }
        return result;
    }

    public BlockAndTalker getBat() {
        return bat;
    }
}
