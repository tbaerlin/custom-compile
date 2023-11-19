package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author umaurer
 */
public class JsonTreeSnippet extends AbstractSnippet<JsonTreeSnippet, JsonTreeView> {

    public static class Class extends SnippetClass {
        public Class() {
            super("JsonTree"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new JsonTreeSnippet(context, config);
        }
    }

    private final String jsonKey;

    private JsonListDetailsSnippet detailsSnippet = null;

    protected JsonTreeSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.jsonKey = config.getString("jsonKey"); // $NON-NLS-0$
        setView(new JsonTreeView(this));
    }

    @Override
    public void onControllerInitialized() {
        final String detailId = getConfiguration().getString("detailId", null); // $NON-NLS-0$
        if (detailId != null) {
            this.detailsSnippet = (JsonListDetailsSnippet) this.contextController.getSnippet(detailId);
            this.detailsSnippet.setInitialQuery(getView().getFirstNodeText(), getView().getFirstNodeData());
        }
    }

    public void destroy() {
        // nothing to do
    }

    public String getJsonKey() {
        return this.jsonKey;
    }

    public void updateView() {
        // empty
    }

    public void onSelect(String name, String query) {
        if (this.detailsSnippet != null) {
            this.detailsSnippet.setQuery(name, query);
        }

    }
}
