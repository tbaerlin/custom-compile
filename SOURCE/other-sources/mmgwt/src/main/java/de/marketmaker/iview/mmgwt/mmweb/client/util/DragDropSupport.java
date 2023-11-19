package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DragDropEventBase;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.CssUtil;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.StringUtility;
import de.marketmaker.itools.gwtutil.client.widgets.TableUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.SnippetDropHandler;

/**
 * Author: umaurer
 * Created: 08.06.15
 */
public class DragDropSupport {
    public static final String TRANSFER_DATA_FORMAT = "text"; // IE9 only supports "text" and "URL" // $NON-NLS$

    public static final String TRANSFER_DATA_PREFIX_SNIPPET = "snippet:"; // $NON-NLS$

    public static final String CLASS_NAME_DROPPABLE = "mm-droppable"; // $NON-NLS$

    @SuppressWarnings("unused")
    public static final String CLASS_NAME_DROP_HOVER = "mm-dropHover"; // $NON-NLS$

    public static final String ATT_DND_ROW = "mm:dnd-row"; // $NON-NLS$

    public static final String ATT_DND_COLUMN = "mm:dnd-col"; // $NON-NLS$

    public static final String ATT_DND_ACCEPT_PREFIX = "mm:dnd-accept-prefix"; // $NON-NLS$

    private static String currentTransferData = null;

    public static HandlerRegistration makeDraggable(final Widget widget, final Widget dragHandle,
            final String transferData, final String logId) {
        widget.getElement().setDraggable(Element.DRAGGABLE_TRUE);

        final HandlerListRegistration hlr = new HandlerListRegistration();
        hlr.add(widget.addBitlessDomHandler(event -> {
            if (dragHandle == null) {
                return;
            }
            final Element dragHandleElement = dragHandle.getElement();
            final Element eventElement = Element.as(event.getNativeEvent().getEventTarget());
            if (de.marketmaker.itools.gwtutil.client.util.DOMUtil.isOrHasDescendant(dragHandleElement, eventElement)) {
                Firebug.log("DragDropSupport <onMouseDown> " + logId + " - click on handle -> headerDrag = true");
                widget.getElement().setAttribute("headerDrag", "true"); // $NON-NLS$
            }
            else {
                Firebug.log("DragDropSupport <onMouseDown> " + logId + " - click outside handle -> headerDrag = false");
                widget.getElement().setAttribute("headerDrag", "false"); // $NON-NLS$
            }
        }, MouseDownEvent.getType()));

        if (dragDropForIeNeeded(widget.getElement())) {
            new Ie9DraggableMouseHandler(hlr, widget, dragHandle);
        }

        hlr.add(widget.addBitlessDomHandler(event -> {
            if (dragHandle == null || "true".equals(widget.getElement().getAttribute("headerDrag"))) { // $NON-NLS$
                Firebug.log("DragDropSupport <onDragStart> " + logId);
                event.setData(TRANSFER_DATA_FORMAT, transferData);
                currentTransferData = transferData;
            }
            else {
                Firebug.log("DragDropSupport <onDragStart> " + logId + " - no header drag -> preventDefault()");
                event.preventDefault();
            }
        }, DragStartEvent.getType()));
        hlr.add(widget.addBitlessDomHandler(event -> {
            Firebug.log("DragDropSupport <onDragEnd> " + logId);
            currentTransferData = null;
        }, DragEndEvent.getType()));

        return hlr;
    }

    static class Ie9DraggableMouseHandler implements MouseDownHandler, MouseMoveHandler,
            MouseUpHandler {
        private boolean leftButtonPressed = false;

        private final Widget widget;

        private final Widget dragHandle;

        Ie9DraggableMouseHandler(HandlerListRegistration hlr, Widget widget, Widget dragHandle) {
            this.widget = widget;
            this.dragHandle = dragHandle;
            hlr.add(widget.addBitlessDomHandler(this, MouseDownEvent.getType()));
            hlr.add(widget.addBitlessDomHandler(this, MouseMoveEvent.getType()));
            hlr.add(widget.addBitlessDomHandler(this, MouseUpEvent.getType()));
        }

        @Override
        public void onMouseDown(MouseDownEvent event) {
            this.leftButtonPressed = event.getNativeButton() == NativeEvent.BUTTON_LEFT;
        }

        @Override
        public void onMouseMove(MouseMoveEvent event) {
            if (this.dragHandle == null || "true".equals(this.widget.getElement().getAttribute("headerDrag"))) { // $NON-NLS$
                if (this.leftButtonPressed) {
                    dragDrop(this.widget.getElement());
                }
            }
            this.leftButtonPressed = false;

        }

        @Override
        public void onMouseUp(MouseUpEvent event) {
            this.leftButtonPressed = false;
        }
    }

    private static boolean dragDropForIeNeeded(Element element) {
        return Window.Navigator.getUserAgent().contains("MSIE 9.") && dragDropAvailable(element); // $NON-NLS$
    }

    private static native boolean dragDropAvailable(Element element) /*-{
        return element.dragDrop;
    }-*/;

    private static native void dragDrop(Element element) /*-{
        element.dragDrop();
    }-*/;

    public static HandlerRegistration makeDroppable(FlexTable table,
            SnippetDropHandler dropHandler) {
        final HandlerListRegistration hlr = new HandlerListRegistration();
        new TableDropHandler(hlr, table, dropHandler);
        return hlr;
    }

    public static HandlerRegistration makeDroppable(Widget w, WidgetDropHandler dropHandler) {
        final HandlerListRegistration hlr = new HandlerListRegistration();
        new SimpleDropHandler(hlr, w, dropHandler);
        return hlr;
    }

