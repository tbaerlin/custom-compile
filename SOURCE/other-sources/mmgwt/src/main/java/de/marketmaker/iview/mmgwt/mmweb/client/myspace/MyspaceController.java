/*
 * MyspaceController.java
 *
 * Created on 11.04.2008 14:33:29
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.myspace;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.MainControllerListenerAdapter;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.MyspaceConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.DzPageSnippetView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.MDISnippetsController;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.MDISnippetsView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MyspaceController implements PageController {
    private final ArrayList<MDISnippetsController> subControllers = new ArrayList<>();

    private MyspaceView view;

    private MDISnippetsController current;

    private static MyspaceController INSTANCE;

    public static MyspaceController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MyspaceController();
        }
        return INSTANCE;
    }

    private MyspaceController() {
        final ArrayList<MyspaceConfig> configs = getConfig().getMyspaceConfigs();
        for (int i = 0; i < configs.size(); i++) {
            if (i < SessionData.INSTANCE.getMaxNumMyspaces()) {
                addSubcontroller(configs.get(i));
            }
            else {
                // R-62045 ensure user cannot access more spaces than current product allows
                getConfig().removeMyspace(i);
            }
        }

        AbstractMainController.INSTANCE.addListener(new MainControllerListenerAdapter() {
            public void beforeStoreState() {
                if (view != null) {
                    view.captureSnippetCoordinates();
                }
            }
        });
    }

    public void asStartPage(int activeItem) {
        final MyspaceConfig config = getConfig().getMyspaceConfigs().get(activeItem);
        getConfig().addProperty(AppConfig.PROP_KEY_STARTPAGE, "B_A/" + config.getId()); // $NON-NLS-0$
    }

    public MDISnippetsView getStartPage() {
        final String startPageConfigId = getStartPageConfigId();
        return getSpaceView(startPageConfigId);
    }

    public String getStartPageConfigId() {
        final String key = getConfig().getProperty(AppConfig.PROP_KEY_STARTPAGE);
        if (key == null || !key.startsWith("B_A/")) { // $NON-NLS-0$
            return null;
        }
        final String[] token = StringUtil.splitToken(key);
        return token[1];
    }

    public MDISnippetsController createSpace(String name) {
        final MyspaceConfig config = getConfig().addMyspace(name);
        return addSubcontroller(config);
    }

    public void destroy() {
        for (MDISnippetsController controller : subControllers) {
            controller.destroy();
        }
    }

    public boolean supportsHistory() {
        return true;
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        boolean initializeActiveSpace = false;
        if (this.view == null) {
            this.view = new MyspaceView(this);
            initializeActiveSpace = true;
        }
        AbstractMainController.INSTANCE.getView().setContent((ContentView) this.view);
        if (!this.view.isVisible()) {
            this.view.show();
        }

        final HistoryToken historyToken = event.getHistoryToken();
        final String spaceId = historyToken.get(1, null);
        if (spaceId == null) {
            if (initializeActiveSpace) {
                this.view.setActiveSpace(0);
            }
            else {
                this.view.updateHomeIcon();
            }
            return;
        }
        // the active item has been specified
        final ArrayList<MyspaceConfig> configs = getConfig().getMyspaceConfigs();
        for (int i = 0; i < configs.size(); i++) {
            if (spaceId.equals(configs.get(i).getId())) {
                this.view.setActiveSpace(i);
                return;
            }
        }
        this.view.setActiveSpace(0);
    }

    public void activate() {
        if (this.current != null) {
            this.current.activate();
        }
    }

    public void deactivate() {
        if (this.current != null) {
            this.current.deactivate();
        }
    }

    public void refresh() {
        if (this.current != null) {
            this.current.reload();
        }
    }

    public void removeSpace(int activeItem) {
        getConfig().removeMyspace(activeItem);
        final MDISnippetsController c = this.subControllers.remove(activeItem);
        c.destroy();
    }

    public void renameSpace(int activeItem, String title) {
        getConfig().renameMyspace(activeItem, title);
    }

    int getNumSpaces() {
        return this.subControllers.size();
    }

    MDISnippetsView getSpaceView(int n) {
        return this.subControllers.get(n).getView();
    }

    private MDISnippetsView getSpaceView(String configId) {
        final ArrayList<MyspaceConfig> configs = getConfig().getMyspaceConfigs();
        for (int i = 0; i < configs.size(); i++) {
            if (configId.equals(configs.get(i).getId())) {
                return this.subControllers.get(i).getView();
            }
        }
        return null;
    }

    int getViewIndex(MDISnippetsView view) {
        return this.subControllers.indexOf(view.getController());
    }

    void addSnippet(int n, SnippetClass clazz) {
        this.subControllers.get(n).addSnippet(clazz);
    }

    public ArrayList<MDISnippetsController> getSubControllers() {
        return this.subControllers;
    }

    private MDISnippetsController addSubcontroller(MyspaceConfig config) {
        final MDISnippetsController controller = MDISnippetsController.create(config);
        this.subControllers.add(controller);
        return controller;
    }

    private AppConfig getConfig() {
        return SessionData.INSTANCE.getUser().getAppConfig();
    }

    public String getPrintHtml() {
        final Element result = (Element) view.getElement().cloneNode(true);

        // each DzPageSnippetView contains an iframe that has to be converted to a div for printing
        // their src attribute is empty, their content is added dynamically and somehow lost...
        // there is a hook in iframe.onLoad to rerednder their content
        // unfortuantly this hook is not called when reading the dom for printing... its a mess
        // TODO: make all myspaces snippets implement a decent getPrintHtml() method, then collect and layout their html here
        final HashMap<String, String> contentData = findContent();
        final HashMap<Element, String> dzIframes = findDzPageSnippetFrames(result);

        for (Map.Entry<Element, String> entry : dzIframes.entrySet()) {
            final Element iframe = entry.getKey();
            final String pagenumber = entry.getValue();
            final Element parent = iframe.getParentElement();
            if (contentData.containsKey(pagenumber)) {
                parent.replaceChild(new HTML(contentData.get(pagenumber)).getElement(), iframe);
            } else {
                parent.replaceChild(new Label("pagenumber: " + pagenumber + " not found").getElement(), iframe); // $NON-NLS$
            }
        }

        return result.getInnerHTML();
    }

    // collect iframes and pagenumber in a hashmap
    private HashMap<Element, String> findDzPageSnippetFrames(Element result) {
        final HashMap<Element, String> iframeStore = new HashMap<>();
        // this is a live view of the matching elements:
        final NodeList<Element> iframes = result.getElementsByTagName("iframe");  // $NON-NLS$
        for (int i = 0; i < iframes.getLength(); i++) {
            final Element iframe = iframes.getItem(i);
            final Element parent = iframe.getParentElement();
            final String pagenumber = parent.getAttribute("pagenumber");  // $NON-NLS$
            if (StringUtil.hasText(pagenumber)) {
                iframeStore.put(iframe, pagenumber);
            }
        }
        return iframeStore;
    }

    // collect pagenumber and content in a hashmap
    private HashMap<String, String> findContent() {
        final HashMap<String, String> contentStore = new HashMap<>();
        final List<Snippet> currentSnippets = current.getSnippets();
        for (Snippet snippet : currentSnippets) {
            final SnippetView snippetView = snippet.getView();
            if (snippetView instanceof DzPageSnippetView) {
                final DzPageSnippetView dzPageSnippetView = ((DzPageSnippetView) snippetView);
                if (dzPageSnippetView.isPrintable()) {
                    final String printHtml = dzPageSnippetView.getPrintHtml();
                    final String pagenumber = snippet.getConfiguration().getString("pagenumber");  // $NON-NLS$
                    contentStore.put(pagenumber, printHtml);
                }
            }
        }
        return contentStore;
    }


    @Override
    public boolean isPrintable() {
        return true;
    }

    public PdfOptionSpec getPdfOptionSpec() {
        return null;
    }

    public void addPdfPageParameters(Map<String, String> mapParameters) {
    }

    public String[] getAdditionalStyleSheetsForPrintHtml() {
        final Set<String> collect = new HashSet<>();
        final List<Snippet> currentSnippets = current.getSnippets();
        for (Snippet snippet : currentSnippets) {
            final SnippetView snippetView = snippet.getView();
            if (snippetView instanceof DzPageSnippetView) {
                collect.add(((DzPageSnippetView) snippetView).getAdditionalStylesheet());
            }
        }
        return collect.toArray(new String[collect.size()]);
    }

    public void setCurrent(MDISnippetsController controller) {
        if (this.current != null && this.current != controller) {
            this.current.deactivate();
        }
        this.current = controller;
        this.current.activate();
    }

    public MyspaceView getView() {
        return view;
    }
}
