/*
 * PrefixTree.java
 *
 * Created on 15.06.2012 15:23:50
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.itools.gwtutil.client.widgets.suggestoracle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * An implementation of a prefix tree (aka Trie) that can be used within suggest oracles.
 * The implementation uses Breadth First Search (BFS) to find the leafs of a subtree.
 *
 * @author Markus Dick
 */
public class PrefixTree {
    private final Map<String, Object>root;

    public PrefixTree() {
        root = new HashMap<String, Object>();
    }

    public void add(String word) {
        if(word.length() == 0) return;

        Map<String, Object>node = root;
        Map<String, Object>nextNode = root;
        String key = "";

        for(int i = 0; i < word.length(); i++) {
            key += word.charAt(i);
            nextNode = (Map)node.get(key);
            if(nextNode == null) {
                nextNode = new HashMap<String, Object>();
                node.put(key, nextNode);
                node = nextNode;
            }
            else {
                node = nextNode;
            }
        }
        node.put(key, key); //indicates, that this key is also a leaf
    }

    public void clear() {
        root.clear();
    }

    public List<String> getSuggestions(String query, Integer limit) {
        if(limit == 0 || query == null || query.trim().length() == 0) return Collections.EMPTY_LIST;

        final ArrayList<String> list = new ArrayList<String>();
        final LinkedList<Map> queue = new LinkedList<Map>();

        Map<String, Object>node = root;
        Map<String, Object>nextNode = root;
        String key = "";

        /* Find the subtree where the search for suggests should start */
        for(int i = 0; i < query.length(); i++) {
            key += query.charAt(i);
            nextNode = (Map)node.get(key);
            if(nextNode != null) {
                node = nextNode;
            }
            else {
                //the given prefix ist not in the tree
                return Collections.EMPTY_LIST;
            }
        }

        /* Performs Breadth First Search (BFS) to find all leafs in the subtree.
           The leafs are the found suggests. */
        queue.add(node);

        while(!queue.isEmpty()) {
            node = queue.removeFirst(); //to change the impl. to DFS just use removeLast!
            for(Object o : node.values()) {
                if(o instanceof String) {
                    list.add((String)o);
                }
                if(o instanceof HashMap) {
                    queue.add((Map)o);
                }
            }
        }

        return list;
    }

   /* public static void main(String[] args) {
        PrefixTree t = new PrefixTree();
        t.add("TestXYZ");
        t.add("TestABC");
        t.add("Tesa");
        t.add("X12");
        t.add("Abcd");

        for(String test : t.getSuggestions("Test", 1000)) System.out.println(test);
    }*/
}
