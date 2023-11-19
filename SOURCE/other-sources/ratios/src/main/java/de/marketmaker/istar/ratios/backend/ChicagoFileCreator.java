/*
 * ChicagoFileCreator.java
 *
 * Created on 07.09.11 12:20
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import net.jcip.annotations.NotThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.marketmaker.istar.common.util.FileUtil;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.common.xml.XmlWriter;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

/**
 * Creates input files for chicago that are used to create files with current prices for the
 * ratio calculator.<p>
 * Clients are expected to invoke
 * <ol>
 * <li>{@link #prepare(boolean)} as first step during an instrument update</li>
 * <li>{@link #add(de.marketmaker.istar.domain.instrument.InstrumentTypeEnum, de.marketmaker.istar.domain.instrument.Quote)} for all quotes for which prices are needed</li>
 * <li>{@link #createFiles(boolean)} after all quotes have been added</li> or
 * {@link #cancel()} if s.th. went terribly wrong.
 * </ol>
 * </p>
 * Avoids to store all keys and ids etc. in memory, which would also be difficult in the
 * case of instrument file updates. Instead, it maintains files in a temp directory that contain
 * <tt>instrumentid;quoteid;vwdcode</tt> for all quotes that are supposed to be added to the
 * chicago input files. New data is first added to temporary files before it replaces (update=false)
 * or is merged with (update=true) the data from the latest full instrument update.
 * @author oflege
 */
@NotThreadSafe
public class ChicagoFileCreator implements InitializingBean {

    private enum ChicagoExportType {
        PRICES(""),
        MDPSRATIOS("-mdpsratios");

        private final String fileSuffix;

        ChicagoExportType(String fileSuffix) {
            this.fileSuffix = fileSuffix;
        }

        public String getFileSuffix() {
            return fileSuffix;
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<InstrumentTypeEnum, PrintWriter> writers
            = new EnumMap<>(InstrumentTypeEnum.class);

    private File tempDir = new File("java.io.tmpdir");

    private File chicagoInputBaseDir;

    private File allowedFieldsBaseDir;

    private String cronExpression = "17 1-59/30 6-23 ? * *";

    private final Map<InstrumentTypeEnum, List<Integer>> fieldids =
            new EnumMap<>(InstrumentTypeEnum.class);

    private final Map<InstrumentTypeEnum, List<Integer>> fieldidsMdpsRatios =
            new EnumMap<>(InstrumentTypeEnum.class);

    private Set<InstrumentTypeEnum> types = EnumSet.noneOf(InstrumentTypeEnum.class);

    public ChicagoFileCreator() {
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public void setTempDir(File tempDir) {
        this.tempDir = tempDir;
    }

    public void setAllowedFieldsBaseDir(File allowedFieldsBaseDir) {
        this.allowedFieldsBaseDir = allowedFieldsBaseDir;
    }

    public void setChicagoInputBaseDir(File chicagoInputBaseDir) {
        this.chicagoInputBaseDir = chicagoInputBaseDir;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!this.tempDir.isDirectory() && !this.tempDir.mkdirs()) {
            throw new IllegalArgumentException("tempDir not usable: " + this.tempDir.getAbsolutePath());
        }
        this.logger.info("<afterPropertiesSet> using tempDir " + this.tempDir.getAbsolutePath());

        for (final InstrumentTypeEnum type : InstrumentTypeEnum.values()) {
            readFields(type, ChicagoExportType.PRICES, this.fieldids);
            readFields(type, ChicagoExportType.MDPSRATIOS, this.fieldidsMdpsRatios);
        }
        this.types.addAll(this.fieldids.keySet());
        this.types.addAll(this.fieldidsMdpsRatios.keySet());

        this.logger.info("<initialize> fieldIds by type: " + this.fieldids);
    }

    private void readFields(InstrumentTypeEnum type, ChicagoExportType cet,
            Map<InstrumentTypeEnum, List<Integer>> map) throws FileNotFoundException {
        final File file = new File(this.allowedFieldsBaseDir,
                "chicago-fields-" + type.name().toLowerCase() + cet.getFileSuffix() + ".conf");

        if (!file.exists()) {
            return;
        }

        try (Scanner scanner = new Scanner(file)) {
            final List<Integer> fieldids = new ArrayList<>();
            while (scanner.hasNextLine()) {
                final String name = scanner.nextLine();
                final VwdFieldDescription.Field field = VwdFieldDescription.getFieldByName(name);
                if (field != null) {
                    fieldids.add(field.id());
                }
                else {
                    this.logger.warn("<readFields> unknown field '" + name + "' in " + file.getName());
                }
            }
            map.put(type, fieldids);
        }
    }

    public void prepare(boolean update) throws IOException {
        if (!this.writers.isEmpty()) {
            throw new IllegalStateException();
        }
        for (InstrumentTypeEnum type : types) {
            File file = getTempFileName(type, update);
            if (file.exists() && !file.delete()) {
                throw new IOException("could not delete " + file.getAbsolutePath());
            }
            this.writers.put(type, new PrintWriter(file));
        }
    }

    private File getTempFileName(InstrumentTypeEnum type, boolean update) {
        return new File(this.tempDir, type.name() + "-" + update + ".csv");
    }

    private File getFileName(InstrumentTypeEnum type) {
        return new File(this.tempDir, type.name() + ".csv");
    }

    public void add(InstrumentTypeEnum t, Quote q) {
        final PrintWriter pw = this.writers.get(t);
        if (pw != null) {
            pw.println(q.getInstrument().getId() + ";" + q.getId() + ";" + q.getSymbolVwdcode());
        }
    }

    private boolean rename(File from, File to) {
        if (!from.renameTo(to)) {
            this.logger.error("<rename> failed to rename " + from.getName() + " to " + to.getName());
            return false;
        }
        return true;
    }

    private EnumSet<InstrumentTypeEnum> consolidateKeyFiles(boolean update) {
        closeWriters();

        final EnumSet<InstrumentTypeEnum> result = EnumSet.noneOf(InstrumentTypeEnum.class);
        for (InstrumentTypeEnum type : types) {
            if (update) {
                if (mergeFiles(type)) {
                    result.add(type);
                }
            }
            else {
                final File typeFile = getFileName(type);
                FileUtil.deleteIfExists(typeFile);

                File newFile = getTempFileName(type, false);
                if (rename(newFile, typeFile)) {
                    result.add(type);
                }
            }
        }
        return result;
    }

    private boolean mergeFiles(InstrumentTypeEnum type) {
        final File typeFile = getFileName(type);
        if (!typeFile.canRead()) {
            this.logger.error("<mergeFiles> cannot read " + typeFile.getAbsolutePath());
            return false;
        }

        final File newFile = getTempFileName(type, true);
        if (newFile.length() == 0L) {
            this.logger.info("<mergeFiles> no data for " + type);
            FileUtil.deleteIfExists(newFile);
            return false;
        }

        final File tmp = getTempFileName(type, false);
        FileUtil.deleteIfExists(tmp);

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(tmp);
            final Set<String> iids = append(newFile, pw, null);
            append(typeFile, pw, iids);
            IoUtils.close(pw);
            pw = null;
            FileUtil.deleteIfExists(newFile);
            FileUtil.deleteIfExists(typeFile);
            return rename(tmp, typeFile);
        } catch (IOException e) {
            this.logger.error("<consolidateKeyFiles> failed", e);
            return false;
        } finally {
            IoUtils.close(pw);
        }
    }

