package de.marketmaker.iview.mmgwt.dmxmldocu.client.parameter;

import java.util.HashMap;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Ulrich Maurer
 *         Date: 25.07.12
 */
public class BlockOverviewPanel extends Composite {
    private final FlowPanel flowPanel = new FlowPanel();
    private String lastPrefix;
    private static final HashMap<String, String> MAP_PREFIXES = createPrefixMap();

    private static HashMap<String, String> createPrefixMap(){
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put("ALT", "Alerting"); // $NON-NLS$
        map.put("BND", "Bonds"); // $NON-NLS$
        map.put("CER", "Certificates"); // $NON-NLS$
        map.put("CUR", "Currencies"); // $NON-NLS$
        map.put("ED", "Econoday"); // $NON-NLS$
        map.put("FND", "Funds"); // $NON-NLS$
        map.put("FUT", "Futures"); // $NON-NLS$
        map.put("IMG", "Images"); // $NON-NLS$
        map.put("IND", "Indices"); // $NON-NLS$
        map.put("INT", "vwd Internal"); // $NON-NLS$
        map.put("MER", "Macroeconomic Resources"); // $NON-NLS$
        map.put("MSC", "Miscellaneous"); // $NON-NLS$
        map.put("NWS", "News"); // $NON-NLS$
        map.put("OPT", "Options"); // $NON-NLS$
        map.put("PF", "Portfolio"); // $NON-NLS$
        map.put("RSC", "Risk Service"); // $NON-NLS$
        map.put("STK", "Stocks"); // $NON-NLS$
        map.put("WL", "Watchlist"); // $NON-NLS$
        map.put("WM", "WM Data"); // $NON-NLS$
        map.put("WNT", "Warrants"); // $NON-NLS$
        return map;
    }


    public BlockOverviewPanel() {
        this.flowPanel.setStyleName("blockOverviewPanel");
        initWidget(this.flowPanel);
    }

    public void add(final String blockName) {
        final String prefix = getPrefix(blockName);
        if (!prefix.equals(this.lastPrefix)) {
            final Label lblPrefix = new Label(prefix);
            lblPrefix.setStyleName("blockOverviewPrefix");
            this.flowPanel.add(lblPrefix);
            this.lastPrefix = prefix;
        }
        final Label label = new Label(blockName);
        label.setStyleName("blockOverviewLabel");
        label.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                History.newItem(blockName);
            }
        });
        this.flowPanel.add(label);
    }

    private String getPrefix(String blockName) {
        final int pos = blockName.indexOf('_');
        if (pos == -1) {
            return "No Prefix"; // $NON-NLS$
        }
        final String prefix = blockName.substring(0, pos);
        final String description = MAP_PREFIXES.get(prefix);
        return description == null ? prefix : (prefix + " - " + description);
    }
}
