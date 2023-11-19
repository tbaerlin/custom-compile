package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.MetadataAware;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;

/**
 * @author umaurer
 */
public class PdfOptionSnippet extends AbstractSnippet<PdfOptionSnippet, PdfOptionSnippetView> implements SymbolSnippet, PdfUriSnippet, MetadataAware {
    public static class Class extends SnippetClass {
        public Class() {
            super("PdfOption"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new PdfOptionSnippet(context, config);
        }
    }

    private final PdfOptionHelper pdfOptionHelper;

    protected PdfOptionSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        final PdfOptionSnippetView view = new PdfOptionSnippetView(this);
        setView(view);
        final String linkFile = config.getString("linkFile", "stockportrait.pdf"); // $NON-NLS-0$ $NON-NLS-1$
        final String guidefOptionsId = config.getString("options"); // $NON-NLS-0$
        this.pdfOptionHelper = new PdfOptionHelper(view, new PdfOptionSpec(linkFile, null, guidefOptionsId), null);
        view.addLink(getPdfUri(), "mm-pdf-option-link", false); // $NON-NLS-0$
    }


    public boolean isMetadataNeeded() {
        return true;
    }

    public void onMetadataAvailable(MSCQuoteMetadata metadata) {
        this.pdfOptionHelper.setDisabled("convensys", !metadata.isConvensysIAvailable()); // $NON-NLS-0$
        this.pdfOptionHelper.setDisabled("convensysShares", !metadata.isConvensysIAvailable()); // $NON-NLS-0$
        this.pdfOptionHelper.setDisabled("screener", !metadata.isScreenerAvailable()); // $NON-NLS-0$
        this.pdfOptionHelper.setDisabled("estimates", !metadata.isEstimatesAvailable()); // $NON-NLS-0$
    }

    public void destroy() {
    }

    public void updateView() {
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.pdfOptionHelper.setOption("symbol", symbol); // $NON-NLS-0$
    }

    public void setOption(String id, Boolean value) {
        this.pdfOptionHelper.setOption(id, value);
    }

    public void setOption(String id, String value) {
        this.pdfOptionHelper.setOption(id, value);
    }

    public boolean isTopToolbarUri() {
        return true;
    }

    public String getPdfUri() {
        Firebug.log("no longer supported: PdfOptionSnippet.getPdfUri()"); // $NON-NLS-0$
        return this.pdfOptionHelper.getPdfUri();
    }

    public PdfOptionSpec getPdfOptionSpec() {
        return this.pdfOptionHelper.getPdfOptionSpec();
    }
}
