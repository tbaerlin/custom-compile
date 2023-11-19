package de.marketmaker.iview.mmgwt.mmweb.client.dashboard;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.FlexTable;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DragDropSupport;

/**
 * Author: umaurer
 * Created: 15.06.15
 */
public class DropTargetSnippet extends AbstractSnippet<DropTargetSnippet, DropTargetSnippetView> {

    protected DropTargetSnippet() {
        super(null, new SnippetConfiguration("DropTarget")); // $NON-NLS$
        setView(new DropTargetSnippetView(this));
    }

    @Override
    public void destroy() {

    }

    @Override
    public void updateView() {

    }

    @Override
    public void onAddedToSnippetsView(FlexTable table, int row, int column) {
        final FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();
        formatter.addStyleName(row, column, DragDropSupport.CLASS_NAME_DROPPABLE);
        final Element element = formatter.getElement(row, column);
        element.setAttribute(DragDropSupport.ATT_DND_ROW, Integer.toString(row));
        element.setAttribute(DragDropSupport.ATT_DND_COLUMN, Integer.toString(column));
        element.setAttribute(DragDropSupport.ATT_DND_ACCEPT_PREFIX, DragDropSupport.TRANSFER_DATA_PREFIX_SNIPPET);
    }
}
