package de.marketmaker.iview.mmgwt.mmweb.client.view;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.widgets.Separator;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BrowserSpecific;

/**
 * @author Ulrich Maurer
 *         Date: 21.10.11
 */
public class FloatingToolbar extends WidgetComponent {
    public enum ToolbarHeight {
        FOR_ICON_SIZE_UNDEF(-1), // height not specified
        FOR_ICON_SIZE_S(20), //usually 16x16 px icons in mmf
        FOR_ICON_SIZE_M(28), //usually 24x24 px icons in mmf
        FOR_ICON_SIZE_L(52), //usually 48x48 px icons in mmf
        FOR_ICON_SIZE_XL(68) //usually 64x64 px icons in mmf
        ;
        private final int heightInPixel;

        ToolbarHeight(int heightInPixel) {
            this.heightInPixel = heightInPixel;
        }

        public int getHeightInPixel() {
            return heightInPixel;
        }
    }

    private static final int PADDING_AND_BORDER = 5;

    private final ToolbarHeight toolbarHeight;
    private static final boolean HIDDEN_BUTTONS = false;
    private final FlowPanel panelOuter = new FlowPanel();
    private final SimplePanel panelInner = new SimplePanel();

    private Button buttonLeft = new Button("&nbsp;"); // $NON-NLS$
    private Button buttonRight = new Button("&nbsp;"); // $NON-NLS$
    @SuppressWarnings({"PointlessBooleanExpression"})
    private boolean buttonsVisible = !HIDDEN_BUTTONS;
    private boolean buttonLeftVisible = false;
    private boolean buttonRightVisible = false;

    private final FloatingAnimation animation = new FloatingAnimation();

    private final FlexTable table = new FlexTable();
    private FlexTable.FlexCellFormatter cellFormatter = this.table.getFlexCellFormatter();
    private int lastCellId = 0;
    private List<Integer> listFillIds = new ArrayList<>(1);

    public FloatingToolbar() {
        this(ToolbarHeight.FOR_ICON_SIZE_UNDEF);
    }

    public FloatingToolbar(ToolbarHeight toolbarHeight) {
        this(toolbarHeight, new ResizeLayoutPanel());
    }

    private FloatingToolbar(ToolbarHeight toolbarHeight, ResizeLayoutPanel toolbarPanel) {
        super(toolbarPanel);
        this.toolbarHeight = toolbarHeight;
        if (toolbarHeight != ToolbarHeight.FOR_ICON_SIZE_UNDEF) {
            toolbarPanel.getElement().getStyle().setHeight(toolbarHeight.getHeightInPixel(), Style.Unit.PX);
        }
        this.table.setCellPadding(0);
        this.table.setCellSpacing(0);

        this.buttonLeft.addMouseOverHandler(event -> this.animation.startLeft(getFloatPosition()));
        this.buttonRight.addMouseOverHandler(event -> this.animation.startRight(getFloatPosition()));
        final MouseOutHandler buttonMouseOutHandler = event -> this.animation.stop();
        this.buttonLeft.addMouseOutHandler(buttonMouseOutHandler);
        this.buttonRight.addMouseOutHandler(buttonMouseOutHandler);

        this.panelOuter.add(this.panelInner);
        this.panelOuter.add(this.buttonLeft);
        this.panelOuter.add(this.buttonRight);

        this.panelOuter.addStyleName("mm-ftoolbar-outer");
        this.panelInner.addStyleName("mm-ftoolbar-inner");
        this.buttonLeft.addStyleName("mm-ftoolbar-button");
        this.buttonLeft.addStyleName("mm-ftoolbar-buttonLeft");
        this.buttonRight.addStyleName("mm-ftoolbar-button");
        this.buttonRight.addStyleName("mm-ftoolbar-buttonRight");

        toolbarPanel.addStyleName("x-toolbar");
        toolbarPanel.addStyleName("mm-ftoolbar-panel");
        toolbarPanel.setWidget(this.panelOuter);
        toolbarPanel.addResizeHandler(event -> recalculateSize());

        BrowserSpecific.INSTANCE.fixIe7FloatingToolbar(toolbarPanel, this.panelOuter);

        toolbarPanel.addStyleName("mm-ftoolbar-component");

        if (HIDDEN_BUTTONS) {
            this.buttonsVisible = false;
            this.panelOuter.addDomHandler(event -> {
                buttonsVisible = true;
                setButtonVisibility();
            }, MouseOverEvent.getType());
            this.panelOuter.addDomHandler(event -> {
                buttonsVisible = false;
                setButtonVisibility();
            }, MouseOutEvent.getType());
        }
        else {
            this.buttonsVisible = true;
        }
        setButtonVisibility();

        this.panelInner.setWidget(this.table);
    }

    @Override
    public void setVisible(boolean visible) {
        if(SessionData.isAsDesign()) {
            this.getWidget().setVisible(visible);
        }
        else {
            super.setVisible(visible);
        }
    }

    public int getToolbarHeightPixel() {
        if (this.toolbarHeight == ToolbarHeight.FOR_ICON_SIZE_UNDEF) {
            throw new IllegalStateException("toolbar height undefined"); // $NON-NLS$
        }
        return this.toolbarHeight.getHeightInPixel() + PADDING_AND_BORDER;
    }

