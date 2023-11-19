/*
 * MDISnippetsViewImpl.java
 *
 * Created on 23.04.2008 09:29:18
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.event.DragEvent;
import com.extjs.gxt.ui.client.event.DragListener;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.ResizeEvent;
import com.extjs.gxt.ui.client.event.ResizeListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.fx.Draggable;
import com.extjs.gxt.ui.client.fx.Resizable;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.AbsoluteData;
import com.extjs.gxt.ui.client.widget.layout.AbsoluteLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.myspace.MyspaceView;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MDISnippetsView extends LayoutContainer {
    /**
     * A snippet that can be dragged
     */
    public class MyPanel extends ContentPanel implements SnippetContainerView {
        private Draggable dd;

        private Resizable resizable;

        private final Snippet snippet;

        private DropTarget target;

        private int zIndex;

        public MyPanel(Snippet snippet, final int zIndex) {
            setHeading(snippet.getConfiguration().getString("title", "n/a")); // $NON-NLS-0$ $NON-NLS-1$
            setSize(snippet.getConfiguration().getInt("w", 400), // $NON-NLS-0$
                    snippet.getConfiguration().getInt("h", 300)); // $NON-NLS-0$
            setScrollMode(com.extjs.gxt.ui.client.Style.Scroll.AUTO);

            this.snippet = snippet;
            this.zIndex = zIndex;

            // make sure a click on the header brings window to the front
            getHeader().sinkEvents(Events.OnClick.getEventCode());
            getHeader().addListener(Events.OnClick, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent componentEvent) {
                    bringToFront();
                }
            });

            this.dd = new Draggable(this, getHeader());
            this.dd.setProxyStyle("mm-drag-proxy"); // $NON-NLS-0$
            this.dd.setUpdateZIndex(true);
            this.dd.setContainer(MDISnippetsView.this);
            this.dd.addDragListener(new DragListener() {
                @Override
                public void dragEnd(DragEvent dragEvent) {
                    // the AbsoluteLayout has to be informed about the new position, otherwise
                    // it will use the old AbsoluteData when doLayout is called to add a snippet
                    position();
                    ackChange();
                }
            });

            this.resizable = new Resizable(this, "se"); // handle at south-east // $NON-NLS-0$
            this.resizable.setMinHeight(100);
            this.resizable.setMinWidth(100);
            this.resizable.addResizeListener(new ResizeListener() {
                @Override
                public void resizeEnd(ResizeEvent resizeEvent) {
                    ackChange();
                }
            });

            if (snippet.getDropTargetGroup() == null) {
                return;
            }

            this.target = new DropTarget(this);
            this.target.addDNDListener(new DNDListener() {
                @Override
                public void dragDrop(DNDEvent e) {
                    controller.notifyDrop(MyPanel.this.snippet, e);
                }
            });
            this.target.setGroup(snippet.getDropTargetGroup());
            this.target.setOverStyle("drag-ok"); // $NON-NLS-0$
        }

        private void ackChange() {
            SessionData.INSTANCE.getUser().getAppConfig().firePropertyChange("property.change", null, 1); // $NON-NLS-0$
        }

        @Override
        protected void onRender(@SuppressWarnings("deprecation") Element element, int i) {
            super.onRender(element, i);
            setZIndex(this.zIndex);
        }

        protected void bringToFront() {
            if (this.zIndex != XDOM.getTopZIndex(-1)) {
                this.zIndex = XDOM.getTopZIndex();
                setZIndex(this.zIndex);
            }
        }

        void position() {
            position(getPosition(true).x, getPosition(true).y);
        }

        void position(int x, int y) {
            MDISnippetsView.this.getLayout().setPosition(this, x, y);
        }

        void dispose() {
            this.dd.release();
            this.dd = null;
            this.resizable.release();
            this.resizable = null;
            if (this.target != null) {
                this.target.release();
                this.target = null;
            }
        }

        void captureCoordinates() {
            final El elh = getHeader().el();
            if (elh == null) {
                return;
            }

            final SnippetConfiguration config = this.snippet.getConfiguration();
            config.put("w", getWidth()); // $NON-NLS-0$
            config.put("h", getHeight()); // $NON-NLS-0$
            config.put("x", getPosition(true).x); // $NON-NLS-0$
            config.put("y", getPosition(true).y); // $NON-NLS-0$
            config.put("z", DOM.getIntStyleAttribute(getElement(), "zIndex")); // $NON-NLS-0$ $NON-NLS-1$

//            DebugUtil.logToFirebugConsole(config.toString());
        }
    }

    private final MDISnippetsController controller;

    public MDISnippetsView(final MDISnippetsController controller) {
        this.controller = controller;

        addStyleName("mm-mdi-snippets"); // $NON-NLS-0$
        setBorders(false);
        setScrollMode(com.extjs.gxt.ui.client.Style.Scroll.AUTO);
        setLayout(new AbsoluteLayout());

        addSnippets();

        addListener(Events.BeforeHide, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                captureSnippetCoordinates();
            }
        });
    }

    @Override
    public AbsoluteLayout getLayout() {
        return (AbsoluteLayout) super.getLayout();
    }

    public MDISnippetsController getController() {
        return controller;
    }

    public String getName() {
        return this.controller.getConfig().getName();
    }

    public void captureSnippetCoordinates() {
        for (Component c : getItems()) {
            ((MyPanel) c).captureCoordinates();
        }
    }

    public void destroy() {
        for (Component c : getItems()) {
            destroySnippetPanel((MyPanel) c);
        }
    }

    void addSnippet(final Snippet snippet, boolean layout) {
        final SnippetConfiguration c = snippet.getConfiguration();

        final int maxZ = XDOM.getTopZIndex(-1);

        int z = c.getInt("z", -1); // $NON-NLS-0$
        if (z == -1) {
            z = XDOM.getTopZIndex();
            c.put("z", z); // $NON-NLS-0$
        }
        else if (z > maxZ){
            XDOM.getTopZIndex(z - maxZ);
        }

        final int x = c.getInt("x", 0); // $NON-NLS-0$
        final int y = c.getInt("y", 0); // $NON-NLS-0$

        final MyPanel p = new MyPanel(snippet, z);

        p.setStyleAttribute("position", "absolute"); // $NON-NLS-0$ $NON-NLS-1$
        p.position(x, y);
        p.setStyleName("mm-snippet"); // $NON-NLS-0$

        snippet.getView().setContainer(p);

        if (p.snippet.isConfigurable()) {
            final ToolButton cfgButton = new ToolButton("x-tool-gear"); // $NON-NLS$
            cfgButton.addSelectionListener(new SelectionListener<IconButtonEvent>() {
                @Override
                public void componentSelected(IconButtonEvent iconButtonEvent) {
                    snippet.configure(cfgButton);
                }
            });
            cfgButton.setToolTip(I18n.I.configuration());
            p.getHeader().addTool(cfgButton);
        }

        final ToolButton delButton = new ToolButton("x-tool-close", new SelectionListener<IconButtonEvent>() { // $NON-NLS-0$
            @Override
            public void componentSelected(IconButtonEvent iconButtonEvent) {
                Dialog.confirm(I18n.I.confirmRemoveElement(), new Command() {
                    public void execute() {
                        removeSnippet(p);
                    }
                });
            }
        });
        delButton.setToolTip(I18n.I.tooltipRemoveElement());
        p.getHeader().addTool(delButton);

        add(p, new AbsoluteData(x, y));

        if (layout) {
            doLayout();
        }
    }

    private void addSnippets() {
        for (final Snippet snippet : controller.getSnippets()) {
            addSnippet(snippet, false);
        }
    }

    private void removeSnippet(MyPanel p) {
        // controller will no longer know that the snippet existed
        this.controller.remove(p.snippet);

        destroySnippetPanel(p);

        // the "add snippet" button may be disabled and needs to be enabled again:
        if (MyspaceView.INSTANCE != null) {
            MyspaceView.INSTANCE.updateButtonStates();
        }
    }

    private void destroySnippetPanel(MyPanel p) {
        // remove it from view
        remove(p);
        p.dispose();
    }
}
