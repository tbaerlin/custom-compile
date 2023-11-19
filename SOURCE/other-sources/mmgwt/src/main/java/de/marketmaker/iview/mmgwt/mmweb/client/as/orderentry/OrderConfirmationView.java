/*
 * OrderConfirmationView.java
 *
 * Created on 05.02.13 14:57
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.DialogButton;
import de.marketmaker.itools.gwtutil.client.widgets.DialogIfc;
import de.marketmaker.itools.gwtutil.client.widgets.ImageButton;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;

import java.util.List;

/**
 * @author Markus Dick
 */
public class OrderConfirmationView<P extends OrderConfirmationDisplay.Presenter> implements OrderConfirmationDisplay<P> {
    public static final String AS_OE_STYLE = "as-oe"; //$NON-NLS$
    public static final String CONFIRM_DIALOG_STYLE = "confirmDlg"; //$NON-NLS$
    public static final String HEADLINE_STYLE = "headline";//$NON-NLS$
    public static final String CP_STYLE = "cp";//$NON-NLS$
    public static final String FLEX_TABLE_STYLE = "ft";//$NON-NLS$
    public static final String RIGHT_COLS_STYLE = "rightcols";//$NON-NLS$
    public static final String LABEL_STYLE = "labels";//$NON-NLS$

    private P presenter;

    private List<Section> sections;
    private int columns;

    private final DialogIfc dialog;
    private final FlexTable table;
    private DialogButton executeButton;
    private DialogButton cancelButton;
    private DialogButton backButton;
    private final FloatingToolbar toolbar;
    private boolean printDateVisible;
    private MmJsDate printDate;
    private String title;
    private boolean showing = false;