    private Set<String> append(File f, PrintWriter pw,
            Set<String> iids) throws FileNotFoundException {
        final Set<String> result = (iids == null) ? new HashSet<>() : null;
        final Scanner scUpdate = new Scanner(f);
        while (scUpdate.hasNextLine()) {
            final String line = scUpdate.nextLine();
            final String iid = line.substring(0, line.indexOf(";"));
            if (result != null) {
                result.add(iid);
                pw.println(line);
            }
            else if (!iids.contains(iid)) {
                pw.println(line);
            }
        }
        scUpdate.close();
        return result;
    }

    private void closeWriters() {
        for (InstrumentTypeEnum type : this.types) {
            final PrintWriter pw = this.writers.get(type);
            IoUtils.close(pw);
        }
        this.writers.clear();
    }

    public void createFiles(boolean update) throws Exception {
        final EnumSet<InstrumentTypeEnum> validTypes = consolidateKeyFiles(update);

        for (InstrumentTypeEnum type : validTypes) {
            if (this.fieldids.containsKey(type)) {
                writeFile(type, this.fieldids.get(type), ChicagoExportType.PRICES);
            }
            if (this.fieldidsMdpsRatios.containsKey(type)) {
                writeFile(type, this.fieldidsMdpsRatios.get(type), ChicagoExportType.MDPSRATIOS);
            }
        }
    }

    public void cancel() {
        closeWriters();
    }