    public <W extends Widget> W add(W widget) {
        this.table.setWidget(0, this.lastCellId, widget);
        this.cellFormatter.addStyleName(0, this.lastCellId, "x-toolbar-cell");
        this.lastCellId++;
        return widget;
    }

    public Widget getWidget(int idx) {
        return this.table.getWidget(0, idx);
    }

    @SuppressWarnings("unused")
    public int getWidgetCount() {
        return this.table.getCellCount(0);
    }

    public void removeAll() {
        this.table.clear();
        this.listFillIds.clear();
        this.lastCellId = 0;
    }

    public void addEmpty() {
        addEmpty("16px"); // $NON-NLS$
    }

    public void addEmpty(String width) {
        table.setHTML(0, this.lastCellId, "<img src=\"clear.cache.gif\" width=\"" + width + "\"/>"); // $NON-NLS$
        cellFormatter.addStyleName(0, this.lastCellId, "x-toolbar-cell");
        this.lastCellId++;
    }

    public void addFill() {
        if(!BrowserSpecific.INSTANCE.isToolbarFillSupported()) {
            add(new Separator());
            return;
        }
        this.table.setHTML(0, this.lastCellId, "&nbsp;"); // $NON-NLS$
        this.cellFormatter.addStyleName(0, this.lastCellId, "x-toolbar-cell");
        setFillWidth(this.lastCellId, "99%"); // $NON-NLS$
        this.listFillIds.add(this.lastCellId);
        this.lastCellId++;
    }

    public void addSeparator() {
        add(new Separator());
    }

    public Label addLabel(String text) {
        final Label label = new Label(text);
        label.setStyleName("mm-toolbar-text");
        add(label);
        return label;
    }

    public void recalculateSize() {
        for (Integer fillId : this.listFillIds) {
            setFillWidth(fillId, "auto"); // $NON-NLS$
        }
        final Style panelInnerStyle = this.panelInner.getElement().getStyle();
        BrowserSpecific.INSTANCE.clearWidthBeforeRecalculation(panelInnerStyle);
        final int widthOuter = this.panelOuter.getOffsetWidth();
        if(widthOuter == 0) {
            return;
        }
        final int widthWidget = this.table.getOffsetWidth();
        if (widthOuter >= widthWidget) {
            panelInnerStyle.setWidth(widthOuter, Style.Unit.PX);
        }
        setFloatPosition(getFloatPosition());
        //noinspection PointlessBooleanExpression,ConstantConditions
        if (!HIDDEN_BUTTONS) {
            setButtonVisibility();
        }
        for (Integer fillId : this.listFillIds) {
            setFillWidth(fillId, "99%"); // $NON-NLS$
        }
    }

    private void setFillWidth(int colId, String width) {
        this.cellFormatter.getElement(0, colId).getStyle().setProperty("width", width); // $NON-NLS$
    }

    public int getFloatPosition() {
        return this.panelInner.getElement().getOffsetLeft();
    }

    public int setFloatPosition(int position) {
        final int widthOuter = this.panelOuter.getOffsetWidth();
        final int widthWidget = this.table.getOffsetWidth();

        if (widthOuter < widthWidget) {
            final int minPosition = widthOuter - widthWidget;
            if (position >= 0) {
                position = 0;
                this.buttonLeftVisible = false;
                this.buttonRightVisible = true;
            }
            else if (position <= minPosition) {
                position = minPosition;
                this.buttonLeftVisible = true;
                this.buttonRightVisible = false;
            }
            else {
                this.buttonLeftVisible = true;
                this.buttonRightVisible = true;
            }
            this.panelInner.getElement().getStyle().setLeft(position, Style.Unit.PX);
        }
        else {
            if (position != 0) {
                this.panelInner.getElement().getStyle().setLeft(0, Style.Unit.PX);
                position = 0;
            }
            this.buttonLeftVisible = false;
            this.buttonRightVisible = false;
        }
        return position;
    }

    private void setButtonVisibility() {
        this.buttonLeft.setVisible(this.buttonsVisible && this.buttonLeftVisible);
        this.buttonRight.setVisible(this.buttonsVisible && this.buttonRightVisible);
    }

    class FloatingAnimation {
        private static final int FRAME_DELAY = 50;
        private static final double FRAME_UPDATE = 15d;
        private long startTime;
        private int startPosition;
        private double updateValue = 0d;

        private final Timer timer = new Timer(){
            @Override
            public void run() {
                updateAnimation();
            }
        };

        public void startLeft(int startPosition) {
            start(startPosition, FRAME_UPDATE);
        }

        public void startRight(int startPosition) {
            start(startPosition, -FRAME_UPDATE);
        }

        private void start(int startPosition, double updateValue) {
            this.startTime = System.currentTimeMillis();
            this.startPosition = startPosition;
            this.updateValue = updateValue;
            this.timer.schedule(FRAME_DELAY);
        }

        public void stop() {
            this.updateValue = 0d;
            setButtonVisibility();
        }

        private void updateAnimation() {
            if (this.updateValue == 0d) {
                return;
            }
            final double delay = System.currentTimeMillis() - this.startTime;
            final double delta = delay / FRAME_DELAY * this.updateValue;
            final int position = this.startPosition + (int) delta;
            final int newPosition = setFloatPosition(position);
            if (newPosition == position) {
                this.timer.schedule(FRAME_DELAY);
            }
            else {
                this.updateValue = 0d;
                setButtonVisibility();
            }
        }
    }
}
