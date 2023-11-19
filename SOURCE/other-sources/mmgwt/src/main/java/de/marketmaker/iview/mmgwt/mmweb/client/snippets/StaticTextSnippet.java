package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;


public class StaticTextSnippet
        extends AbstractSnippet<StaticTextSnippet, SnippetTextView<StaticTextSnippet>> {

    public static class Class extends SnippetClass {
        public Class() {
            super("StaticText"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new StaticTextSnippet(context, config);
        }
    }

    protected StaticTextSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        setView(new SnippetTextView<>(this));
    }

    @Override
    public void destroy() {
    }

    @Override
    public void updateView() {
        getView().setHtml("<div style=\"padding: 5px\">" + getConfiguration().getString("content") + "</div>"); // $NON-NLS$
    }

}
