/*
 * MultiFinderGroupsSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.dmxml.FinderGroupTable;
import de.marketmaker.iview.mmgwt.mmweb.client.MultiFinderGroupsController;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;

import java.util.ArrayList;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MultiFinderGroupsSnippet extends AbstractSnippet<MultiFinderGroupsSnippet, MultiFinderGroupsSnippetView> {
    protected final LinkListener<MultiFinderGroupsController.Cell> linkListener;

    public static class Class extends SnippetClass {
        public Class() {
            super("MultiFinderGroups"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new MultiFinderGroupsSnippet(context, config);
        }

        @Override
        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("colSpan", 3); // $NON-NLS$
        }
    }

    private DmxmlContext.Block<FinderGroupTable> block;

    private MultiFinderGroupsSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.setView(new MultiFinderGroupsSnippetView(this));
        this.block = createBlock("MSC_FinderGroups"); // $NON-NLS$
        this.block.setParameter("type", config.getString("type")); // $NON-NLS$
        this.block.setParameter("primaryField", config.getString("primaryField")); // $NON-NLS$
        this.block.setParameter("secondaryField", config.getString("secondaryField")); // $NON-NLS$
        this.block.setParameter("disablePaging", config.getString("disablePaging")); // $NON-NLS$
        this.block.setParameter("sortBy", config.getString("sortBy")); // $NON-NLS$
        this.block.setParameter("ascending", config.getString("ascending")); // $NON-NLS$

        this.linkListener = MultiFinderGroupsController.createLinkListener(
                this.block.getParameter("type"), // $NON-NLS$
                this.block.getParameter("primaryField"), // $NON-NLS$
                this.block.getParameter("secondaryField") // $NON-NLS$
        );
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public boolean isConfigurable() {
        return true;
    }

    public void updateView() {
        if (!this.block.isResponseOk()) {
            getView().update(DefaultTableDataModel.NULL, new ArrayList<String>());
            return;
        }

        final FinderGroupTable result = this.block.getResult();
        final TableDataModel tdm = MultiFinderGroupsController.toTableDataModel(result);
        getView().update(tdm, result.getColumn());
    }
}
