/*
 * LoadDepotsAndInvestorsMethod.java
 *
 * Created on 01.03.13 17:20
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSession;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.ImplementedOrderModules;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.IsBrokingAllowedFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search.SearchMethods;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Depot;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.BlockAndTalker;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.ShellObjectSearchResponse;
import de.marketmaker.iview.pmxml.ZoneDesc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Markus Dick
 */
final class LoadDepotsAndInvestorsMethod {
    public enum Type {STANDALONE, ACTIVITY}
    private enum State { DEPOTS, INVESTORS, FINISH }

    private final OrderSession orderSession;

    private final InvestorsCallback investorsCallback;
    private final DepotsCallback depotsCallback;

    private final Type type;

    private State nextState;

    private boolean privacyModeActive = false;

    LoadDepotsAndInvestorsMethod(OrderSession orderSession, Type type, boolean privacyModeActive, DepotsCallback depotsCallback) {
        this(orderSession, type, privacyModeActive, depotsCallback, null);
        this.nextState = State.DEPOTS;
    }

    LoadDepotsAndInvestorsMethod(OrderSession orderSession, Type type, boolean privacyModeActive, DepotsCallback depotsCallback, InvestorsCallback investorsCallback) {
        this.investorsCallback = investorsCallback;
        this.depotsCallback = depotsCallback;
        this.orderSession = orderSession;
        this.nextState = State.DEPOTS;
        this.type = type;
        this.privacyModeActive = privacyModeActive;
    }

    public void invoke() {
        execute();
    }

    private void execute() {
        switch(this.nextState) {
            case DEPOTS:
                this.nextState = State.INVESTORS;
                loadDepots();
                break;
            case INVESTORS:
                this.nextState = State.FINISH;
                loadInvestors();
                break;
            case FINISH:
            default:
                Firebug.debug("<LoadDepotsAndInvestorsMethod.execute> loading finished");
        }
    }

    private void loadInvestors() {
        if(this.investorsCallback == null) {
            execute();
            return;
        }

        SearchMethods.INSTANCE.depotObjectSearch("*", new AsyncCallback<ShellObjectSearchResponse>() { //$NON-NLS$
            @Override
            public void onSuccess(ShellObjectSearchResponse result) {
                onLoadInvestorsSuccess(result);
            }

            @Override
            public void onFailure(Throwable caught) {
                onLoadInvestorsFailed(caught);
            }
        });
    }

    private void onLoadInvestorsFailed(Throwable caught) {
        Firebug.warn("<LoadDepotsAndInvestorsMethod.loadInvestors> failed", caught);

        this.execute();
        this.investorsCallback.onLoadInvestorsFailed();
    }

    private void onLoadInvestorsSuccess(ShellObjectSearchResponse result) {
        final OrderSession os = this.orderSession;

        List<ShellMMInfo> depotObjects = result.getObjects();
        List<ShellMMInfo> investorObjects = new ArrayList<>();
        for (ShellMMInfo depotObject : depotObjects) {
            if (ShellMMType.ST_INHABER.equals(depotObject.getTyp())) {
                investorObjects.add(depotObject);
            }
        }

        Collections.sort(investorObjects, new SearchMethods.ShellMMInfoComparator(true));
        final Map<String, ZoneDesc> zoneDescMap = SearchMethods.INSTANCE.toMap(result.getZones());

        final String investorId = os.getOwner().getId();
        int index = -1;
        for (int i = 0; i < investorObjects.size(); i++) {
            if (investorId.equals(investorObjects.get(i).getId())) {
                index = i;
            }
        }

        this.execute();
        this.investorsCallback.onLoadInvestorsSuccess(investorObjects, zoneDescMap, index);
    }

    private void loadDepots() {
        if(this.depotsCallback == null) {
            execute();
            return;
        }

        final DmxmlContext context = new DmxmlContext();
        final BlockAndTalker<Depot.OnlyActiveDepotsTalker, List<Depot>, Depot> bat = new BlockAndTalker<>(context, new Depot.OnlyActiveDepotsTalker());
        final String id = orderSession.getOwner().getId();
        Firebug.debug("<LoadDepotsAndInvestorsMethod.loadDepots> investorId=" + id);
        bat.setDatabaseId(id);
        bat.setPrivacyModeActive(this.privacyModeActive);

        context.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onSuccess(ResponseType result) {
                onLoadDepotsSuccess(bat);
            }

            @Override
            public void onFailure(Throwable caught) {
                onLoadDepotsFailed(caught);
            }
        });
    }

    private void onLoadDepotsFailed(Throwable caught) {
        Firebug.warn("<LoadDepotsAndInvestorsMethod.loadDepots>", caught);

        execute();
        this.depotsCallback.onLoadDepotsFailed();
    }

    private void onLoadDepotsSuccess(BlockAndTalker<Depot.OnlyActiveDepotsTalker, List<Depot>, Depot> bat) {
        if(!bat.getBlock().isResponseOk()) {
            execute();
            this.depotsCallback.onLoadDepotsFailed();
        }

        final DmxmlContext context = new DmxmlContext();
        context.setCancellable(false);

        final ArrayList<IsBrokingAllowedFeature> features = new ArrayList<>();
        final HashMap<String, Depot> depotsById = new HashMap<>();

        for (Depot depot : bat.createResultObject()) {
            final IsBrokingAllowedFeature feature = new IsBrokingAllowedFeature(context);
            features.add(feature);

            final String depotId = depot.getId();
            feature.setId(depotId);
            depotsById.put(depotId, depot);
        }

        final String depotIdToSelect = this.orderSession.getSecurityAccount().getId();

        context.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                final ArrayList<Depot> allowedDepots = new ArrayList<>();
                for (IsBrokingAllowedFeature feature : features) {
                    switch (type) {
                        case ACTIVITY:
                            if(feature.isActivityBrokingAllowed()) {
                                allowedDepots.add(depotsById.get(feature.getDepotId()));
                            }
                            break;
                        case STANDALONE:
                        default:
                            if(feature.isStandaloneBrokingAllowed()) {
                                allowedDepots.add(depotsById.get(feature.getDepotId()));
                            }
                            break;
                    }
                }

                final List<Depot> filteredAllowedDepots = ImplementedOrderModules.filterDepots(allowedDepots);
                Depot selectedDepot = null;
                for (Depot depot : filteredAllowedDepots) {
                    if (depotIdToSelect.equals(depot.getId())) {
                        selectedDepot = depot;
                    }
                }

                execute();
                depotsCallback.onLoadDepotsSuccess(filteredAllowedDepots, selectedDepot);
            }
        });
    }

    public interface InvestorsCallback {
        void onLoadInvestorsSuccess(List<ShellMMInfo> investors, Map<String, ZoneDesc> zoneDescs, int index);
        void onLoadInvestorsFailed();
    }

    public interface DepotsCallback {
        void onLoadDepotsSuccess(List<Depot> depots, Depot itemToSelect);
        void onLoadDepotsFailed();
    }
}
