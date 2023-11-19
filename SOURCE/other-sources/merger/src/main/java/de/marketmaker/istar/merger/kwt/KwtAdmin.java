/*
 * KwtAdmin.java
 *
 * Created on 11.03.2009 14:12:03
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.kwt;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.Gson;
import de.marketmaker.istar.domain.util.IsinUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class KwtAdmin {
    protected Element root;

    private final Gson gson = new Gson();

    protected PrintWriter pw = new PrintWriter(System.out);

    private int numDumped = 0;

    private File inputFile = null;

    private File outputFile = null;

    private File dumpFile = null;

    private String urlStr = "http://dzbank.vwd.com/kwt_admin/?module=5&mode=xml&signon=cfe4527fe1c4a54d044b50ec1e22b02a";

    @SuppressWarnings("FieldCanBeLocal")
    private String zinspapiereUrl = "http://dzbank.vwd.com/kwt_admin/export/zinspapiere.csv";

    private File zinspapiereFile = null;

    private Map<String, String[]> zinspapiere = new HashMap<>();

    private static final String[] attrs = new String[]{"name", "url", "symbol", "note"};

    private static final String[] zinspapiereColNames = new String[]{
            "kupon", "text", "laufzeit", "isin", "flat1", "nettokurs", "duration",
            "mod-duration", "pvbp", "rendite", "kursdatum"
    };

    public KwtAdmin(String[] args) {
        int n = 0;
        while (n < args.length && args[n].startsWith("-")) {
            if ("-f".equals(args[n])) {
                this.inputFile = new File(args[++n]);
            } else if ("-o".equals(args[n])) {
                this.outputFile = new File(args[++n]);
            } else if ("-z".equals(args[n])) {
                this.zinspapiereFile = new File(args[++n]);
            } else if ("-d".equals(args[n])) {
                this.dumpFile = new File(args[++n]);
            } else if ("-u".equals(args[n])) {
                this.urlStr = args[++n];
            } else if ("-h".equals(args[n])) {
                usage();
            } else {
                usage();
            }
            n++;
        }
    }

    private void usage() {
        System.err.println("Usage: KwtAdmin [-f filename] [-o filename] [-d filename] [-u filename]");
        System.err.println("  -f : read input from file, not url");
        System.err.println("  -o : output file, if not specified: System.out");
        System.err.println("  -d : file to capture admintool output");
        System.err.println("  -u : url to use, default is " + urlStr);
        System.exit(-1);
    }

    /**
     * Use one item for every kind of node in the system
     */
    public static class Item implements Comparable<Item> {
        private String id;

        private String name;

        private String symbol;

        private String url;

        private String note;

        private int sorting;

        private List<Item> children = new ArrayList<>();

        public Item(String id) {
            this.id = id;
        }

        public int compareTo(Item o) {
            return this.sorting - o.sorting;
        }

        void add(Item item) {
            this.children.add(item);
        }

        public void sort() {
            this.children.sort(null);
            for (Item child : children) {
                child.sort();
            }
        }

        @Override
        public String toString() {
            return this.id + ", " + this.sorting + ", " + this.name + ", " + this.symbol + ", " + this.url;
        }

        public boolean hasChildrenWithSymbols() {
            return !this.children.isEmpty() && StringUtils.hasText(this.children.get(0).symbol);
        }

        public boolean isValidNavigationItem() {
            if (StringUtils.hasText(this.url) || StringUtils.hasText(this.symbol)) {
                return true;
            }
            for (Item child : children) {
                if (child.isValidNavigationItem()) {
                    return true;
                }
            }
            return false;
        }
    }


    public static void main(String[] args) throws Exception {
        new KwtAdmin(args).start();
    }

    private void start() throws Exception {
        final InputStream is = getInputStream();
        final byte[] bytes = FileCopyUtils.copyToByteArray(is);
        if (this.dumpFile != null) {
            if (this.dumpFile.exists() && !this.dumpFile.delete()) {
                System.err.println("ERROR: Failed to delete " + this.dumpFile.getAbsolutePath());
            }
            final RandomAccessFile raf = new RandomAccessFile(this.dumpFile, "rw");
            raf.write(bytes);
            raf.close();
        }
        final SAXBuilder builder = new SAXBuilder();
        final Document document = builder.build(new ByteArrayInputStream(bytes));
        root = document.getRootElement();

        readZinspapiere();

        if (this.outputFile != null) {
            this.pw = new PrintWriter(this.outputFile, "UTF-8");
        }

        pw.println("{");
        doNavigation();
        doFondsFactsheets();
        doPortalFonds();
        doFondsemittenten();
        doPortalZertifikate();
        doZertifikategruppen();
        doZertifikateemittenten();
        doKurslisten();
        doProductmap();
        doZinspapiere();
        pw.println("}");
        pw.flush();
        pw.close();
    }

    private void readZinspapiere() throws IOException {
        InputStream inputStream = getZinspapiereInputStream();
        Scanner sc = new Scanner(inputStream);
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] values = line.split("(\";|;\"|;)");
            if (values.length > 3 && IsinUtil.isIsin(values[3])) {
                this.zinspapiere.put(values[3], values);
            }
        }
        sc.close();
    }

    private InputStream getZinspapiereInputStream() throws IOException {
        if (this.zinspapiereFile != null) {
            return new FileInputStream(this.zinspapiereFile);
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(this.zinspapiereUrl).openConnection();
        final int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IllegalStateException("ResponseCode " + responseCode + " from " + this.zinspapiereUrl);
        }

        return connection.getInputStream();
    }

    private InputStream getInputStream() throws Exception {
        if (this.inputFile != null) {
            return new FileInputStream(this.inputFile);
        }
        return new URL(this.urlStr).openStream();
    }

    private void dumpData
            (Map<String, Item> map, String id) {
        if (numDumped++ > 0) {
            pw.print(", ");
        }
        final Item item = map.get("0");
        item.id = id;
        printNavigation(item, 0, false);
    }

    private void doNavigation() {
        final Map<String, Item> map = getItems("navigation", "idNavigation", "idParentNavigation");
        dumpData(map, "Kwtgui");
    }

    private void doFondsFactsheets() {
        final Map<String, Item> map = getItems("fondsfactsheets", "idFondsfactsheets", "idParentFondsfactsheets");
        getItems(map, "fondsfactsheetsUrls", "idFondsfactsheetsUrls", "idFondsfactsheets");
        for (Item item : map.values()) {
            if (item.symbol == null) {
                continue;
            }
            final int i = item.symbol.indexOf(".");
            if (i != -1) {
                item.symbol = item.symbol.substring(0, i);
            }
            item.id = item.symbol;
            item.symbol = null;
        }
        dumpData(map, "kwt_reports_list");
    }

    private void doPortalFonds() {
        final Map<String, Item> map = getItems("portalfonds", "idPortalfonds", "idParentPortalfonds");
        getItems(map, "portalfondsSymbole", "idPortalfondsSymbole", "idPortalfonds");
        dumpLists(map, "list_kwt_portal_fonds");
    }

    private void dumpLists(Map<String, Item> map, final String listname) {
        final Item root = map.get("0");
        for (int i = 0; i < root.children.size(); i++) {
            final Item child = root.children.get(i);
            if (child.children.isEmpty()) {
                continue;
            }
            dumpList(child, listname + (i + 1));
        }
    }

    private String quoted(String s) {
        return gson.toJson(s);
    }

    private void dumpList(Item child, final String listname) {
        pw.println(", " + quoted(listname) + " : { \"title\":" + quoted(child.name) + ", \"elements\": [");
        for (int j = 0; j < child.children.size(); j++) {
            final Item item = child.children.get(j);
            pw.print("  ");
            pw.print("{\"id\":" + quoted(item.symbol));
            if (StringUtils.hasText(item.name)) {
                pw.print(", \"name\":" + quoted(item.name));
            }
            if (StringUtils.hasText(item.note)) {
                pw.print(", \"note\":" + quoted(item.note));
            }
            if (StringUtils.hasText(item.url)) {
                pw.print(", \"url\":" + quoted(item.url));
            }
            pw.print("}");
            if (j < child.children.size() - 1) {
                pw.print(",");
            }
            pw.println();
        }
        pw.println("]}");
    }

    private void printLists(String prefix, Item root, final String listname,
            List<Item> itemsWithLists, int n) {
        pw.println(", " + quoted(listname) + ":[");
        for (int i = 0; i < root.children.size(); i++) {
            final Item child = root.children.get(i);
            indent(n);
            pw.print("  { \"id\":" + quoted(child.id) + ", \"title\":" + quoted(child.name));
            if (child.hasChildrenWithSymbols()) {
                itemsWithLists.add(child);
                pw.print(", \"listid\":\"" + getListName(prefix, child) + "\"}");
            } else if (!child.children.isEmpty()) {
                printLists(prefix, child, "elements", itemsWithLists, n + 1);
                indent(n + 1);
                pw.print("}");
            } else {
                pw.print("}");
            }
            if (i < (root.children.size() - 1)) {
                pw.print(",");
            }
            pw.println();
        }
        indent(n);
        pw.println("]");
    }

    private String getListName(String prefix, Item child) {
        return "productmap".equals(prefix)
                ? ("kwt_" + prefix + child.id)
                : ("list_kwt_" + prefix + child.id);
    }

    private void printLists(String prefix, List<Item> itemsWithLists) {
        for (final Item child : itemsWithLists) {
            dumpList(child, getListName(prefix, child));
        }
    }

    private void doKurslisten() {
        if (this.root.getChild("kurslisten") == null) {
            return;
        }
        final Map<String, Item> lists = getItems("kurslisten", "idKurslisten", "idParentKurslisten");
        getItems(lists, "kurslistenSymbole", "idKurslistenSymbole", "idKurslisten");
        List<Item> itemsWithLists = new ArrayList<>();
        printLists("kurslisten", lists.get("0"), "kwt_kurslisten", itemsWithLists, 0);
        printLists("kurslisten", itemsWithLists);
    }

    private void doProductmap() {
        if (this.root.getChild("produkte") == null) {
            return;
        }
        final Map<String, Item> lists = getItems("produkte", "idProdukte", "idParentProdukte");
        getItems(lists, "produkteSymbole", "idProdukteSymbole", "idProdukte");

        List<Item> itemsWithLists = new ArrayList<>();
        printLists("productmap", lists.get("0"), "kwt_productmap", itemsWithLists, 0);
        addUrls(itemsWithLists, getZinspapiereUrls());
        printLists("productmap", itemsWithLists);
    }

    private void addUrls(List<Item> itemsWithLists, Map<String, List<Item>> zinspapiereUrls) {
        for (Item item : itemsWithLists) {
            for (Item child : item.children) {
                final List<Item> listUrlItem = zinspapiereUrls.get(child.symbol);
                if (listUrlItem != null) {
                    final StringBuilder sb = new StringBuilder();
                    String divider = "";
                    for (Item urlItem : listUrlItem) {
                        String url = urlItem.url;
                        if (!url.matches("^https{0,1}://.*")) {
                            url = "http://" + url;
                        }
                        sb.append(divider).append("<a href=\"").append(url).append("\" target=\"_blank\">").append(urlItem.name).append("</a>");
                        divider = ", ";
                    }
                    child.url = sb.toString();
                }
            }
        }
    }

    private Map<String, List<Item>> getZinspapiereUrls() {
        if (this.root.getChild("zinspapiereUrls") == null) {
            return Collections.emptyMap();
        }
        final Map<String, List<Item>> result = new HashMap<>();
        for (Item item: getItems("zinspapiereUrls", "idZinspapiereUrls", "idParentZinspapiereUrls").values()) {
            if (StringUtils.hasText(item.symbol) &&
                    (StringUtils.hasText(item.name) || StringUtils.hasText(item.url))) {
                if (result.containsKey(item.symbol)) {
                    result.get(item.symbol).add(item);
                }
                else {
                    final List<Item> list = new ArrayList<>();
                    list.add(item);
                    result.put(item.symbol, list);
                }
            }
        }
        return result;
    }

    private void doZinspapiere() {
        pw.println(", " + quoted("zinspapiere") + ": {");
        int n = 0;
        for (Map.Entry<String, String[]> entry : zinspapiere.entrySet()) {
            pw.println("  " + quoted(entry.getKey()) + ": {");
            final String[] value = entry.getValue();
            for (int i = 0, j = Math.min(value.length, zinspapiereColNames.length); i < j; i++) {
                if (i > 0) pw.print(", ");
                pw.print(  quoted(zinspapiereColNames[i]) + ":" + quoted(value[i]));
            }
            pw.println(++n < this.zinspapiere.size() ? "}," : "}");
        }
        pw.println("}");
    }

    private void doFondsemittenten() {
        final Map<String, Item> map = getItems("fondsemittenten", "idFondsemittenten", "idParentFondsemittenten");
        dumpSimpleList(map, "kwt_fonds_issuer_list");
        dumpQuery(map, "kwt_fonds_issuer_query", "issuername");
    }

    private void doPortalZertifikate() {
        final Map<String, Item> map = getItems("portalzertifikate", "idPortalzertifikate", "idParentPortalzertifikate");
        getItems(map, "portalzertifikateSymbole", "idPortalzertifikateSymbole", "idPortalzertifikate");
        dumpLists(map, "list_kwt_portal_zerts");
    }

    private void doZertifikategruppen() {
        final Map<String, Item> map = getItems("zertifikategruppen", "idZertifikategruppen", "idParentZertifikategruppen");
        getItems(map, "zertifikategruppenSymbole", "idZertifikategruppenSymbole", "idZertifikategruppen");
        dumpData(map, "Kwtzertis");
    }

    private void doZertifikateemittenten() {
        final Map<String, Item> map = getItems("zertifikateemittenten", "idZertifikateemittenten", "idParentZertifikateemittenten");
        dumpSimpleList(map, "kwt_zert_issuer_list");
        dumpQuery(map, "kwt_zert_issuer_query", "issuername");
    }

    private void dumpSimpleList(Map<String, Item> map, final String name) {
        pw.println(", " + quoted(name) + " : [");
        pw.print("  ");
        final Item child = map.get("0");
        for (int j = 0; j < child.children.size(); j++) {
            if (j > 0) {
                pw.print(", ");
            }
            pw.print(quoted(child.children.get(j).name));
        }
        pw.println();
        pw.println("]");
    }

    private void dumpQuery(Map<String, Item> map, final String name, final String field) {
        StringBuilder sb = new StringBuilder(100);
        sb.append(field).append("=='");
        final Item child = map.get("0");
        for (int j = 0; j < child.children.size(); j++) {
            if (j > 0) {
                sb.append("@");
            }
            sb.append(child.children.get(j).name);
        }
        sb.append("'");

        pw.println(", " + quoted(name) + " : " + quoted(sb.toString()));
    }


    private void printNavigation(Item item, int n, boolean comma) {
        if (!item.isValidNavigationItem()) {
            return;
        }
        indent(n);

        if (n < 2) {
            pw.print(quoted(item.id) + " : ");
        }
        pw.print("{");
        if (n > 0) {
            printAttrs(item);
        } else {
            pw.println();
        }

        if (!item.children.isEmpty()) {
            for (int i = 0, m = item.children.size() - 1; i <= m; i++) {
                final Item child = item.children.get(i);
                printNavigation(child, n + 1, hasAnotherSibling(item.children, i + 1));
            }
            indent(n);
            if (n > 0) {
                pw.print("]");
            }
        }

        pw.print("}");
        if (comma) {
            pw.print(",");
        }
        pw.println();
    }

    private boolean hasAnotherSibling(List<Item> children, int i) {
        for (int j = i; j < children.size(); j++) {
            if (children.get(j).isValidNavigationItem()) {
                return true;
            }
        }
        return false;
    }

    private void printAttrs(Item item) {
        printAttrs(item, false);
    }

    private void printAttrs(Item item, boolean startComma) {
        final String[] values = {item.name, item.url, item.symbol, item.note};
        boolean comma = startComma;
        for (int i = 0; i < attrs.length; i++) {
            if (StringUtils.hasText(values[i])) {
                if (comma) pw.print(", ");
                comma = true;
                pw.print(quoted(attrs[i]) + ":" + quoted(values[i]));
            }
        }
        if (!item.children.isEmpty()) {
            if (comma) pw.print(", ");
            pw.println("\"children\":[");
        }
    }


    private void indent(int j) {
        for (int i = 0; i < j; i++) {
            pw.print("  ");
        }
    }

    private Map<String, Item> getItems(String tag, final String idTag, final String pidTag) {
        return getItems(null, tag, idTag, pidTag);
    }

    private Map<String, Item> getItems(Map<String, Item> parents,
                                       String tag, String idTag, String pidTag) {
        Map<String, Item> result = new HashMap<>();
        if (root.getChild(tag) == null) {
            return result;
        }
        @SuppressWarnings({"unchecked"})
        final List<Element> es = root.getChild(tag).getChildren();
        for (Element e : es) {
            final String id = e.getChildTextTrim(idTag);
            final String pid = e.getChildTextTrim(pidTag);
            Item item = getItem(result, id, true);
            final Item parent = getItem(parents != null ? parents : result, pid, parents == null);
            if (parent == null) {
                continue;
            }
            parent.add(item);
            item.name = e.getChildTextTrim("name");
            item.symbol = e.getChildTextTrim("symbol");
            item.note = e.getChildTextTrim("hinweis");
            item.url = e.getChildTextTrim("url");
            item.sorting = getSorting(e);
        }
        return result;
    }

    private int getSorting(Element e) {
        final String s = e.getChildTextTrim("sorting");
        return StringUtils.hasText(s) ? Integer.parseInt(s) : 0;
    }

    private Item getItem(Map<String, Item> map, String id, boolean create) {
        final Item parent = map.get(id);
        if (parent != null || !create) {
            return parent;
        }

        final Item result = new Item(id);
        map.put(id, result);
        return result;
    }
}
