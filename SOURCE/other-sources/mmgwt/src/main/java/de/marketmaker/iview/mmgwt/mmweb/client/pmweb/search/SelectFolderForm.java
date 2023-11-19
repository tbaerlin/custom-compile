/*
 * SelectSymbolFormWithPmAvail.java
 *
 * Created on 21.02.13 15:10
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SelectSymbolForm;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SelectSymbolFormControllerInterface;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.VisibilityCheck;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.Map;

/**
 * @author Michael Lösch
 */
public class SelectFolderForm extends SelectSymbolForm {
    //TODO: This whole SelectSymbolForm abuse must be cleaned up sometimes!
    //TODO: Everytime something else than the "classical" symbol search is done, this actually doesn't fit
    public SelectFolderForm(Map<String, String> params, String[] types,
                               final SelectSymbolFormControllerInterface c) {

        super(params, types, null, null, c, SnippetConfigurationView.SymbolParameterType.ISIN, false);
    }

    protected DefaultTableColumnModel createTableColumnModel(boolean showQuoteDataColumns) {
        final TableCellRenderers.DelegateRenderer<ShellMMType> shellMMTypeRenderer =
                new TableCellRenderers.DelegateRenderer<>(PmRenderers.SHELL_MM_TYPE);
        final VisibilityCheck hide = SimpleVisibilityCheck.valueOf(false);

        //to keep it simple the useless fields are just set invisible

        return new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.option(), 0.05f).withRenderer(this.renderer)
                , new TableColumn(I18n.I.name(), 0.80f).withRenderer(new TableCellRenderers.QuoteNameRenderer(100, "-"))  // $NON-NLS-0$
                , new TableColumn("1", 0).withVisibilityCheck(hide) // $NON-NLS-0$
                , new TableColumn("2", 0).withVisibilityCheck(hide) // $NON-NLS-0$
                , new TableColumn("3", 0).withVisibilityCheck(hide) // $NON-NLS$
                , new TableColumn("4", 0).withVisibilityCheck(hide) // $NON-NLS$
                , new TableColumn(I18n.I.type(), 0.15f).withRenderer(shellMMTypeRenderer)
                , new TableColumn("5", 0).withVisibilityCheck(hide) // $NON-NLS$
        });
    }

    @Override
    protected String getSearchLabel() {
        return I18n.I.searchString() + "  ";
    }
}
