package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

/**
 * @author umaurer
 */
public interface JsonListDetailsSnippet extends FinderQuerySnippet{
    public void setList(String name, String jsonListKey);
    public void setInitialQuery(String name, String query);
}