    static class TableDropHandler implements DragEnterHandler, DragOverHandler, DragLeaveHandler,
            DropHandler {
        private Element dragOverCellElement = null;

        private final HTMLTable table;

        private final SnippetDropHandler snippetDropHandler;

        public TableDropHandler(HandlerListRegistration hlr, HTMLTable table,
                SnippetDropHandler dropHandler) {
            this.table = table;
            this.snippetDropHandler = dropHandler;
            hlr.add(table.addBitlessDomHandler(this, DragEnterEvent.getType()));
            hlr.add(table.addBitlessDomHandler(this, DragOverEvent.getType()));
            hlr.add(table.addBitlessDomHandler(this, DragLeaveEvent.getType()));
            hlr.add(table.addBitlessDomHandler(this, DropEvent.getType()));
        }

        @Override
        public void onDragEnter(DragEnterEvent event) {
            Firebug.debug("onDragEnter");
            onDropHover(TableUtil.getCellElement(this.table, event), event);
        }

        @Override
        public void onDragOver(DragOverEvent event) {
            onDropHover(TableUtil.getCellElement(this.table, event), event);
        }

        @Override
        public void onDragLeave(DragLeaveEvent event) {
            Firebug.debug("onDragLeave");
            onDropHover(null, event);
        }

        private void onDropHover(Element cellElement, DragDropEventBase event) {
            if (this.dragOverCellElement == cellElement) {
                return;
            }
            this.dragOverCellElement = cellElement;

            this.snippetDropHandler.onDndLeave();
            if (cellElement != null) {
                final String prefix = cellElement.getAttribute(ATT_DND_ACCEPT_PREFIX);
                if (StringUtility.isEmpty(prefix) || currentTransferData == null || !currentTransferData.startsWith(prefix)) {
                    return;
                }
                final int row = Integer.parseInt(cellElement.getAttribute(ATT_DND_ROW));
                final int column = Integer.parseInt(cellElement.getAttribute(ATT_DND_COLUMN));
                if (this.snippetDropHandler.onDndEnter(currentTransferData, row, column)) {
                    event.preventDefault();
                }
            }
        }

        @Override
        public void onDrop(DropEvent event) {
            onDropHover(null, event);
            event.preventDefault();
            final Element cellElement = TableUtil.getCellElement(this.table, event);
            if (cellElement != null && CssUtil.hasStyle(cellElement, CLASS_NAME_DROPPABLE)) {
                final String transferData = event.getData(TRANSFER_DATA_FORMAT);
                final String prefix = cellElement.getAttribute(ATT_DND_ACCEPT_PREFIX);
                if (transferData == null || !transferData.startsWith(prefix)) {
                    return;
                }
                final int row = Integer.parseInt(cellElement.getAttribute(ATT_DND_ROW));
                final int column = Integer.parseInt(cellElement.getAttribute(ATT_DND_COLUMN));
                if (this.snippetDropHandler.isDropAllowed(transferData, row, column)) {
                    this.snippetDropHandler.onDrop(transferData, row, column);
                }
            }
        }
    }

    public static class SimpleDropHandler implements DragEnterHandler, DragOverHandler,
            DragLeaveHandler, DropHandler {
        private Element dragOverElement = null;

        private final WidgetDropHandler dropHandler;

        private final Widget widget;

        public SimpleDropHandler(HandlerListRegistration hlr, Widget widget,
                WidgetDropHandler dropHandler) {
            this.widget = widget;
            this.dropHandler = dropHandler;
            hlr.add(widget.addBitlessDomHandler(this, DragEnterEvent.getType()));
            hlr.add(widget.addBitlessDomHandler(this, DragOverEvent.getType()));
            hlr.add(widget.addBitlessDomHandler(this, DragLeaveEvent.getType()));
            hlr.add(widget.addBitlessDomHandler(this, DropEvent.getType()));
        }

        @Override
        public void onDragEnter(DragEnterEvent event) {
            onDropHover(this.widget.getElement(), event);
        }

        @Override
        public void onDragOver(DragOverEvent event) {
            onDropHover(this.widget.getElement(), event);
        }

        @Override
        public void onDragLeave(DragLeaveEvent event) {
            onDropHover(null, event);
        }

        private void onDropHover(Element element, DragDropEventBase event) {
            if (this.dragOverElement == element) {
                return;
            }
            this.dragOverElement = element;

            this.dropHandler.onDndLeave();
            if (element != null) {
                /*final String prefix = element.getAttribute(ATT_DND_ACCEPT_PREFIX);*/
                if (/*StringUtility.isEmpty(prefix) || */currentTransferData == null/* || !currentTransferData.startsWith(prefix)*/) {
                    return;
                }
                if (this.dropHandler.onDndEnter(currentTransferData)) {
                    event.preventDefault();
                }
            }
        }

        @Override
        public void onDrop(DropEvent event) {
            onDropHover(null, event);
            event.preventDefault();
            /*final Element element = this.widget.getElement();*/
            /*if (element != null && CssUtil.hasStyle(element, CLASS_NAME_DROPPABLE)) {
                final String transferData = event.getData(TRANSFER_DATA_FORMAT);
                final String prefix = element.getAttribute(ATT_DND_ACCEPT_PREFIX);
                if (transferData == null || !transferData.startsWith(prefix)) {
                    return;
                }*/
            final String transferData = event.getData(TRANSFER_DATA_FORMAT);
            if (this.dropHandler.isDropAllowed(transferData)) {
                this.dropHandler.onDrop(transferData);
            }
           /* }*/
        }
    }
}
