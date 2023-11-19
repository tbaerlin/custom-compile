package de.marketmaker.iview.mmgwt.mmweb.client.snippets;



import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.dmxml.MSCGisPages;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.DzPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

public class DzPageSnippet extends AbstractSnippet<DzPageSnippet, DzPageSnippetView> {

    private final DmxmlContext.Block<MSCGisPages> block;

    public static class Class extends SnippetClass {
        public Class() {
            super("DzPage", I18n.I.dzBankPages()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new DzPageSnippet(context, config);
        }

        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("pagenumber", DzPageController.getDefaultPageId()); // $NON-NLS$
            config.put("type", "dz"); // $NON-NLS$
        }
    }

    public DzPageSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        setView(new DzPageSnippetView(this, config));

        this.block = createBlock("MSC_GisPages"); // $NON-NLS-0$
        onParametersChanged();
    }

    public void configure(Widget triggerWidget) {
        final SnippetConfigurationView configView = new SnippetConfigurationView(this);
        configView.addConfigurePages();
        configView.show();
    }

    public void loadPage(String pagenumber) {
        getConfiguration().put("pagenumber", pagenumber); // $NON-NLS-0$
        onParametersChanged();
        this.contextController.reload();
    }

    @Override
    public void destroy() {
        destroyBlock(this.block);
    }

    @Override
    public void updateView() {
        if (block.isNotRequested()) {
            // the block is sent when setting the pagenumber
            return;
        }

        if (!block.isResponseOk()) {
            getView().showError();
            return;
        }
        final String pageContent = block.getResult().getPage();
        if (!StringUtil.hasText(pageContent)) {
            getView().showEmpty();
            return;
        }
        getView().showContent(pageContent);
    }

    @Override
    protected void onParametersChanged() {
        final SnippetConfiguration config = getConfiguration();
        this.block.setParameter("pagenumber", config.getString("pagenumber")); // $NON-NLS$
        this.block.setParameter("type", config.getString("type")); // $NON-NLS$
    }

    boolean useLocalLinks() {
        return "true".equals(getConfiguration().getString("localLinks", null)); // $NON-NLS$
    }

    @Override
    public void activate() {
        loadPage(getConfiguration().getString("pagenumber")); // $NON-NLS$
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

}
