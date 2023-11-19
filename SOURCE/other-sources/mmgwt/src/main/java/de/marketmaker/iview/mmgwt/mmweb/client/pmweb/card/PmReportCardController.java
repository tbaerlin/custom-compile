package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.card;

import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.AnalysisMetadataCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.AnalysisMetadataHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.InvestorItem;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.CloseCardEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.CreateCardEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.CreateCardHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.DisplayCardEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.LayoutDesc;
import de.marketmaker.iview.pmxml.ParameterDesc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author umaurer
 */
public class PmReportCardController implements CreateCardHandler {
    private static PmReportCardController instance = null;

    private ArrayList<Integer> listCardIds = new ArrayList<Integer>();
    private HashMap<Integer, PmReportCard> mapCards = new HashMap<Integer, PmReportCard>();
    private PmReportCard currentCard = null;
    private int maxCardId = 0;

    private HashMap<String, String> mapGlobalParameters = new HashMap<String, String>();


    public static PmReportCardController getInstance() {
        if (instance == null) {
            instance = new PmReportCardController();
        }
        return instance;
    }

    private PmReportCardController() {
        EventBusRegistry.get().addHandler(CreateCardEvent.getType(), this);
    }

    public void createCard(CreateCardEvent event) {
        final InvestorItem investor = event.getInvestor();
        final LayoutDesc reportBase = PmWebSupport.getInstance().getReportDefault(investor.getType());
        AnalysisMetadataHandler.getInstance().getMetadata(reportBase.getLayout().getGuid(), new AnalysisMetadataCallback() {
            public void onMetadataAvailable(LayoutDesc report, String jsonConfig) {
                createCard(investor, report);
            }
        });
    }

    private void createCard(InvestorItem investor, LayoutDesc report) {
        final PmReportCard card = new PmReportCard(++this.maxCardId, investor, report, this.mapGlobalParameters);
        addCard(card);
        gotoCard(card.getCardId());
    }

    private void addCard(PmReportCard card) {
        this.mapCards.put(card.getCardId(), card);
        this.listCardIds.add(card.getCardId());
    }

    public void gotoCard(int cardId) {
        gotoCard(this.mapCards.get(cardId));
    }

    public void gotoCard(PmReportCard card) {
        gotoCard(card.getCardId(), card.getInvestorItem(), card.getReport().getLayout().getGuid(), card.getMapParameters());
    }

    public void gotoCardWithReportId(final PmReportCard card, final String layoutGuid) {
        if (card.getReport().getLayout().getGuid().equals(layoutGuid)) {
            return;
        }
        AnalysisMetadataHandler.getInstance().getMetadata(layoutGuid, new AnalysisMetadataCallback() {
            public void onMetadataAvailable(LayoutDesc report, String jsonConfig) {
                gotoCard(card.getCardId(), card.getInvestorItem(), layoutGuid,
                        getReportParameters(report.getParameters(),  card.getMapParameters()));
            }
        });
    }

    private HashMap<String, String> getReportParameters(List<ParameterDesc> listReportParameters, Map<String, String> mapCardParameters) {
        final HashMap<String, String> mapParameters = new HashMap<String, String>(listReportParameters.size());
        for (ParameterDesc parameter : listReportParameters) {
            final String key = parameter.getName();
            final String value = mapCardParameters.get(key);
            if (value != null) {
                mapParameters.put(key, value);
            }
        }
        return mapParameters;
    }

    private void gotoCard(int cardId, InvestorItem investorItem, String layoutGuid, Map<String, String> mapParameters) {
        final StringBuilder sbToken = new StringBuilder();
        StringUtil.appendToken(sbToken, "PM_R"); // $NON-NLS$
        StringUtil.appendToken(sbToken, "cardId=" + cardId); // $NON-NLS$
        if (investorItem.isCached()) {
            StringUtil.appendToken(sbToken, "investorKey=" + investorItem.getKey()); // $NON-NLS$
        }
        else {
            StringUtil.appendToken(sbToken, "inputObject=" + // $NON-NLS$
                    investorItem.getId() + "|" +
                    investorItem.getType().toString() + "|" +
                    investorItem.getName() + "|" +
                    investorItem.getZone());
        }
        StringUtil.appendToken(sbToken, "layoutGuid=" + layoutGuid); // $NON-NLS$
        if (mapParameters != null) {
            for (Map.Entry<String, String> entry : mapParameters.entrySet()) {
                StringUtil.appendToken(sbToken, entry.getKey() + "=" + entry.getValue());
            }
        }
        PlaceUtil.goTo(sbToken.toString());
    }

    public void displayCard(int cardId, InvestorItem investorItem, LayoutDesc report, HashMap<String, String> mapParameters) {
        PmReportCard card = this.mapCards.get(cardId);
        if (card == null) {
            if (cardId > this.maxCardId) {
                this.maxCardId = cardId;
            }
            card = new PmReportCard(cardId, investorItem, report, mapParameters);
            addCard(card);
        }
        else {
            if (!card.getInvestorItem().getId().equals(investorItem.getId())) {
                throw new RuntimeException("invalid investor item for card " + cardId + ": " + card.getInvestorItem().getId() + " != " + investorItem.getId()); // $NON-NLS$
            }
            card.setReport(report);
            card.putMapParameters(mapParameters);
        }
        this.currentCard = card;
        DisplayCardEvent.fire(card);
    }

    public void closeCard(Integer cardId) {
        final PmReportCard card = this.mapCards.remove(cardId);
        final int deletedIndex = this.listCardIds.indexOf(cardId);
        this.listCardIds.remove(deletedIndex);
        CloseCardEvent.fire(card);
        if (this.currentCard == card) {
            final int newIndex = this.listCardIds.size() == deletedIndex ? (deletedIndex - 1) : deletedIndex;
            if (newIndex >= 0) {
                gotoCard(this.listCardIds.get(newIndex));
            }
        }
    }

    public void closeAllCards() {
        final HashSet<Integer> cardIds = new HashSet<Integer>(this.mapCards.keySet());
        for (Integer cardId : cardIds) {
            closeCard(cardId);
        }
    }

    public void putGlobalParameters(HashMap<String, String> mapParameters) {
        PmReportCard.putAll(this.mapGlobalParameters, mapParameters);
    }

    public String toString(){
        final StringBuilder sb = new StringBuilder();
        sb.append("PmReportCardController["); // $NON-NLS$
        boolean first = true;
        for (PmReportCard card : mapCards.values()) {
            if (first) {
                first = false;
            }
            else {
                sb.append(", ");
            }
            card.toString(sb);
        }
        sb.append("]");
        return sb.toString();
    }
}