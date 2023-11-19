package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.InvestorItem;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView;

/**
 * @author Ulrich Maurer
 *         Date: 05.04.11
 */
public class PmSignalsSnippetView extends SnippetView<PmSignalsSnippet> {
    public static class Signal {
        public static final Signal[] SIGNALS = new Signal[]{
                new Signal("Asset Allocation", "heute", InvestorItem.Type.Inhaber, "Musterkunde 1", "Aktien-Obergrenze überschritten (Soll: 30-40%)"),      // $NON-NLS$
                new Signal("Asset Allocation", "04.04.2011", InvestorItem.Type.Inhaber, "Müller (AF3), Ralf", "Liquidität &gt; 10%"),                            // $NON-NLS$
                new Signal("Verlustschwelle", "02.04.2011", InvestorItem.Type.Portfolio, "Musterportfolio Konservativ", "Schwelle bei -12% erreicht, Grenze bei -10%"), // $NON-NLS$
                new Signal("Restriktion", "01.04.2011", InvestorItem.Type.Inhaber, "Burgmaier", "keine Stromkonzerne"),                                 // $NON-NLS$
                new Signal("Konto", "28.03.2011", InvestorItem.Type.Inhaber, "Lautberger", "negativer Kontostand"),                                      // $NON-NLS$
                new Signal("Kampagne", "24.03.2011", InvestorItem.Type.Inhaber, "Privatkunde A", "hat Fonds \"Japan Total Return\" im Portfolio")           // $NON-NLS$
        };

        private final String type;
        private final String date;
        private final InvestorItem.Type investorType;
        private final String investorName;
        private final String description;

        public Signal(String type, String date, InvestorItem.Type investorType, String investorName, String description) {
            this.type = type;
            this.date = date;
            this.investorType = investorType;
            this.investorName = investorName;
            this.description = description;
        }

        public SafeHtml toHtml() {
            return new SafeHtmlBuilder()
                    .appendHtmlConstant("<div style=\"float: right; margin-left: 4px;\">").appendEscaped(this.date).appendHtmlConstant("</div>") // $NON-NLS$
                    .appendEscaped(this.type)
                    .appendHtmlConstant("<div style=\"margin-left: 10px;\">" + this.investorType.getIcon().getHTML() + " ").appendEscaped(this.investorName).appendHtmlConstant("</div>") // $NON-NLS$
                    .appendHtmlConstant("<div style=\"margin-left: 10px; padding-bottom: 10px;\">").appendEscaped(description).appendHtmlConstant("</div>") // $NON-NLS$
                    .toSafeHtml();
        }
    }

    private final Panel panelSignals;

    protected PmSignalsSnippetView(PmSignalsSnippet snippet) {
        super(snippet);
        setTitle(I18n.I.pmSignals());
        this.panelSignals = getSignalsPanel();
    }

    public static Panel getSignalsPanel() {
        final FlowPanel panel = new FlowPanel();
        for (final Signal signal : Signal.SIGNALS) {
            panel.add(new HTML(signal.toHtml()));
        }
        return panel;
    }

    public static void addSignals(ContentPanel panel) {
        for (final Signal signal : Signal.SIGNALS) {
            panel.add(new HTML(signal.toHtml()));
        }
    }

    @Override
    protected void onContainerAvailable() {
        this.container.setContentWidget(this.panelSignals);
    }
}
