/*
 * SnippetFactory.java
 *
 * Created on 01.04.2008 10:18:25
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SnippetsFactory {

    private static final String DEFAULT_CONTEXT_NAME = ""; // $NON-NLS$

    private final ContentContainer contentContainer;

    private final String def;

    private final HashMap<String, DmxmlContext> contexts = new HashMap<>();

    private final HashMap<String, ArrayList<Snippet>> snippetsByContext
            = new HashMap<>();

    // all snippets, including those in subcontexts
    private final ArrayList<Snippet> snippets;

    private final ArrayList<SnippetConfiguration> configs;

    public static SnippetsController createFlexController(ContentContainer contentContainer, String def) {
        return new SnippetsFactory(contentContainer, def).buildFlex();
    }

    public static SnippetsController createSingleController(ContentContainer contentContainer, String def) {
        return new SnippetsFactory(contentContainer, def).buildSingle();
    }

    private SnippetsFactory(ContentContainer contentContainer, String def) {
        this.contentContainer = contentContainer;
        this.def = def;
        this.configs = parseConfigs();
        this.snippets = createSnippets();
    }

    private SnippetsController buildFlex() {
        final SnippetsController result = getController(createFlexController(DEFAULT_CONTEXT_NAME));
        result.setDefKey(this.def);
        return result;
    }

    private SnippetsController buildSingle() {
        final SnippetsController result = getController(createSingleController(DEFAULT_CONTEXT_NAME));
        result.setDefKey(this.def);
        return result;
    }

    private ArrayList<Snippet> createSnippets() {
        final ArrayList<Snippet> result = new ArrayList<>(this.configs.size());
        for (SnippetConfiguration config : this.configs) {
            final String contextName = config.getString("subcontext", DEFAULT_CONTEXT_NAME);// $NON-NLS-0$
            final DmxmlContext context = getOrCreateContext(contextName);
            final Snippet snippet = SnippetClass.create(context, config);
            if (snippet != null) {
                result.add(snippet);
                this.snippetsByContext.get(contextName).add(snippet);
            }
        }
        return result;
    }

    private ArrayList<SnippetConfiguration> parseConfigs() {
        final ArrayList<SnippetConfiguration> result = new ArrayList<>();

        final JSONWrapper guiDef = SessionData.INSTANCE.getGuiDef(this.def);
        if (guiDef.isArray()) {
            for (int i = 0; i < guiDef.size(); i++) {
                final SnippetConfiguration sc = SnippetConfiguration.createFrom(guiDef.get(i));
                if (sc != null) {
                    result.add(sc);
                }
            }
        }
        else {
            final ArrayList<String> defs = StringUtil.split(this.def, ':');
            for (String s : defs) {
                if (s.length() > 0) {
                    result.add(SnippetConfiguration.createFrom(s));
                }
            }
        }
        return result;
    }

    private SnippetsController getController(SnippetsController snippetsController) {
        final HashSet<String> subcontextNames = new HashSet<>(this.contexts.keySet());
        subcontextNames.remove(DEFAULT_CONTEXT_NAME);
        if (!subcontextNames.isEmpty()) {
            snippetsController.setSubcontrollers(createSubcontrollers(subcontextNames));
        }

        introduceReferences(snippetsController);
        snippetsController.onControllerInitialized();

        return snippetsController;
    }

    private void introduceReferences(SnippetsController controller) {
        for (SnippetConfiguration config : this.configs) {
            final String id = config.getString("id", null); // $NON-NLS-0$
            if (id == null) {
                continue;
            }
            final Snippet<?> snippet = controller.getSnippet(id);
            if (snippet == null) {
                continue;
            }
            final Set<String> names = snippet.getReferenceNames();
            for (String name : names) {
                final String[] ids = config.getArray(name);
                if (ids != null) {
                    final Snippet[] others = new Snippet[ids.length];
                    for (int i = 0; i < ids.length; i++) {
                        others[i] = controller.getSnippet(ids[i]);
                    }
                    snippet.setReferences(name, others);
                }
                else {
                    final String other = config.getString(name);
                    snippet.setReferences(name, new Snippet[] { controller.getSnippet(other) });
                }
            }
        }
    }

    private ArrayList<SnippetsController> createSubcontrollers(HashSet<String> subcontextNames) {
        final ArrayList<SnippetsController> result = new ArrayList<>();
        for (String name : subcontextNames) {
            result.add(createFlexController(name));
        }
        return result;
    }

    private SnippetsController createSingleController(String name) {
        return new SingleSnippetController(this.contentContainer,
                this.contexts.get(name),
                this.snippetsByContext.get(name),
                DEFAULT_CONTEXT_NAME.equals(name) ? this.snippets : null);
    }

    private SnippetsController createFlexController(String name) {
        return new SnippetsController(this.contentContainer,
                this.contexts.get(name),
                this.snippetsByContext.get(name),
                DEFAULT_CONTEXT_NAME.equals(name) ? this.snippets : null);
    }

    private DmxmlContext getOrCreateContext(String id) {
        final DmxmlContext existing = this.contexts.get(id);
        if (existing != null) {
            return existing;
        }
        final DmxmlContext result = new DmxmlContext();
        this.contexts.put(id, result);
        this.snippetsByContext.put(id, new ArrayList<Snippet>());
        return result;
    }

    public static List<Snippet> createSnippets(DmxmlContext context,
            ArrayList<SnippetConfiguration> configs) {
        final List<Snippet> snippets = new ArrayList<>(configs.size());

        for (SnippetConfiguration config : configs) {
            final Snippet snippet = SnippetClass.create(context, config);
            if (snippet != null) {
                snippets.add(snippet);
            }
        }
        return snippets;
    }
}
