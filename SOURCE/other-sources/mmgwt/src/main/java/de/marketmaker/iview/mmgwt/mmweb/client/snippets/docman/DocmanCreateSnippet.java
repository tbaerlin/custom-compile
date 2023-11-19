package de.marketmaker.iview.mmgwt.mmweb.client.snippets.docman;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.*;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.*;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.List;


public class DocmanCreateSnippet extends AbstractSnippet<DocmanCreateSnippet, FlowPanelView<DocmanCreateSnippet>> implements SymbolSnippet {

    public static class Class extends SnippetClass {
        public Class() {
            super("DocmanCreate"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new DocmanCreateSnippet(context, config);
        }
    }

    private final DmxmlContext docmanCtx;

    private final DmxmlContext.Block<FNDReports> fndReports;

    private final DmxmlContext.Block<DOCURL> docUrl;

    // maybe put these into config?!
    private String instrumentName;
    private String instrumentIsin;
    private String instrumentWkn;


    private DocmanCreateSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        config.put("title", I18n.I.docmanCreate());  // $NON-NLS$

        docmanCtx = new DmxmlContext();

        fndReports = docmanCtx.addBlock("FND_Reports"); // $NON-NLS-0$
        fndReports.setEnabled(false);

        docUrl = docmanCtx.addBlock("DOC_URL");   // $NON-NLS-0$
        docUrl.setParameter("absoluteUrl", "true");   // $NON-NLS$
        docUrl.setParameter("docType", "DOC_3RD");   // $NON-NLS$
        docUrl.setEnabled(false);

        setView(new FlowPanelView<DocmanCreateSnippet>(this));
    }

    @Override
    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        fndReports.setParameter("symbol", symbol); // $NON-NLS-0$
        fndReports.setEnabled(symbol != null);
        docmanCtx.issueRequest(new ResponseTypeCallback(){
            @Override
            protected void onResult() {
                updateView();
            }
        });
    }

    @Override
    public void destroy() {
        docmanCtx.removeBlock(fndReports);
        docmanCtx.removeBlock(docUrl);
    }

    @Override
    public void updateView() {
        getView().getWidget().clear();

        if (fndReports.isEnabled() && fndReports.isResponseOk()) {
            final FNDReports fndReportsResult = this.fndReports.getResult();
            this.instrumentName = fndReportsResult.getInstrumentdata().getName();
            this.instrumentIsin = fndReportsResult.getInstrumentdata().getIsin();
            this.instrumentWkn = fndReportsResult.getInstrumentdata().getWkn();
            setupContent();
        }
    }

    private void setupContent() {
        final FlowPanelView<DocmanCreateSnippet> view = getView();
        view.add(new Label(I18n.I.docmanCreateLabel()));
        final List<ReportType> reports = fndReports.getResult().getReport();
        for (ReportType report : reports) {
            final String url = report.getUrl();
            final String type = report.getType();
            if ("FactSheet".equals(type)) { //$NON-NLS$
                // do not show Monatsreport - mail Büssing 14.05.14
                continue;
            }
            String title = report.getTitle();
            if ("KIID".equals(type)) { //$NON-NLS$
                title += "<br/>(" + I18n.I.docmanKeyInvestorInfo() + ")"; //$NON-NLS$
            }
            final String date = report.getDate();
            view.add(new DownloadDocLinkItem(url, type, title, date));
        }
    }

    private class DownloadDocLinkItem extends HTML {

        DownloadDocLinkItem(final String url, final String type, String title, String date) {
            super("<table><tr>"  // $NON-NLS$
                    + "<td><div class=\"mm-desktopIcon-pdf\">&nbsp;</div></td>"  // $NON-NLS$
                    + "<td><u>" + title + "</u></td>"  // $NON-NLS$
                    + "</tr></table>", true);  // $NON-NLS$
            setStyleName("mm-desktopIcon");  // $NON-NLS$
            setPixelSize(220, 20);
            getElement().getStyle().setCursor(Style.Cursor.POINTER);
            setTitle(title + "(" + date + ")");
            addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    downloadArchivedDocument(url, type);
                }
            });
        }
    }

    private void downloadArchivedDocument(String url, String type) {
        fndReports.setEnabled(false);

        final AdhocParameter adhocParam = new AdhocParameterBuilder("adhocData")  // $NON-NLS$
                .addParameterItem("doc_type", type)  // $NON-NLS$
                .addParameterItem("instrument_name", instrumentName)  // $NON-NLS$
                .addParameterItem("instrument_wkn", instrumentWkn)  // $NON-NLS$
                .addParameterItem("instrument_isin", instrumentIsin)  // $NON-NLS$
                .addParameterItem("src_url", url)  // $NON-NLS$
                .build();

        docUrl.setParameter("symbol", instrumentIsin);  // $NON-NLS$
        docUrl.setParameter(adhocParam);
        docUrl.setEnabled(true);

        docmanCtx.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                if(docUrl.isResponseOk()) {
                    Window.open(docUrl.getResult().getRequest(), "_blank", "");  // $NON-NLS$
                }
                else {
                    Firebug.info("Error: " + docUrl.getError());
                }
                docUrl.setEnabled(false);
                refreshPeerSnippets();
            }
        });
    }

    private void refreshPeerSnippets() {
        // FIXME: this is hacky
        final String peerId = getConfiguration().getString("triggerUpdate"); // $NON-NLS$
        if (!StringUtil.hasText(peerId)) {
            return;
        }
        final Snippet peer = contextController.getSnippet(peerId);
        if (peer instanceof DocmanArchiveSnippet) {
            ((DocmanArchiveSnippet)peer).archiveChanged();
        }
    }

    public static class AdhocParameterBuilder {
        private final AdhocParameter adhocParameter= new AdhocParameter();

        public AdhocParameterBuilder(String key) {
            adhocParameter.setKey(key);
        }

        public AdhocParameterBuilder addParameterItem(String key, String value) {
            final AdhocParameterItem item = new AdhocParameterItem();
            item.setKey(key);
            item.setValue(value);
            adhocParameter.getItem().add(item);
            return this;
        }

        public AdhocParameter build() {
            return adhocParameter;
        }
    }
}
