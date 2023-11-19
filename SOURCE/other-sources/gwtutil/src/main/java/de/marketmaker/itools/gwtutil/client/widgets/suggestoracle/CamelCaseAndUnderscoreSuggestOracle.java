/*
 * CamelCaseAndUnderscoreSuggestOracle.java
 *
 * Created on 15.06.2012 15:02:35
 *
 * Copyright 2007 Google Inc.
 * Copyright 2012 vwd AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package de.marketmaker.itools.gwtutil.client.widgets.suggestoracle;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This SuggestOracle implementation treats camel cases and underscores as whitespaces.
 * This is mostly a copy of GWT's MultiWordSuggestOracle.
 *
 * @see com.google.gwt.user.client.ui.MultiWordSuggestOracle
 *
 * @author Markus Dick
 */
public class CamelCaseAndUnderscoreSuggestOracle extends SuggestOracle {
    private static final char WHITESPACE_CHAR = ' ';
    private static final String WHITESPACE_STRING = " "; // $NON-NLS$
    private static final char UNDERSCORE_CHARACTER = '_';

    /**
     * Regular expression used to collapse all whitespace in a query string.
     */
    private static final String NORMALIZE_TO_SINGLE_WHITE_SPACE = "\\s+"; // $NON-NLS$

    /**
     * Associates substrings with words.
     */
    private final PrefixTree tree = new PrefixTree();

    /**
     * Associates individual words with candidates.
     */
    private Map<String, Set<String>> toCandidates = new HashMap<String, Set<String>>();

    /**
     * Associates candidates with their formatted suggestions.
     */
    private Map<String, String> toRealSuggestions = new HashMap<String, String>();

    private Response defaultResponse;

    public CamelCaseAndUnderscoreSuggestOracle() {
        /* nothing to do here */
    }

