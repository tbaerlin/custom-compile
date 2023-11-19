package de.marketmaker.iview.mmgwt.mmweb.client.dashboard;

import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.TableUtil;

import java.util.List;

/**
 * Author: umaurer
 * Created: 20.07.15
 */
public class SpanConfigPopup {
    public static final int ROW_COUNT = 3;
    public static final int COLUMN_COUNT = 3;

    private final PopupPanel popupPanel;
    private final FlexTable table;
    private final SpanConfigHandler handler;
    private TableCellElement lastCell;
    private boolean[][] freeCells;
    private int rowSpan;
    private int colSpan;
    private final Label label = new Label();

    public interface SpanConfigHandler {
        void onSpan(int rowSpan, int colSpan);
    }

    public SpanConfigPopup(SpanConfigHandler handler) {
        this.handler = handler;
        this.popupPanel = new PopupPanel(true);
        this.popupPanel.setStyleName("mm-toolbar-popup");

        this.table = new FlexTable();
        this.table.setStyleName("mm-dashboard-spanConfig");
        this.table.setCellSpacing(0);
        this.table.setCellPadding(0);
        for (int row = 0; row < ROW_COUNT; row++) {
            for (int column = 0; column < COLUMN_COUNT; column++) {
                this.table.setHTML(row, column, "&nbsp;"); // $NON-NLS$
            }
        }
        this.table.addBitlessDomHandler(new MouseMoveHandler() {
            @Override
            public void onMouseMove(MouseMoveEvent event) {
                setSpanHover(TableUtil.getCellElement(table, event));
            }
        }, MouseMoveEvent.getType());
        this.table.addBitlessDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                setSpanHover(rowSpan, colSpan);
            }
        }, MouseOutEvent.getType());

        this.table.addBitlessDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                setSpan(TableUtil.getCellElement(table, event));
            }
        }, ClickEvent.getType());

        final FlowPanel panel = new FlowPanel();
        panel.add(this.table);
        panel.add(this.label);

        this.popupPanel.setWidget(panel);
    }

    public void show(Widget nearBy, int row, int mCol, int rowSpan, int colSpan, List<TableCellPos> freeCells) {
        this.rowSpan = rowSpan;
        this.colSpan = colSpan;
        setFreeCells(row, mCol, freeCells);
        setSpanHover(rowSpan, colSpan);
        this.popupPanel.showRelativeTo(nearBy);
    }

    private void setFreeCells(int row, int mCol, List<TableCellPos> freeCells) {
        this.freeCells = new boolean[ROW_COUNT][COLUMN_COUNT];
        final FlexTable.FlexCellFormatter formatter = this.table.getFlexCellFormatter();
        for (TableCellPos freeCell : freeCells) {
            final int r = freeCell.getRow() - row;
            final int c = freeCell.getMCol() - mCol;
            formatter.addStyleName(r, c, "mm-drop-accepted");
            this.freeCells[r][c] = true;
        }
    }

    private boolean isFree(int rowSpan, int colSpan) {
        for (int r = 0; r < rowSpan; r++) {
            for (int c = 0; c < colSpan; c++) {
                if (!this.freeCells[r][c]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void setSpanHover(TableCellElement cellElement) {
        if (cellElement == this.lastCell) {
            return;
        }
        this.lastCell = cellElement;

        if (cellElement == null) {
            setSpanHover(this.rowSpan, this.colSpan);
        }
        else {
            final int rowSpan = TableUtil.getRowElement(cellElement).getSectionRowIndex() + 1;
            final int colSpan = cellElement.getCellIndex() + 1;
            if (isFree(rowSpan, colSpan)) {
                setSpanHover(rowSpan, colSpan);
            }
        }
    }

    private void setSpanHover(int rowSpan, int colSpan) {
        final FlexTable.FlexCellFormatter formatter = this.table.getFlexCellFormatter();
        for (int row = 0; row < rowSpan; row++) {
            for (int column = 0; column < colSpan; column++) {
                formatter.addStyleName(row, column, "span");
            }
            for (int column = colSpan; column < COLUMN_COUNT; column++) {
                formatter.removeStyleName(row, column, "span");
            }
        }
        for (int row = rowSpan; row < ROW_COUNT; row++) {
            for (int column = 0; column < COLUMN_COUNT; column++) {
                formatter.removeStyleName(row, column, "span");
            }
        }
        this.label.setText(colSpan + "x" + rowSpan); // $NON-NLS$
    }

    private void setSpan(TableCellElement cellElement) {
        if (cellElement == null) {
            return;
        }
        final int rowSpan = TableUtil.getRowElement(cellElement).getSectionRowIndex() + 1;
        final int colSpan = cellElement.getCellIndex() + 1;
        if (isFree(rowSpan, colSpan)) {
            this.popupPanel.hide();
            this.handler.onSpan(rowSpan, colSpan);
        }
    }
}
