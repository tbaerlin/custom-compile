/*
 * MDISnippetsController.java
 *
 * Created on 23.04.2008 09:28:33
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.extjs.gxt.ui.client.event.DNDEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.MyspaceConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MDISnippetsController extends SnippetsController {
    public static MDISnippetsController create(MyspaceConfig config) {
        final DmxmlContext context = new DmxmlContext();
        final List<Snippet> snippets = SnippetsFactory.createSnippets(context, config.getSnippetConfigs());
        return new MDISnippetsController(context, snippets, config);
    }

    private MyspaceConfig config;

    private MDISnippetsController(DmxmlContext context, List<Snippet> snippets, MyspaceConfig config) {
        super(null, context, snippets, null);
        this.config = config;
        onControllerInitialized();
    }

    @Override
    protected Widget createView() {
        return new MDISnippetsView(this);
    }

    public void addSnippet(SnippetClass clazz) {
        final SnippetConfiguration sc = clazz.getDefaultConfig();
        sc.put("context", Snippet.CONFIG_CONTEXT_MDI); // $NON-NLS$
        sc.put("x", 20); // $NON-NLS$
        sc.put("y", 20); // $NON-NLS$
        final Snippet snippet = clazz.newSnippet(this.context, sc);
        add(snippet);
        this.config.addSnippet(snippet.getConfiguration());

        getView().addSnippet(snippet, true);
        refresh();
    }

    public void destroy() {
        super.destroy();
        getView().destroy();
    }

    public MyspaceConfig getConfig() {
        return config;
    }

    public MDISnippetsView getView() {
        initView();
        return (MDISnippetsView) this.view;
    }

    public void remove(Snippet snippet) {
        super.remove(snippet);
        this.config.removeSnippet(snippet.getConfiguration());
        snippet.destroy();
    }

    protected void doOnResult() {
        updateSubviews();
        getView().layout();
    }

    public void notifyDrop(final Snippet origTargetSnip, final DNDEvent evt) {
        if (evt.getData() instanceof QuoteWithInstrument) {
            origTargetSnip.notifyDrop((QuoteWithInstrument) evt.getData());
        }
        if (!evt.isControlKey()) {
            return;
        }
        final String groupId = evt.getDragSource().getGroup();
        boolean foundMultipleInstances = hasSameNamesForDropGroup(groupId, getSnippets());

        if (foundMultipleInstances) {
            Dialog.confirm(I18n.I.confirmReplaceContent(), new Command() {
                public void execute() {
                    doDrop(groupId, origTargetSnip, evt);
                }
            });
        } else {
            doDrop(groupId, origTargetSnip, evt);
        }
    }

    protected void doDrop(String groupId, Snippet exclude, DNDEvent evt) {
        // drop on all snippets
        for (Snippet snip : getSnippets()) {
            if (!groupId.equals(snip.getDropTargetGroup())) {
                continue;
            }
            if (snip == exclude) {
                continue; // already dropped
            }
            if (evt.getData() instanceof QuoteWithInstrument) {
                snip.notifyDrop((QuoteWithInstrument) evt.getData());
            }
        }
    }

    protected boolean hasSameNamesForDropGroup(String groupId, List<Snippet> snippets) {
        HashSet<String> names = new HashSet<>();
        Iterator<Snippet> iterator = snippets.iterator();
        for (Snippet snip : snippets) {
            if (!groupId.equals(snip.getDropTargetGroup())) {
                continue;
            }
            String name =  iterator.next().getConfiguration().getName();
            if (names.contains(name)) {
                return true;
            }
            names.add(name);
        }
        return false;
    }

}