    /**
     * Adds a suggestion to the oracle. Each suggestion must be plain text.
     *
     * @param suggestion the suggestion
     */
    public void add(String suggestion) {
        String candidate = normalizeSuggestion(suggestion);
        // candidates --> real suggestions.
        toRealSuggestions.put(candidate, suggestion);

        // word fragments --> candidates.
        String[] words = candidate.split(WHITESPACE_STRING);
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            tree.add(word);
            Set<String> l = toCandidates.get(word);
            if (l == null) {
                l = new HashSet<String>();
                toCandidates.put(word, l);
            }
            l.add(candidate);
        }
    }

    /**
     * Adds all suggestions specified. Each suggestion must be plain text.
     *
     * @param collection the collection
     */
    public final void addAll(Collection<String> collection) {
        for (String suggestion : collection) {
            add(suggestion);
        }
    }

    /**
     * Removes all of the suggestions from the oracle.
     */
    public void clear() {
        tree.clear();
        toCandidates.clear();
        toRealSuggestions.clear();
    }

    @Override
    public boolean isDisplayStringHTML() {
        return true;
    }

    @Override
    public void requestDefaultSuggestions(Request request, Callback callback) {
        if (defaultResponse != null) {
            callback.onSuggestionsReady(request, defaultResponse);
        } else {
            super.requestDefaultSuggestions(request, callback);
        }
    }

    @Override
    public void requestSuggestions(Request request, Callback callback) {
        String query = normalizeSearch(request.getQuery());
        int limit = request.getLimit();

        // Get candidates from search words.
        List<String> candidates = createCandidatesFromSearch(query);

        // Respect limit for number of choices.
        int numberTruncated = Math.max(0, candidates.size() - limit);
        for (int i = candidates.size() - 1; i > limit; i--) {
            candidates.remove(i);
        }

        // Convert candidates to suggestions.
        List<MultiWordSuggestion> suggestions =
                convertToFormattedSuggestions(query, candidates);

        Response response = new Response(suggestions);
        response.setMoreSuggestionsCount(numberTruncated);

        callback.onSuggestionsReady(request, response);
    }

    /**
     * Sets the default suggestion collection.
     *
     * @param suggestionList the default list of suggestions
     */
    public void setDefaultSuggestions(Collection<Suggestion> suggestionList) {
        this.defaultResponse = new Response(suggestionList);
    }

    /**
     * A convenience method to set default suggestions using plain text strings.
     *
     * Note to use this method each default suggestion must be plain text.
     *
     * @param suggestionList the default list of suggestions
     */
    public final void setDefaultSuggestionsFromText(
            Collection<String> suggestionList) {
        Collection<Suggestion> accum = new ArrayList<Suggestion>();
        for (String candidate : suggestionList) {
            accum.add(createSuggestion(candidate, candidate));
        }
        setDefaultSuggestions(accum);
    }

    /**
     * Creates the suggestion based on the given replacement and display strings.
     *
     * @param replacementString the string to enter into the SuggestBox's text box
     *          if the suggestion is chosen
     * @param displayString the display string
     *
     * @return the suggestion created
     */
    protected MultiWordSuggestion createSuggestion(String replacementString,
                                                   String displayString) {
        return new MultiWordSuggestion(replacementString, displayString);
    }

    /**
     * Returns real suggestions with the given query in <code>strong</code> html
     * font.
     *
     * @param query query string
     * @param candidates candidates
     * @return real suggestions
     */
    private List<MultiWordSuggestion> convertToFormattedSuggestions(String query,
                                                                    List<String> candidates) {
        List<MultiWordSuggestion> suggestions = new ArrayList<MultiWordSuggestion>();

        for (int i = 0; i < candidates.size(); i++) {
            String candidate = candidates.get(i);
            int cursor = 0;
            int index = 0;
            // Use real suggestion for assembly.
            String formattedSuggestion = toRealSuggestions.get(candidate);

            // Create strong search string.
            SafeHtmlBuilder accum = new SafeHtmlBuilder();

            String[] searchWords = query.split(WHITESPACE_STRING);
            while (true) {
                WordBounds wordBounds = findNextWord(formattedSuggestion.toLowerCase(), searchWords, index);
                if (wordBounds == null) {
                    break;
                }

                String part1 = formattedSuggestion.substring(cursor, wordBounds.startIndex);
                String part2 = formattedSuggestion.substring(wordBounds.startIndex,
                        wordBounds.endIndex);
                cursor = wordBounds.endIndex;
                accum.appendEscaped(part1);
                accum.appendHtmlConstant("<strong>"); // $NON-NLS$
                accum.appendEscaped(part2);
                accum.appendHtmlConstant("</strong>"); // $NON-NLS$
                index = wordBounds.endIndex;
            }

            // Check to make sure the search was found in the string.
            if (cursor == 0) {
                continue;
            }

            accum.appendEscaped(formattedSuggestion.substring(cursor));
            MultiWordSuggestion suggestion = createSuggestion(formattedSuggestion,
                    accum.toSafeHtml().asString());
            suggestions.add(suggestion);
        }
        return suggestions;
    }

    /**
     * Find the sorted list of candidates that are matches for the given query.
     */
    private List<String> createCandidatesFromSearch(String query) {
        ArrayList<String> candidates = new ArrayList<String>();

        if (query.length() == 0) {
            return candidates;
        }

        // Find all words to search for.
        String[] searchWords = query.split(WHITESPACE_STRING);
        HashSet<String> candidateSet = null;
        for (int i = 0; i < searchWords.length; i++) {
            String word = searchWords[i];

            // Eliminate bogus word choices.
            if (word.length() == 0 || word.matches(WHITESPACE_STRING)) {
                continue;
            }

            // Find the set of candidates that are associated with all the
            // searchWords.
            HashSet<String> thisWordChoices = createCandidatesFromWord(word);
            if (candidateSet == null) {
                candidateSet = thisWordChoices;
            } else {
                candidateSet.retainAll(thisWordChoices);

                if (candidateSet.size() < 2) {
                    // If there is only one candidate, on average it is cheaper to
                    // check if that candidate contains our search string than to
                    // continue intersecting suggestion sets.
                    break;
                }
            }
        }
        if (candidateSet != null) {
            candidates.addAll(candidateSet);
            Collections.sort(candidates);
        }
        return candidates;
    }

    /**
     * Creates a set of potential candidates that match the given query.
     *
     * @param query query string
     * @return possible candidates
     */
    private HashSet<String> createCandidatesFromWord(String query) {
        HashSet<String> candidateSet = new HashSet<String>();
        List<String> words = tree.getSuggestions(query, Integer.MAX_VALUE);
        if (words != null) {
            // Find all candidates that contain the given word the search is a
            // subset of.
            for (int i = 0; i < words.size(); i++) {
                Collection<String> belongsTo = toCandidates.get(words.get(i));
                if (belongsTo != null) {
                    candidateSet.addAll(belongsTo);
                }
            }
        }
        return candidateSet;
    }

    /**
     * Returns a {@link WordBounds} representing the first word in {@code
     * searchWords} that is found in candidate starting at {@code indexToStartAt}
     * or {@code null} if no words could be found.
     */
    private WordBounds findNextWord(String candidate, String[] searchWords, int indexToStartAt) {
        WordBounds firstWord = null;
        for (String word : searchWords) {
            int index = candidate.indexOf(word, indexToStartAt);
            if (index != -1) {
                WordBounds newWord = new WordBounds(index, word.length());
                if (firstWord == null || newWord.compareTo(firstWord) < 0) {
                    firstWord = newWord;
                }
            }
        }
        return firstWord;
    }

    /**
     * Normalize the search key by making it lower case, removing multiple spaces,
     * apply whitespace masks, and make it lower case.
     */
    protected String normalizeSearch(String search) {
        // Use the same whitespace masks and case normalization for the search
        // string as was used with the candidate values.
        search = normalizeSuggestion(search);

        // Remove all excess whitespace from the search string.
        search = search.replaceAll(NORMALIZE_TO_SINGLE_WHITE_SPACE,
                WHITESPACE_STRING);

        return search.trim();
    }

    /**
     * Takes the formatted suggestion,
     * makes it lower case, converts CamelCases to whitespaces,
     * and blanks out any existing underscores for searching.
     */
    protected String normalizeSuggestion(String formattedSuggestion) {
        formattedSuggestion = normalizeSuggestionCamelCase(formattedSuggestion);

        // Lower case suggestion.
        formattedSuggestion = formattedSuggestion.toLowerCase();

        // Apply underscore.
        formattedSuggestion = formattedSuggestion.replace(UNDERSCORE_CHARACTER, WHITESPACE_CHAR);

        return formattedSuggestion;
    }

    protected String normalizeSuggestionCamelCase(String search) {
        if(search == null || search.length() <= 2) return search;

        int searchLength = search.length();
        String normalized = "";
        int subStart = 0;
        char currentChar = 0;
        char predecessorChar = search.charAt(1);

        for(int i = 2; i < searchLength; i++) {
            currentChar = search.charAt(i);

            if(Character.isLowerCase(predecessorChar) && Character.isUpperCase(currentChar)) {
                normalized += search.substring(subStart, i);
                normalized += " ";
                subStart = i;
            }

            predecessorChar = currentChar;
        }
        normalized += search.substring(subStart, searchLength);

        return normalized;
    }

    /**
     * Represents the bounds of a word within a string.
     *
     * The bounds are represented by a {@code startIndex} (inclusive) and
     * an {@code endIndex} (exclusive).
     */
    public static class WordBounds implements Comparable<WordBounds> {

        final int startIndex;
        final int endIndex;

        public WordBounds(int startIndex, int length) {
            this.startIndex = startIndex;
            this.endIndex = startIndex + length;
        }

        public int compareTo(WordBounds that) {
            int comparison = this.startIndex - that.startIndex;
            if (comparison == 0) {
                comparison = that.endIndex - this.endIndex;
            }
            return comparison;
        }
    }

    /**
     */
    public static class MultiWordSuggestion implements Suggestion, IsSerializable {
        private String displayString;
        private String replacementString;

        /**
         * Constructor used by RPC.
         */
        public MultiWordSuggestion() {
        }

        /**
         * Constructor for <code>MultiWordSuggestion</code>.
         *
         * @param replacementString the string to enter into the SuggestBox's text
         *          box if the suggestion is chosen
         * @param displayString the display string
         */
        public MultiWordSuggestion(String replacementString, String displayString) {
            this.replacementString = replacementString;
            this.displayString = displayString;
        }

        public String getDisplayString() {
            return displayString;
        }

        public String getReplacementString() {
            return replacementString;
        }
    }
}