    public OrderConfirmationView() {
        final int height = (int) (Window.getClientHeight() * .75);

        this.dialog = Dialog.getImpl().createDialog().withStyle("as-oe-dlg");  // $NON-NLS$

        final FlowPanel layout = new FlowPanel();
        layout.addStyleName(AS_OE_STYLE);
        layout.addStyleName(CONFIRM_DIALOG_STYLE);
        layout.setHeight(height + "px");  // $NON-NLS$

        this.table = new FlexTable();
        this.table.setStyleName(CP_STYLE);
        this.table.addStyleName(FLEX_TABLE_STYLE);
        layout.add(this.table);
        this.dialog.withWidget(layout);

        initButtons();

        this.toolbar = new FloatingToolbar(FloatingToolbar.ToolbarHeight.FOR_ICON_SIZE_S);
        final ImageButton printButton = new ImageButton(IconImage.get("as-tool-print").createImage(), null, null); //$NON-NLS$
        printButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                OrderConfirmationView.this.presenter.onPrintClicked();
            }
        });
        this.toolbar.add(printButton);
        this.dialog.withTopWidget(this.toolbar);
    }

    protected void initButtons() {
        this.executeButton = this.dialog.addButton(I18n.I.ok(), new Command() {
            @Override
            public void execute() {
                OrderConfirmationView.this.dialog.keepOpen();
                OrderConfirmationView.this.presenter.onExecuteClicked();
            }
        });

        this.backButton = this.dialog.addButton(I18n.I.back(), new Command() {
            @Override
            public void execute() {
                OrderConfirmationView.this.dialog.keepOpen();
                OrderConfirmationView.this.presenter.onBackClicked();
            }
        });

        final Command cancelCommand = new Command() {
            @Override
            public void execute() {
                OrderConfirmationView.this.dialog.keepOpen();
                OrderConfirmationView.this.presenter.onCancelClicked();
            }
        };
        this.cancelButton = this.dialog.addButton(I18n.I.cancel(), cancelCommand);

        this.dialog.withEscapeCommand(cancelCommand);
    }

    @Override
    public void setPresenter(P presenter) {
        this.presenter = presenter;
    }

    protected P getPresenter() {
        return presenter;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
        this.dialog.withTitle(title);
    }

    protected FloatingToolbar getToolbar() {
        return this.toolbar;
    }

    protected DialogIfc getDialogIfc() {
        return this.dialog;
    }

    @Override
    public void show() {
        updateView();
        if(!this.showing) {
            this.dialog.show();
            this.showing = true;
        }
    }

    @Override
    public void hide() {
        this.dialog.closePopup();
        this.showing = false;
    }

    @Override
    public void setExecuteButtonText(String text) {
        this.executeButton.setText(text);
    }

    @Override
    public void setExecuteButtonVisible(boolean visible) {
        this.executeButton.setVisible(visible);
    }

    @Override
    public void setCancelButtonText(String text) {
        this.cancelButton.setText(text);
    }

    @Override
    public void setCancelButtonVisible(boolean visible) {
        this.cancelButton.setVisible(visible);
    }

    @Override
    public void setBackButtonText(String text) {
        this.backButton.setText(text);
    }

    @Override
    public void setBackButtonVisible(boolean visible) {
        this.backButton.setVisible(visible);
    }

    @Override
    public void setColumns(int count) {
        this.columns = count;
    }

    @Override
    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    @Override
    public void setPrintDate(MmJsDate date) {
        this.printDate = date;
    }

    @Override
    public void setPrintDateVisible(boolean printDateVisisble) {
        this.printDateVisible = printDateVisisble;
    }

    @Override
    public String getPrintHtml() {
        SafeHtmlBuilder sb = new SafeHtmlBuilder();

        sb.appendHtmlConstant("<div class=\"as-oe\"><div>"); //$NON-NLS$

        if(StringUtil.hasText(this.title)) {
            sb.appendHtmlConstant("<h1>");  //$NON-NLS$
            sb.appendEscaped(this.title);
            sb.appendHtmlConstant("</h1>"); //$NON-NLS$
        }

        //remove style to allow auto size of table in  print window!
        final TableElement te = this.table.getElement().cloneNode(true).cast();
        final DivElement de = DOM.createDiv().cast();
        te.setAttribute("style", "");  //$NON-NLS$
        de.insertFirst(te);
        sb.appendHtmlConstant(de.getInnerHTML());

        if(this.printDateVisible && this.printDate != null) {
            sb.appendHtmlConstant("<div class=\"printDate\">"); //$NON-NLS$
            sb.appendEscaped(I18n.I.printDate());
            sb.appendEscaped(" "); //$NON-NLS$
            sb.appendEscaped(JsDateFormatter.formatDdmmyyyyHhmmss(this.printDate));
            sb.appendHtmlConstant("</div>"); //$NON-NLS$
        }

        sb.appendHtmlConstant("</div></div>"); //$NON-NLS$

        return sb.toSafeHtml().asString();
    }

    @Override
    public boolean isShowing() {
        return this.showing;
    }

    private void updateView() {
        final FlexTable t = this.table;
        final FlexTable.FlexCellFormatter f = t.getFlexCellFormatter();

        final int sectionsSize = this.sections.size();

        int row = 0;
        int startPos = 0;

        while(startPos < sectionsSize) {
            int count = getSectionsForRow(startPos);
//            Firebug.debug("getSectionsForRow startPos=" + startPos + " return count=" + count);
            if(count == 0) break;
            int maxRows = getMaxRows(startPos, count);
//            Firebug.debug("getMaxRows startPos=" + startPos + " count=" + count + " return maxRows=" + maxRows);

            int nextCol = 0;

            List<Section> sectionsForColumns = this.sections.subList(startPos, startPos+count);

//            DebugUtil.logToFirebugConsole("sectionsForColumns.size=" + sectionsForColumns.size());

            for(Section section : sectionsForColumns) {
//                DebugUtil.logToFirebugConsole("HEADLINE row=" + row + " col=" + nextCol + " colspan=" + section.getColumnSpan() * 2 + " text=" + section.getHeadline());

                t.setText(row, nextCol, section.getHeadline());
                f.setColSpan(row, nextCol, normalizeColumnSpan(section.getColumnSpan()) * 2);
                f.setStyleName(row, nextCol, HEADLINE_STYLE);
                nextCol++;
            }
            row++;
            nextCol = 0;

            int entryIndex = 0;
            while(entryIndex < maxRows) {
                for(Section section : sectionsForColumns) {
                    final int normalizedSectionColumnSpan = normalizeColumnSpan(section.getColumnSpan());

                    if(entryIndex < section.getEntries().size()) {
                        final SimpleEntry simpleEntry = section.getEntries().get(entryIndex);
                        if(simpleEntry instanceof Entry) {
                            final int labelColumnSpan = 1;
                            final int valueColumnSpan;
                            if(normalizedSectionColumnSpan > 1) {
                                valueColumnSpan = (normalizedSectionColumnSpan * 2) - 1;
                            }
                            else {
                                valueColumnSpan = 1;
                            }

                            final Entry entry = (Entry)simpleEntry;

                            t.setText(row, nextCol, renderLabel(entry));
                            f.setColSpan(row, nextCol, labelColumnSpan);
//                            DebugUtil.logToFirebugConsole("ENTRY LABEL row=" + row + " col=" + nextCol + " colspan=" + section.getColumnSpan() + " text=" + entry.getLabel());

                            f.setStyleName(row, nextCol, LABEL_STYLE);
                            if(nextCol > 0) {
                                f.addStyleName(row, nextCol, RIGHT_COLS_STYLE);
                            }
                            nextCol++;

                            t.setText(row, nextCol, entry.getValue());
//                            DebugUtil.logToFirebugConsole("ENTRY VALUE row=" + row + " col=" + nextCol + " colspan=" + section.getColumnSpan() + " text=" + entry.getValue());

                            f.setColSpan(row, nextCol, valueColumnSpan);
                        }
                        else {
//                            DebugUtil.logToFirebugConsole("SIMPLE ENTRY VALUE row=" + row + " col=" + nextCol + " colspan=" + section.getColumnSpan() * 2 + " text=" + simpleEntry.getValue());

                            if(simpleEntry instanceof WidgetEntry) {
                                t.setWidget(row, nextCol, ((WidgetEntry) simpleEntry).getWidget());
                                f.setColSpan(row, nextCol, normalizedSectionColumnSpan * 2);
                            }
                            else {
                                t.setText(row, nextCol, simpleEntry.getValue());
                                f.setColSpan(row, nextCol, normalizedSectionColumnSpan * 2);

                                f.setStyleName(row, nextCol, LABEL_STYLE);
                                if(nextCol > 0) {
                                    f.addStyleName(row, nextCol, RIGHT_COLS_STYLE);
                                }
                            }
                        }
                        nextCol++;
                    }
                    else {
                        nextCol += normalizedSectionColumnSpan * 2;
                    }
                }
                nextCol = 0;
                entryIndex++;
                row++;
            }
            startPos += count;
        }
    }

    private String renderLabel(Entry entry) {
        return entry.getLabel();
    }

    private int getMaxRows(int startPos, int count) {
//        Firebug.debug("getMaxRows startPos=" + startPos + " count" + count);
        int maxRows = 0;
        for(int i = startPos; i < startPos + count; i++) {
//            Firebug.debug("getMaxRows i=" + i);
            final Section s = this.sections.get(i);
            int size = s.getEntries().size();
            if(size > maxRows) {
                maxRows = size;
            }
        }
        return maxRows;
    }

    private int getSectionsForRow(int startPos) {
        int sum = 0;
        int sectionCount = 0;

        for(int i = startPos; i < this.sections.size(); i++, sectionCount++) {
            int columnSpan = normalizeColumnSpan(this.sections.get(i).getColumnSpan());
            sum += columnSpan;
            if(sum > this.columns) {
                break;
            }
        }

        return sectionCount;
    }

    private int normalizeColumnSpan(int columnSpan) {
//        Firebug.debug("normalizeColumnSpan columnSpan=" + columnSpan + " this.columns=" + this.columns);
        if(columnSpan > this.columns) {
            return this.columns;
        }
//        Firebug.debug("normalizeColumnSpan return columnSpan=" + columnSpan);

        return columnSpan;
    }
}