    private void writeFile(InstrumentTypeEnum type, List<Integer> fids, ChicagoExportType cet)
            throws Exception {
        final File inFile = getFileName(type);
        if (!inFile.canRead() || inFile.length() == 0) {
            this.logger.info("<writeFile> no data for " + type);
            return;
        }

        TimeTaker tt = new TimeTaker();

        final File outFile = new File(this.chicagoInputBaseDir, "ratiotool-"
                + type.name().toLowerCase() + cet.getFileSuffix() + ".xml");

        // writing to tmp and then renaming tmp to outFile ensures nobody
        // can access the result file before it is written completely
        final File tmp = new File(outFile.getParentFile(), outFile.getName() + ".tmp");
        if (tmp.exists() && !tmp.delete()) {
            throw new IOException("failed to delete " + tmp.getAbsolutePath());
        }

        final XmlWriter xmlWriter = new XmlWriter();
        xmlWriter.setCdataElements(new String[0]);
        xmlWriter.setEncoding("ISO-8859-1");
        xmlWriter.setFile(tmp);
        xmlWriter.setPrettyPrint(true);
        xmlWriter.setRootElement("dp2spec");
        xmlWriter.start();

        final Document document = xmlWriter.getDocument();
        addChicagoHeader(type, fids, cet, xmlWriter, document);
        xmlWriter.startElement("vendorkeys", null);

        int numVkeys = 0;

        try (Scanner sc = new Scanner(inFile)) {
            while (sc.hasNextLine()) {
                final String[] tokens = sc.nextLine().split(";", 3);
                final String instrumentid = tokens[0];
                final String quoteid = tokens[1];
                final String vwdcode = tokens[2];

                final Element vendorkey = document.createElement("vendorkey");
                vendorkey.setAttribute("name", vwdcode);

                switch (cet) {
                    case MDPSRATIOS:
                        vendorkey.setAttribute("alias", instrumentid + "," + quoteid);
                        break;
                    case PRICES:
                        vendorkey.setAttribute("alias", quoteid);
                        break;
                    default:
                        throw new IllegalArgumentException("unknown type: " + cet);
                }
                xmlWriter.writeNode(vendorkey);
                numVkeys++;
            }
        }

        xmlWriter.endElement("vendorkeys");
        xmlWriter.stop();

        if (outFile.exists() && !outFile.delete()) {
            throw new IOException("failed to delete " + outFile.getAbsolutePath());
        }
        if (!tmp.renameTo(outFile)) {
            throw new IOException("failed to rename " + tmp.getAbsolutePath() + " to " + outFile.getName());
        }

        this.logger.info("<writeFile>" + outFile.getAbsolutePath() + ", " + numVkeys + " keys, took " + tt);
    }

    private void addChicagoHeader(InstrumentTypeEnum type, List<Integer> fieldids,
            ChicagoExportType cet, XmlWriter xmlWriter, Document document) throws IOException {
        final Element schedule = document.createElement("schedule");
        final Element trigger = document.createElement("trigger");
        schedule.appendChild(trigger);
        trigger.appendChild(document.createTextNode(this.cronExpression));
        xmlWriter.writeNode(schedule);

        final Element onlyUpdated = document.createElement("onlyUpdated");
        onlyUpdated.appendChild(document.createTextNode("true"));
        xmlWriter.writeNode(onlyUpdated);

        final Element output = document.createElement("output");
        final Element filePrefix = document.createElement("filePrefix");
        output.appendChild(filePrefix);

        filePrefix.appendChild(document.createTextNode(getFilename(type, cet)));
        final Element fileSuffix = document.createElement("fileSuffix");
        output.appendChild(fileSuffix);
        fileSuffix.appendChild(document.createTextNode(".csv"));
        final Element fileTimestamp = document.createElement("fileTimestamp");
        output.appendChild(fileTimestamp);
        fileTimestamp.appendChild(document.createTextNode(""));
        xmlWriter.writeNode(output);

        final Element fields = document.createElement("fields");
        final Element keyAlias = document.createElement("keyAlias");
        fields.appendChild(keyAlias);
        keyAlias.appendChild(document.createTextNode(getIdFields(cet)));
        final Element endTag = document.createElement("endTag");
        fields.appendChild(endTag);
        endTag.appendChild(document.createTextNode("end"));

        for (final int fieldid : fieldids) {
            final Element field = document.createElement("field");
            field.setAttribute("name", VwdFieldDescription.getField(fieldid).name());
            field.setAttribute("alias", Integer.toString(fieldid));
            fields.appendChild(field);
        }
        xmlWriter.writeNode(fields);
    }

    private String getIdFields(ChicagoExportType cet) {
        switch (cet) {
            case MDPSRATIOS:
                return "instrumentid,quoteid";
            case PRICES:
                return "quote";
            default:
                throw new IllegalArgumentException("unknown type: " + cet);
        }
    }

    private String getFilename(InstrumentTypeEnum type, ChicagoExportType cet) {
        switch (cet) {
            case MDPSRATIOS:
                return "dp2out/ratiotool-mdps/mdps-ratios-" + type.name().toLowerCase();
            case PRICES:
                return "dp2out/ratiotool/ratio-prices-" + type.name().toLowerCase();
            default:
                throw new IllegalArgumentException("unknown type: " + cet);
        }
    }
}
