package de.marketmaker.istar.merger.exporttools.dpmanagergernerator;

import de.marketmaker.istar.common.util.XmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.*;

/**
 * User: pspriss
 * Date: 09.10.12
 * Time: 14:18
 */
public class DP2InSocieteGenerale implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String keyFileFonds;

    private String outFileFonds;

    private String keyFileRest;

    private String outFileRest;


    public void setKeyFileFonds(String keyFile) {
        this.keyFileFonds = keyFile;
    }

    public void setOutFileFonds(String outFile) {
        this.outFileFonds = outFile;
    }

    public void setKeyFileRest(String keyFile) {
        this.keyFileRest = keyFile;
    }

    public void setOutFileRest(String outFile) {
        this.outFileRest = outFile;
    }

    public static void main2(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("usage: DP2InSocieteGenerale <config-file>");
            System.exit(1);
        }

        final FileSystemXmlApplicationContext ac = new FileSystemXmlApplicationContext(args[0]);
        final DP2InSocieteGenerale g = (DP2InSocieteGenerale) ac.getBean("generator", DP2InSocieteGenerale.class);
        g.afterPropertiesSet();
        ac.destroy();
    }

    public void afterPropertiesSet() throws Exception {

        this.logger.info("<export> using keyfile " + this.keyFileFonds);
        this.logger.info("<export> writing to " + this.outFileFonds);
        writeFonds(this.keyFileFonds, this.outFileFonds);
        this.logger.info("<export> using keyfile " + this.keyFileRest);
        this.logger.info("<export> writing to " + this.outFileRest);
        writeRest(this.keyFileRest, this.outFileRest);
    }

    private void writeRest(String keyFile, String outFile) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(keyFile));
        int read = 0;
        int written = 0;
        OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(outFile));
        dp2HeaderRest(os);
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("NULL") || line.startsWith("CONCAT")) {
                continue;
            }
            read++;
            if (read % 1000 == 0) {
                this.logger.info("<afterPropertiesSet> read " + read);
            }
            String[] tokens = line.split(";");
            final String vendorKey = tokens[0];
            String[] symbol = vendorKey.split("\\.");
            final String suffix = symbol[0];
            final String platz = symbol[2];
            if (((suffix.equals("1") || suffix.equals("8")) && platz.equals("IT")) || (platz.equals("ITMOT") && suffix.equals("5"))) {
                final String text = line.substring(line.indexOf(".") + 1);
                written++;
                os.write("<vendorkey name=\"" + vendorKey + "\" alias=\"" + XmlUtil.encode(text) + "\"/>\n");
            }
        }
        dp2Footer(os);
        this.logger.info("<export> read " + read + " written " + written + " keys");
        os.close();


    }

    private void writeFonds(String keyFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(keyFile));
        int read = 0;
        int written = 0;
        OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(outFile));
        dp2HeaderFonds(os);
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("NULL") || line.startsWith("CONCAT")) {
                continue;
            }
            read++;
            if (read % 1000 == 0) {
                this.logger.info("<afterPropertiesSet> read " + read);
            }
            String[] tokens = line.split(";");
            final String vendorKey = tokens[0];
            final String text = line.substring(line.indexOf(".") + 1);
            written++;
            os.write("<vendorkey name=\"" + vendorKey + "\" alias=\"" + XmlUtil.encode(text) + "\"/>\n");

        }
        dp2Footer(os);
        this.logger.info("<export> read " + read + " written " + written + " keys");
        os.close();

    }

    public static void main(String[] args) throws Exception {
        DP2InSocieteGenerale d = new DP2InSocieteGenerale();
        if (args.length < 4) {
            //SELECT CONCAT(VENDORKEY,';',C49,';',C96,';',C48) from tab_static where C97='FONDIT';
            d.setKeyFileFonds("d:/produktion/var/data/societe_generale_fondi_allKeys.sql.txt");
            d.setOutFileFonds("d:/produktion/var/data/societe_generale_fondi.xml");
            //SELECT CONCAT(VENDORKEY,';',C49,';',C57) from tab_static where C97='IT';
            //SELECT CONCAT(VENDORKEY,';',C49,';',C57) from tab_static where C97='ITMOT';
            d.setKeyFileRest("d:/produktion/var/data/ societe_generale_altro_allKeys.sql.txt");
            d.setOutFileRest("d:/produktion/var/data/ societe_generale_altro.xml");

        } else {
            d.setKeyFileFonds(args[0]);
            d.setOutFileFonds(args[1]);
        }
        d.afterPropertiesSet();
    }

    private void dp2HeaderFonds(OutputStreamWriter os) throws IOException {
        os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<dp2spec>\n" +
                "<schedule>\n" +
                "<trigger>30 20 * * Mo-Fr</trigger>\n" +
                "</schedule>\n" +
                "<output>\n" +
                "<filePrefix>dp2out/societe_generale_fondi</filePrefix>\n" +
                "<fileSuffix>.csv</fileSuffix>\n" +
                "<fileTimestamp></fileTimestamp>\n" +
                "<fieldSeparator>;</fieldSeparator>\n" +
                "</output>\n" +
                "<fields>\n" +
                "<keyAlias>Symbol;Name;ISIN;Currency</keyAlias>\n" +
                "<endTag></endTag>\n" +
                "<field name=\"ADF_NAV\" alias=\"NAV\" />\n" +  //ADF68
                "<field name=\"ADF_Handelsdatum\" alias=\"Date\"/>\n" + //ADF109
                "</fields>\n" +
                "<vendorkeys>\n");
    }

    private void dp2HeaderRest(OutputStreamWriter os) throws IOException {
        os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<dp2spec>\n" +
                "<schedule>\n" +
                "<trigger>31 20 * * Mo-Fr</trigger>\n" +
                "</schedule>\n" +
                "<output>\n" +
                "<filePrefix>dp2out/societe_generale_altro</filePrefix>\n" +
                "<fileSuffix>.csv</fileSuffix>\n" +
                "<fileTimestamp></fileTimestamp>\n" +
                "<fieldSeparator>;</fieldSeparator>\n" +
                "</output>\n" +
                "<fields>\n" +
                "<keyAlias>Symbol;Name;ISIN</keyAlias>\n" +
                "<field name=\"ADF_Anfang\" alias=\"Open\"/>\n" +     //ADF67
                "<field name=\"ADF_Tageshoch\" alias=\"Day High\"/>\n" + //ADF53
                "<field name=\"ADF_Tagestief\" alias=\"Day Low\"/>\n" +  //ADF63
                "<field name=\"ADF_Official_Close\" alias=\"Official Close\"/>\n" + //ADF670
                "<field name=\"ADF_Umsatz_gesamt\" alias=\"Volume\"/>\n" +  //ADF83
                "<field name=\"ADF_Datum\" alias=\"Date\"/>\n" + //ADF25
                "<endTag></endTag>\n" +
                "</fields>\n" +
                "<vendorkeys>\n");
    }

    private void dp2Footer(OutputStreamWriter os) throws IOException {
        os.write("</vendorkeys>\n" +
                "</dp2spec>");
    }
}
