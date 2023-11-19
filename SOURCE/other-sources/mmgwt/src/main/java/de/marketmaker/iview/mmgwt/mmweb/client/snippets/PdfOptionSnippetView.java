package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;

import java.util.Map;
import java.util.HashMap;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.PdfOptionView;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author umaurer
 */
public class PdfOptionSnippetView extends SnippetView<PdfOptionSnippet> implements PdfOptionView {
    private final FlexTable table = new FlexTable();
    private final FlexTable.FlexCellFormatter cellFormatter = this.table.getFlexCellFormatter();
    private Anchor anchor;
    private final Map<String, CheckBox> mapCheckBoxes = new HashMap<>();

    public PdfOptionSnippetView(PdfOptionSnippet snippet) {
        super(snippet);
        this.table.setStyleName("mm-pdf-option"); // $NON-NLS-0$
        this.table.setHTML(0, 0, I18n.I.pdfPortrait()); 
        this.table.setHTML(1, 0, I18n.I.pdfPortraitMessageSelectOptions()); 
        this.table.setHTML(2, 0, "&nbsp;"); // $NON-NLS-0$
        this.cellFormatter.setStyleName(0, 0, "external-tool-header"); // $NON-NLS-0$
        this.cellFormatter.setStyleName(1, 0, "external-tool-text"); // $NON-NLS-0$
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setContentWidget(this.table);
    }

    @Override
    public void addOption(final String id, String title, boolean checked, String style) {
        final int row = this.table.getRowCount();
        final CheckBox checkBox = new CheckBox(title);
        this.table.setWidget(row, 0, checkBox);
        this.cellFormatter.setStyleName(row, 0, style);
        checkBox.setValue(checked);
        checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>(){
            public void onValueChange(ValueChangeEvent<Boolean> booleanValueChangeEvent) {
                snippet.setOption(id, booleanValueChangeEvent.getValue());
            }
        });
        this.mapCheckBoxes.put(id, checkBox);
    }

    @Override
    public void addOption(final String id, final String title, final String[] values, final boolean checked, final String style) {
        assert values.length == 2;
        final int row = this.table.getRowCount();
        final CheckBox checkBox = new CheckBox(title);
        this.table.setWidget(row, 0, checkBox);
        this.cellFormatter.setStyleName(row, 0, style);
        checkBox.setValue(checked);
        checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>(){
            public void onValueChange(ValueChangeEvent<Boolean> booleanValueChangeEvent) {
                snippet.setOption(id, booleanValueChangeEvent.getValue() ? values[0] : values[1]);
            }
        });
        this.mapCheckBoxes.put(id, checkBox);
    }

    void setEnabled(String id, boolean enabled) {
        final CheckBox checkBox = this.mapCheckBoxes.get(id);
        if (checkBox != null) {
            checkBox.setEnabled(enabled);
        }
    }

    @Override
    public void addLink(String link, String style, boolean httpPost) {
        int row = this.table.getRowCount();
        this.table.setHTML(row, 0, "&nbsp;"); // $NON-NLS-0$
        row++;
        this.anchor = new Anchor(I18n.I.openPdfPortrait(), false, link, "_blank");  // $NON-NLS-0$
        this.table.setWidget(row, 0, this.anchor);
        if (style != null) {
            this.cellFormatter.setStyleName(row, 0, style);
        }
    }

    @Override
    public void updateLink(String link, boolean httpPost) {
        if (this.anchor == null) {
            Firebug.log("Fehler: PdfOptionSnippetView.anchor not initialized"); // $NON-NLS-0$
            DebugUtil.logToServer("Fehler: PdfOptionSnippetView.anchor not initialized"); // $NON-NLS-0$
        }
        this.anchor.setHref(link);
    }
}
