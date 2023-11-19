/*
 * SelectSymbolFormWithPmAvail.java
 *
 * Created on 21.02.13 15:10
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.ShellMMTypeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SelectSymbolForm;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SelectSymbolFormControllerInterface;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.Map;
import java.util.Set;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search.CombinedSearchElement.State;

/**
 * @author Markus Dick
 */
public class SelectPmSymbolForm extends SelectSymbolForm {
    public static SelectPmSymbolForm createDmWithOrderEntryAvail(Map<String, String> params, Set<ShellMMType> pmTypesAvailableForOrdering) {
        return new SelectPmSymbolForm(params, null, null, null, new SelectSymbolControllerWithPmAvail(pmTypesAvailableForOrdering), SnippetConfigurationView.SymbolParameterType.ISIN, true, new CombinedSearchElementStateIconRenderer());
    }

    public static SelectPmSymbolForm createPmWithOrderEntryAvail(Map<String, String> params, Set<ShellMMType> pmTypesAvailableForOrdering) {
        return createPmWithOrderEntryAvail(params,
                pmTypesAvailableForOrdering,
                SelectPmSymbolController.createControllerForOrderEntry(pmTypesAvailableForOrdering));
    }

    public static SelectPmSymbolForm createPmWithOrderEntryAvail(Map<String, String> params, Set<ShellMMType> pmTypesAvailableForOrdering,  SelectPmSymbolController controller) {
        // Filters securities for types allowed for order entry and applies also the availability check.
        // Setting the availability check for already filtered types is non-sense and is only set for clarity.
        return new SelectPmSymbolForm(params,
                ShellMMTypeUtil.toStringArray(pmTypesAvailableForOrdering),
                null,
                null,
                controller,
                SnippetConfigurationView.SymbolParameterType.ISIN,
                false,
                new CombinedSearchElementStateIconRenderer());
    }

    public static SelectPmSymbolForm createPm(Map<String, String> params, String[] types, SelectPmSymbolController controller) {
        return new SelectPmSymbolForm(params, types, null, null, controller,
                SnippetConfigurationView.SymbolParameterType.ISIN, false, new CombinedSearchElementStateIconRenderer());
    }

    protected SelectPmSymbolForm(Map<String, String> params,
                                 String[] types,
                                 final String filterForUnderlyingsForType,
                                 final Boolean filterForUnderlyingsOfLeveragProducts,
                                 final SelectSymbolFormControllerInterface c,
                                 SnippetConfigurationView.SymbolParameterType symbolParameterType,
                                 boolean showQuoteDataColumns,
                                 final TableCellRenderer availColumnRenderer) {

        super(params, types, filterForUnderlyingsForType, filterForUnderlyingsOfLeveragProducts, c, symbolParameterType, showQuoteDataColumns);

        TableColumnModel tableColumnModel = getTableColumnModel();
        TableColumn[] newTableColumns = new TableColumn[tableColumnModel.getColumnCount() + 2];

        for (int i = 0; i < tableColumnModel.getColumnCount(); i++) {
            newTableColumns[i] = tableColumnModel.getTableColumn(i);
        }

        TableColumn shellMMTypeColumn = new TableColumn(I18n.I.type(), 0).withVisibilityCheck(SimpleVisibilityCheck.valueOf(false));
        newTableColumns[tableColumnModel.getColumnCount()] = shellMMTypeColumn;

        TableColumn pmAvailColumn = new TableColumn("", 0.1f).withRenderer(availColumnRenderer);
        newTableColumns[tableColumnModel.getColumnCount() + 1] = pmAvailColumn;

        TableColumnModel newTableColumnModel = new DefaultTableColumnModel(newTableColumns);

        setTableColumnModel(newTableColumnModel);
    }

    public static class CombinedSearchElementStateIconRenderer implements TableCellRenderer {
        private final AbstractImagePrototype iconExclamationProto = IconImage.get("dialog-warning-16"); //$NON-NLS$
        private final AbstractImagePrototype iconQuestionProto = IconImage.get("dialog-question-16"); //$NON-NLS$
        private final AbstractImagePrototype iconCancelProto = IconImage.get("dialog-error-16"); //$NON-NLS$
        private final AbstractImagePrototype iconForbiddenProto = IconImage.get("dialog-forbidden-16"); //$NON-NLS$

        private final Image iconExclamation = iconExclamationProto.createImage();
        private final Image iconQuestion = iconQuestionProto.createImage();
        private final Image iconCancel = iconCancelProto.createImage();
        private final Image iconForbidden = iconForbiddenProto.createImage();

        public CombinedSearchElementStateIconRenderer() {
            this.iconQuestion.setAltText(I18n.I.orderEntryInstrumentAmbiguousIsinInPm());
            this.iconQuestion.setTitle(I18n.I.orderEntryInstrumentAmbiguousIsinInPm());

            this.iconExclamation.setAltText(I18n.I.orderEntryInstrumentNotAvailableForOrderEntry());
            this.iconExclamation.setTitle(I18n.I.orderEntryInstrumentNotAvailableForOrderEntry());

            this.iconCancel.setAltText(I18n.I.orderEntryInstrumentNotAvailableInPm());
            this.iconCancel.setTitle(I18n.I.orderEntryInstrumentNotAvailableInPm());

            this.iconForbidden.setAltText(I18n.I.orderEntryInstrumentNotAvailableDueToBusinessRules());
            this.iconForbidden.setTitle(I18n.I.orderEntryInstrumentNotAvailableDueToBusinessRules());
        }

        public void render(Object data, StringBuffer sb, Context context) {
            if (data == null || !(data instanceof State)) {
                return;
            }

            final State state = (State) data;
            switch (state) {
                case EMPTY_ISIN:
                    sb.append(this.iconExclamation);
                    return;
                case AMBIGUOUS_ISIN:
                    sb.append(this.iconQuestion);
                    return;
                case NOT_AVAILABLE:
                    sb.append(this.iconCancel);
                    return;
                case NO_ORDER_ENTRY_DUE_TO_BUSINESS_RULES:
                    sb.append(this.iconForbidden);
                case AVAILABLE:
                default:
            }
        }

        @Override
        public boolean isPushRenderer() {
            return false;
        }

        @Override
        public String getContentClass() {
            return null;
        }
    }
}
