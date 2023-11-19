package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.card;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.InvestorItem;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.LayoutDesc;

import java.util.HashMap;
import java.util.Map;

/**
 * @author umaurer
 */
public class PmReportCard {
    private int cardId;
    private InvestorItem investorItem;
    private LayoutDesc report;
    private HashMap<String, String> mapParameters = new HashMap<String, String>();

    public PmReportCard(int cardId, InvestorItem investorItem, LayoutDesc report, HashMap<String, String> mapParameters) {
        this.cardId = cardId;
        this.investorItem = investorItem;
        this.report = report;
        putMapParameters(mapParameters);
    }

    public int getCardId() {
        return this.cardId;
    }

    public InvestorItem getInvestorItem() {
        return this.investorItem;
    }

    public LayoutDesc getReport() {
        return this.report;
    }

    public void setReport(LayoutDesc report) {
        this.report = report;
    }

    public HashMap<String, String> getMapParameters() {
        return this.mapParameters;
    }

    public void putMapParameters(HashMap<String, String> mapParameters) {
        putAll(this.mapParameters, mapParameters);
    }

    public static void putAll(HashMap<String, String> map, HashMap<String, String> mapParameters) {
        for (Map.Entry<String, String> entry : mapParameters.entrySet()) {
            final String value = entry.getValue();
            final String key = entry.getKey();
            if (value == null) {
                map.remove(key);
            }
            else {
                map.put(key, value);
            }
        }
    }

    public String getToken() {
        return getTokenWithLayoutGuid(this.report.getLayout().getGuid());
    }

    public String getTokenWithLayoutGuid(String layoutGuid) {
        final StringBuilder sbToken = new StringBuilder();
        StringUtil.appendToken(sbToken, "PM_R"); // $NON-NLS$
        StringUtil.appendToken(sbToken, "cardId=" + this.cardId); // $NON-NLS$
        if (this.investorItem.isCached()) {
            StringUtil.appendToken(sbToken, "investorKey=" + this.investorItem.getKey()); // $NON-NLS$
        }
        else {
            StringUtil.appendToken(sbToken, "inputObject=" + // $NON-NLS$
                    this.investorItem.getId() + "|" +
                    this.investorItem.getType().toString() + "|" +
                    this.investorItem.getName() + "|" +
                    this.investorItem.getZone());
        }
        StringUtil.appendToken(sbToken, "layoutGuid=" + layoutGuid); // $NON-NLS$
        for (Map.Entry<String, String> entry : this.mapParameters.entrySet()) {
            StringUtil.appendToken(sbToken, entry.getKey() + "=" + entry.getValue());
        }
        return sbToken.toString();
    }

    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    public StringBuilder toString(StringBuilder sb) {
        sb.append("PmReportCard[id="); // $NON-NLS$
        sb.append(this.cardId);
        sb.append(", investor="); // $NON-NLS$
        sb.append(this.investorItem.getName());
        sb.append(", report="); // $NON-NLS$
        sb.append(this.report.getLayout().getLayoutName());
        sb.append(", parameters["); // $NON-NLS$
        boolean first = true;
        for (Map.Entry<String, String> entry : this.mapParameters.entrySet()) {
            if (first) {
                first = false;
            }
            else {
                sb.append(", ");
            }
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
        }
        sb.append("]]");
        return sb;
    }
}
