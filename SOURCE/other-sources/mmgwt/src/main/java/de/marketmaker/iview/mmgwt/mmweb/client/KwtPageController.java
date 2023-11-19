/*
 * KwtPageController.java
 *
 * Created on 23.02.2009 18:01:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.HashMap;
import java.util.Map;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Label;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Folder;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.TreeLeaf;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.GuiDefsChangedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.GuiDefsChangedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class KwtPageController extends AbstractPageController {
    private static final Map<String, String> FILE_TYPES = new HashMap<String, String>();

    private static final String DEFAULT_TYPE = "mm-fileType-html"; // $NON-NLS-0$

    static {
        FILE_TYPES.put("pdf", "mm-fileType-pdf"); // $NON-NLS-0$ $NON-NLS-1$
        FILE_TYPES.put("xls", "mm-fileType-xls"); // $NON-NLS-0$ $NON-NLS-1$
        FILE_TYPES.put("doc", "mm-fileType-doc"); // $NON-NLS-0$ $NON-NLS-1$
        FILE_TYPES.put("html", DEFAULT_TYPE); // $NON-NLS-0$
    }

    private static int instanceId = 0;

    private final String guidefsPath;
    private JSONWrapper wrapper;

    private final String key;

    private ContentPanel panel;

    // TODO: remove the wrapper element, guidefsPath should be enough to find the json content in the guidefs
    public KwtPageController(JSONWrapper wrapper, String guidefsPath) {
        super(AbstractMainController.INSTANCE.getView());
        this.guidefsPath = guidefsPath;
        this.wrapper = wrapper;
        this.key = "KWT_" + ++instanceId; // $NON-NLS-0$
        EventBusRegistry.get().addHandler(GuiDefsChangedEvent.getType(), new GuiDefsChangedHandler(){
            @Override
            public void onGuidefsChange(GuiDefsChangedEvent event) {
                panel = null;
                KwtPageController.this.wrapper = SessionData.INSTANCE.getGuiDef(KwtPageController.this.guidefsPath);
            }
        });
    }

    public String getKey() {
        return this.key;
    }

    private String getFileType(String url) {
        final int pos = url.lastIndexOf('.');
        if (pos < 0 || pos >= url.length() - 1) {
            return DEFAULT_TYPE;
        }
        final String suffix = url.substring(pos + 1).toLowerCase();
        final String style = FILE_TYPES.get(suffix);
        return style != null ? style : DEFAULT_TYPE;
    }

    final ModelIconProvider<ModelData> fileTypeIconProvider = new ModelIconProvider<ModelData>() {
        public AbstractImagePrototype getIcon(ModelData model) {
            if (model.get("url") != null) { // $NON-NLS-0$
                return IconImage.get(getFileType((String) model.get("url"))); // $NON-NLS-0$
            }
            return null;
        }
    };

    public void onPlaceChange(PlaceChangeEvent event) {
        if (this.panel == null) {
            initPanel();
        }
        getContentContainer().setContent(this.panel);
        this.panel.layout();
    }

    private void initPanel() {
        final ContentPanel treePanel;
        if (isLeafsOnly()) {
            treePanel = initTreePanel(this.wrapper, false);
        }
        else {
            treePanel = initTreePanels();
        }
        treePanel.addStyleName("mm-content"); // $NON-NLS-0$

        final ContentPanel panel = new ContentPanel();
        panel.setHeaderVisible(false);
        panel.setBorders(false);
        panel.setLayout(new BorderLayout());
        final int width = 400;
        final BorderLayoutData westData = new BorderLayoutData(Style.LayoutRegion.WEST, width);
        westData.setSplit(false);
        final ContentPanel west = new ContentPanel();
        west.setHeaderVisible(false);
        //noinspection GWTStyleCheck
        west.setStyleName("mm-contentData mm-contentPadding"); // $NON-NLS-0$
        west.setLayout(new FitLayout());
        west.add(treePanel);
        panel.add(west, westData);

        panel.add(new Label(""), new BorderLayoutData(Style.LayoutRegion.CENTER)); // $NON-NLS-0$

        this.panel = panel;
    }

    private boolean isLeafsOnly() {
        final JSONWrapper children = this.wrapper.get("children"); // $NON-NLS-0$
        for (int i = 0; i < children.size(); i++) {
            final JSONWrapper child = children.get(i);
            if (child.get("children") != JSONWrapper.INVALID) { // $NON-NLS-0$
                return false;
            }
        }
        return true;
    }

    private ContentPanel initTreePanels() {
        final ContentPanel result = new ContentPanel();
        result.setHeaderVisible(false);
        final AccordionLayout al = new AccordionLayout();
        al.setFill(true);
        result.setLayout(al);
        result.setWidth(600);

        final JSONWrapper children = this.wrapper.get("children"); // $NON-NLS-0$
        for (int i = 0; i < children.size(); i++) {
            final JSONWrapper child = children.get(i);
            final ContentPanel tp = initTreePanel(child, true);
            result.add(tp);
        }
        return result;
    }

    private ContentPanel initTreePanel(JSONWrapper w, boolean withName) {
        final ContentPanel cp = new ContentPanel();
        cp.setBorders(false);
        cp.setScrollMode(com.extjs.gxt.ui.client.Style.Scroll.AUTO);
        if (withName) {
            cp.setHeading(w.get("name").stringValue()); // $NON-NLS-0$
        }
        cp.setHeaderVisible(withName);

        final Folder model = createTreeModel(w);

        final TreeStore<ModelData> store = new TreeStore<ModelData>();
        store.add(model.getChildren(), true);

        final TreePanel<ModelData> tp = new TreePanel<ModelData>(store);
        tp.setBorders(false);
        tp.setDisplayProperty("name"); // $NON-NLS-0$
        for (ModelData modelData : store.getRootItems()) {
            tp.setExpanded(modelData, true);
        }

        tp.setIconProvider(this.fileTypeIconProvider);

        tp.addListener(Events.OnClick, new Listener<TreePanelEvent>() {
            public void handleEvent(TreePanelEvent event) {
                final ModelData data = event.getItem();
                final String url = (String) data.get("url"); // $NON-NLS$
                if (url != null) {
                    if (url.startsWith("#")) {
                        PlaceUtil.goTo(url.substring(1));
                    }
                    else {
                        Window.open(url, "_blank", ""); // $NON-NLS$
                    }
                }
            }
        });

        tp.getStyle().setNodeCloseIcon(IconImage.get("mm-tree-folder-closed")); // $NON-NLS-0$
        tp.getStyle().setNodeOpenIcon(IconImage.get("mm-tree-folder-open")); // $NON-NLS-0$

        cp.add(tp);
        return cp;
    }

    private Folder createTreeModel(JSONWrapper w) {
        final JSONWrapper children = w.get("children"); // $NON-NLS-0$

        final Folder root = new Folder("root"); // $NON-NLS-0$

        for (int i = 0; i < children.size(); i++) {
            addChild(root, children.get(i));
        }

        return root;
    }

    private void addChild(Folder parent, JSONWrapper child) {
        final String name = child.get("name").stringValue(); // $NON-NLS-0$
        final JSONWrapper url = child.get("url"); // $NON-NLS-0$

        if (url.isString()) {
            final TreeLeaf leaf = new TreeLeaf(name);
            leaf.set("url", url.stringValue()); // $NON-NLS-0$
            parent.add(leaf);
            return;
        }

        final JSONWrapper children = child.get("children"); // $NON-NLS-0$
        if (children.isArray()) {
            final Folder folder = new Folder(name);
            parent.add(folder);
            for (int i = 0; i < children.size(); i++) {
                addChild(folder, children.get(i));
            }
        }
    }
}
