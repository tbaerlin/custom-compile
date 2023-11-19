/*
 * VwdFieldDescriptionGenerator.java
 *
 * Created on 08.09.2008 13:30:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.joda.time.DateTime;

import de.marketmaker.istar.common.util.LocalConfigProvider;

import static de.marketmaker.istar.common.util.LocalConfigProvider.getIstarSrcDir;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Folgende Schritte sind notwendig, um eine neue Fieldmap zu generieren:
 * <ol>
 * <li>Aus Fieldmap*.zip die Datei MDPS-Fieldmap*.txt extrahieren, z.B. per
 * <pre>
 * ZIP=...
 * DST=.../feed/src/conf/Fieldmap.csv
 * unzip -qqxc $ZIP 'MDPS-Fieldmap*.txt' | tr -d '\r' | sed 's/[ \t]*$//' | grep -v Umsatz_Boega >> $DST
 * </pre>
 * </li>
 * <li><tt>istar/feed/src/conf/fieldorders.txt</tt> anpassen: Felder hinzufügen, entfernen und
 * order vergeben, basierend auf zu erwartender Häufigkeit des Vorkommens des Feldes. Bestehende
 * order-Werte dürfen <b>nicht</b> geändert werden; falls notwendig (Feld wird wesentlich häufiger
 * als gedacht empfangen), kann eine weitere (kleinere) orderid vergeben werden (siehe z.B.
 * ADF_Mittelkurs). Damit bleiben dann die unter der alten oid gespeicherten Daten lesbar.<p>
 * News-Felder (NDB_...) benötigen keine order.
 * <li><tt>istar/feed/src/conf/staticfields.txt</tt> anpassen. Hier stehen die Namen aller Felder,
 * die als statisch angesehen werden sollen (i.e., Stammdaten), obwohl in Fieldmap.csv 'DYNAMIC' steht.
 * Statische Felder werden im Delay-Snap <b>nicht</b> gespeichert sondern bei bei einer Delay-Abfrage
 * aus dem Realtime-Snap dazugemischt.
 * <li><tt>VwdFieldDescriptionGenerator#main</tt> ausführen
 * </ol>
 * Erzeugt werden:
 * <ol>
 * <li>de.marketmaker.istar.feed.vwd.VwdFieldDescription</li>
 * <li>de.marketmaker.istar.feed.vwd.VwdFieldOrder</li>
 * </ol>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class VwdFieldDescriptionGenerator {

    VelocityEngine ve = new VelocityEngine();

    /**
     * For fields with these names, constants will be generated as
     * <pre>public static final int ID_ADF_<em>name_to_upper_case</em> = <em>id</em>;</pre>.
     * Those constants will be inlined into code that uses them and will therefore provide a
     * performance benefits over calling a field's <tt>id()</tt> method.
     * Makes sense for fields that are used over and over again
     * (e.g., looked up for each parsed record)
     */
    private final Set<String> intConstFields = new HashSet<>(Arrays.asList(
            "ADF_Schluss_Vortagesdatum"
            , "ADF_Boersenzeit"
            , "ADF_Handelsdatum"
            , "ADF_Block_Trade_Zeit"
            , "ADF_Quelle"
            , "ADF_Track"
            , "SOURCE_ID"
            , "BIG_SOURCE_ID"
            , "NDB_Flags"
    ));

    private final Set<String> intConstOrderFields = new HashSet<>(Arrays.asList(
            "BIG_SOURCE_ID"
    ));

    private final File confDir;

    static final Pattern DUMMY_FIELD
            = Pattern.compile("(DUMMY|_(PRICE|TIME|UNUM|SIZE|UCHAR)_?[0-9]+)");

    private static final Comparator<Field> BY_ORDER = new Comparator<Field>() {
        @Override
        public int compare(Field o1, Field o2) {
            return o1.order - o2.order;
        }
    };

    public class Field implements Comparable<Field> {
        int id;

        String name;

        String category = "";

        String type;

        String mdpsType;

        int length;

        int order = Integer.MIN_VALUE;

        boolean dynamic;

        boolean _static;

        boolean ratios;

        private Field(int id, String name, String type, String mdpsType,
                int length, boolean dynamic) {
            this(id, name, type, mdpsType, length, dynamic, !dynamic, false);
        }

        private Field(int id, String name, String type, String mdpsType,
                int length, boolean dynamic, boolean _static, boolean ratios) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.mdpsType = mdpsType;
            this.length = length;
            this.dynamic = dynamic;
            this._static = _static;
            this.ratios = ratios;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public int getId() {
            return id;
        }

        public boolean isDynamic() {
            return dynamic;
        }

        public String getFlags() {
            StringBuilder sb = new StringBuilder();
            if (this._static) append(sb, "STATIC");
            if (this.ratios) append(sb, "RATIO");
            if (this.dynamic) append(sb, "DYNAMIC");
            if (isNews()) append(sb, "NEWS");
            return sb.length() > 0 ? sb.toString() : "0";
        }

        private void append(StringBuilder sb, String name) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append("FLAG_").append(name);
        }

        public int getLength() {
            return length;
        }

        public String getName() {
            return name;
        }

        public int getOrder() {
            return order;
        }

        public String getMdpsType() {
            return mdpsType;
        }

        public String getType() {
            return type;
        }

        public int compareTo(Field o) {
            return this.id - o.id;
        }

        public boolean isNews() {
            return this.name.startsWith("NDB");
        }
    }

    private String version = "?";

    /**
     * names of fields that will be flagged with {@link VwdFieldDescription#FLAG_STATIC}
     * even if the fieldmap says DYNAMIC. Fields with this flag will not be stored in delayed
     * snap records as the values are retrieved from the realtime snap for each request.
     */
    private final List<String> staticFields = new ArrayList<>();

    /**
     * names of fields that will be flagged with {@link VwdFieldDescription#FLAG_RATIO}.
     * Fields with this flag will not be stored in delayed
     * snap records as the values are retrieved from the realtime snap for each request; therefore,
     * we only include that our ratios-backend process computes and the istar-ratios-mdpsexport
     * process exports so that they will be distributed in the mdps feed. The ratio data will only
     * be available in the feed after the delay time has already elapsed
     * TODO: what if the delay time exceeds the ratio computation roundtrip time?
     */
    private final List<String> ratioFields = new ArrayList<>();

    private final List<Field> fields = new ArrayList<>();

    private final Map<String, Field> fieldsByName = new HashMap<>();

    private final Map<Integer, Field> oldOrders = new TreeMap<>();

    private int maxOrder;

    private int maxId;

    public VwdFieldDescriptionGenerator() throws Exception {
        this.confDir = new File(getIstarSrcDir() + "/feed/src/conf");
        Properties p = new Properties();
        p.setProperty("file.resource.loader.path", confDir.getAbsolutePath());
        this.ve.init(p);
        readStaticFields();
        readRatioFields();
    }

    public static void main(String[] args) throws Exception {
        final VwdFieldDescriptionGenerator generator = new VwdFieldDescriptionGenerator();
        generator.generate();
    }

    private void generate() throws Exception {
        readFieldmap();
        addMmFields();
        readFieldorders();

        this.fields.sort(null);
        this.maxId = this.fields.get(this.fields.size() - 1).id + 1;
        generateFieldDescription();

        for (Iterator<Field> it = fields.iterator(); it.hasNext(); ) {
            Field f = it.next();
            if (f.order <= 0) {
                it.remove();
            }
        }

        fields.sort(BY_ORDER);
        generateFieldOrder();
    }

    private boolean isDummy(Field f) {
        return DUMMY_FIELD.matcher(f.name.toUpperCase()).find();
    }

    private void generateFieldDescription() throws Exception {
        VelocityContext context = new VelocityContext();
        context.put("datetime", new DateTime());
        context.put("intConstFields", getConstFields(this.intConstFields));
        context.put("fields", this.fields);
        context.put("version", this.version);
        context.put("maxid", this.maxId);
        Writer writer = new FileWriter(getIstarSrcDir() + "/feed/src/main/java/de/marketmaker/istar/feed/vwd/VwdFieldDescription.java");
        ve.getTemplate("fieldmap.vm").merge(context, writer);
        writer.close();
    }

    private List<Field> getConstFields(final Set<String> names) {
        ArrayList<Field> result = new ArrayList<>();
        for (Field field : fields) {
            if (names.contains(field.name)) {
                result.add(field);
            }
        }
        return result;
    }

    private void generateFieldOrder() throws Exception {
        VelocityContext context = new VelocityContext();
        context.put("datetime", new DateTime());
        context.put("fields", this.fields);
        context.put("intConstFields", getConstFields(this.intConstOrderFields));
        context.put("olds", this.oldOrders);
        context.put("version", this.version);
        context.put("maxorder", this.maxOrder + 1);
        context.put("maxid", this.maxId);
        context.put("firstNonTick", this.fieldsByName.get("ADF_Best_Bid_1").order);
        context.put("firstNonDynamic", getFirstNonDynamic());
        Writer writer = new FileWriter(getIstarSrcDir() + "/feed/src/main/java/de/marketmaker/istar/feed/vwd/VwdFieldOrder.java");
        ve.getTemplate("fieldorder.vm").merge(context, writer);
        writer.close();
    }

    private int getFirstNonDynamic() {
        for (Field field : fields) {
            if (!field.isDynamic() && field.getId() != 17) {
                return field.order;
            }
        }
        throw new IllegalStateException();
    }

    private void addField(Field f) {
        this.fields.add(f);
        this.fieldsByName.put(f.getName(), f);
    }

    private void addMmFields() {
        // internal
        addField(new Field(2, "ListingKey", "STRING", "VLSHSTRING", 255, false, false, false));
        addField(new Field(17, "BIG_SOURCE_ID", "UINT", "USHORT", 2, false, false, false));
        addField(new Field(19, "SOURCE_ID", "UINT", "UNDEFINED", 1, false, false, false));

        // needed for pages, inofficial for some reason
        addField(new Field(22, "ADF_CUSTOM_FID_22", "STRING", "VLLGSTRING", 65525, false, false, false));
        addField(new Field(23, "DB_SEC_TYPE", "STRING", "VLSHSTRING", 255, false, false, false));
        addField(new Field(24, "DB_DATA", "STRING", "VLSHSTRING", 255, false, false, false));

        addField(new Field(2000, "MMF_Zeit", "TIME", "UNDEFINED", 4, true));
        addField(new Field(2001, "MMF_Boersenzeit", "TIME", "UNDEFINED", 4, true));
        addField(new Field(2002, "MMF_Schluss_Vorvortagesdatum", "DATE", "UNDEFINED", 4, true));
        addField(new Field(2003, "MMF_Schluss_Vorvortag", "PRICE", "UNDEFINED", 8, true));
        addField(new Field(2004, "MMF_Created", "TIMESTAMP", "UNDEFINED", 8, false, false, false));
        addField(new Field(2005, "MMF_Deleted", "TIMESTAMP", "UNDEFINED", 8, false, false, false));
        addField(new Field(2006, "MMF_Updated", "TIMESTAMP", "UNDEFINED", 8, false, false, false));
        addField(new Field(2007, "MMF_Bezahlt_Datum", "DATE", "UNDEFINED", 4, true));
        addField(new Field(2008, "MMF_Iid_List", "STRING", "UNDEFINED", 255, false));
        addField(new Field(2009, "MMF_NAV_Vorvortag", "PRICE", "UNDEFINED", 8, true));

        // mdp timeseries fields -- TODO does the type matter, or do we need a general NUMBER type?
        addField(new Field(1500, "ADF_TIS_AT", "PRICE", "UNDEFINED", 8, true));
        addField(new Field(1501, "ADF_TID_AT", "PRICE", "UNDEFINED", 8, true));
        addField(new Field(1502, "ADF_Umlaufende_Anteile_Tranche", "PRICE", "UNDEFINED", 8, true));
        addField(new Field(1503, "ADF_Fondsvolumen_Tranche", "PRICE", "UNDEFINED", 8, true));
        addField(new Field(2249, "ADF_Umsatz_Boega", "PRICE", "UNDEFINED", 8, true));
        addField(new Field(2250, "ADF_TIS_CH", "PRICE", "UNDEFINED", 8, true));
        addField(new Field(2251, "ADF_TID_CH", "PRICE", "UNDEFINED", 8, true));
    }

    private void readFieldmap() throws Exception {

        Scanner s = new Scanner(new File(confDir, "Fieldmap.csv"));
        while (s.hasNextLine()) {
            final String line = s.nextLine().trim();
            if (line.startsWith("#")) {
                if (line.startsWith("# Version: ")) {
                    this.version = line.substring("# Version: ".length()).trim();
                }
                continue;
            }
            final String[] tokens = line.split("\\s+");
            if (tokens.length < 6) {
                continue;
            }
            final String type = toType(tokens[5], tokens[1]);
            final int length = getLength(tokens, type);
            final int id = Integer.parseInt(tokens[0]);
            final String name = toJavaIdentifier(tokens[1]);
            if (name.startsWith("INT_")) {
                continue;
            }
            boolean dummy = name.toLowerCase().contains("dummy") || name.contains("UNUM");
            boolean isStatic = !dummy && ("STATIC".equals(tokens[3]) || isStaticField(name));
            boolean isRatio = !dummy && isRatioField(name);
            boolean isDynamic = !dummy && !isStatic && !isRatio;

            addField(new Field(id, name, type, tokens[5], length, isDynamic, isStatic, isRatio));
        }
    }

    private boolean isStaticField(String name) {
        return anyMatch(name, this.staticFields);
    }

    private boolean isRatioField(String name) {
        if ("ADF_Benchmark_Margin".equals(name)) {
            return false;
        }
        return anyMatch(name, this.ratioFields);
    }

    private boolean anyMatch(String name, final List<String> fields) {
        return fields.stream().anyMatch((match) -> {
                    final String[] prefixSuffix = match.split(Pattern.quote("*"), 2);
                    return prefixSuffix.length == 1
                            ? (name.endsWith("*") ? name.startsWith(match) : name.equals(match))
                            : name.startsWith(prefixSuffix[0]) && name.endsWith(prefixSuffix[1]);
                }
        );
    }

    private int getLength(String[] tokens, String type) {
        if (isStringEncodedShort(tokens[1])) {
            return 2;
        }
        if (/*tokens.length > 6 && */"STRING".equals(type)) {
            if (tokens.length > 6) {
                return Integer.parseInt(tokens[6]);
            }
            else {
                System.err.println("String w/o length in " + Arrays.toString(tokens));
            }
        }
        return "PRICE".equals(type) ? 8 : 4;
    }

    private void readRatioFields() throws IOException {
        File dir = new File(LocalConfigProvider.getIstarSrcDir(), "config/src/main/resources/istar-ratios-mdpsexport/conf");
        File[] files = dir.listFiles(f -> f.getName().startsWith("fields-") && f.getName().endsWith(".prop"));
        Set<String> tmp = new HashSet<>();
        for (File file : files) {
            Files.lines(file.toPath())
                    .filter((line) -> line.startsWith("ADF"))
                    .forEach((line) ->
                                    tmp.add(line.substring(0, Math.max(line.indexOf(' '), line.indexOf('\t'))))
                    );
        }
        this.ratioFields.addAll(tmp);
    }

    private void readStaticFields() throws Exception {
        Files.lines(new File(this.confDir, "staticfields.txt").toPath(), UTF_8)
                .filter((line) -> line.startsWith("ADF"))
                .forEach((line) -> this.staticFields.add(line.trim()));
    }

    private void readFieldorders() throws Exception {
        Scanner s = new Scanner(new File(confDir, "fieldorders.txt"));
        boolean anyError = false;
        while (s.hasNextLine()) {
            final String line = s.nextLine().trim();
            if (line.startsWith("#")) {
                continue;
            }
            final String[] tokens = line.split("\\s+");
            if (tokens.length < 2) {
                continue;
            }
            final String name = toJavaIdentifier(tokens[0]);
            final int order = Integer.parseInt(tokens[1]);
            final Field f = this.fieldsByName.get(name);
            if (f == null) {
                System.err.println("Unknown: " + name);
                continue;
            }
            f.order = order;

            if (f.order == -1 && f.name.startsWith("ADF_") && !isDummy(f)) {
                System.err.println("No order for " + f.name);
                anyError = true;
            }


            this.maxOrder = Math.max(this.maxOrder, order);

            for (int i = 2; i < tokens.length; i++) {
                final Field existing = oldOrders.put(Integer.parseInt(tokens[i]), f);
                if (existing != null) {
                    System.err.println("old order " + tokens[i] + " used by " + f + " and " + existing);
                }
            }
        }
        s.close();
        if (anyError) {
            throw new IllegalStateException("readFildorders found Errors");
        }
    }

    private String toJavaIdentifier(String s) {
        return s.replace('-', '_');
    }

    private String toType(String type, String name) {
        if ("DATE".equals(type)) {
            return "DATE";
        }
        else if ("PRICE".equals(type)) {
            return "PRICE";
        }
        else if ("SIZE".equals(type)) {
            return "UINT";
        }
        else if ("TIME".equals(type)) {
            return "TIME";
        }
        else if ("USHORT".equals(type)) {
            return "USHORT";
        }
        else if ("FLSTRING".equals(type)) {
            // see MdpsParser#parseField for why this exception is necessary
            if (isStringEncodedShort(name)) {
                return "USHORT";
            }
            return "STRING";
        }
        else if ("VLLGSTRING".equals(type)) {
            return "STRING";
        }
        else if ("VLSHSTRING".equals(type)) {
            return "STRING";
        }
        throw new IllegalArgumentException("unknown type: " + type);
    }

    private boolean isStringEncodedShort(String name) {
        return "ADF_Status".equals(name) || "ADF_Tick".equals(name);
    }
}
