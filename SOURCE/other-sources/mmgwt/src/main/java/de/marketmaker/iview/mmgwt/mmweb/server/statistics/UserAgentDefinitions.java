/*
 * UserAgentDefinition.java
 *
 * Created on 09.04.2010 13:08:35
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author oflege
 */
class UserAgentDefinitions {
    private static class Definition {
        private final String replacement;

        private final Pattern pattern;

        public Definition(String replacement, Pattern pattern) {
            this.replacement = replacement;
            this.pattern = pattern;
        }

        public String resolveName(String s) {
            final Matcher m = this.pattern.matcher(s);
            if (m.matches()) {
                return m.replaceFirst(this.replacement);
            }
            return null;
        }
    }

    private final List<Definition> definitions = new ArrayList<>();

    void add(String replacement, String pattern) {
        this.definitions.add(new Definition(replacement, Pattern.compile(pattern)));
    }

    String resolveName(String s) {
        for (Definition definition : definitions) {
            final String result = definition.resolveName(s);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
