/*
 * CompanyFundamentalsProviderVwdIt.java
 *
 * Created on 29.09.2011 21:54:42
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.vwdit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.domain.data.ConvensysRawdata;
import de.marketmaker.istar.domainimpl.data.ConvensysRawdataImpl;
import de.marketmaker.istar.merger.provider.VwdItCompanyFundamentalsProvider;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CompanyFundamentalsProviderVwdIt implements VwdItCompanyFundamentalsProvider {

    public static final String ORG_PDF_PREFIX = "org_";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DateTimeFormatter dtf = DateTimeFormat.forPattern("dd-MM-yyyy");

    private File contentBasedir;

    private File rawdataBasedir;

    private File pdfBasedir;

    public void setContentBasedir(File contentBasedir) {
        this.contentBasedir = contentBasedir;
    }

    public void setRawdataBasedir(File rawdataBasedir) {
        this.rawdataBasedir = rawdataBasedir;
    }

    public void setPdfBasedir(File pdfBasedir) {
        this.pdfBasedir = pdfBasedir;
    }

    @Override
    public String getConvensysContent(String isin, String contentKey) {
        final File dir = new File(this.contentBasedir, contentKey);
        if (!dir.isDirectory()) {
            this.logger.warn("<getConvensysContent> no such directory: " + dir.getAbsolutePath());
            return null;
        }
        final File f = new File(dir, isin + ".txt");
        if (!f.canRead()) {
            this.logger.info("<getConvensysContent> no such file: " + f.getAbsolutePath());
            return null;
        }
        try {
            return Files.lines(Paths.get(f.getAbsolutePath())).collect(Collectors.joining("\n"));
        } catch (IOException e) {
            this.logger.warn("<getConvensysContent> failed for" + f.getAbsolutePath(), e);
            return null;
        }
    }

    @Override
    public Map<String,Object> getAdditionalInformation(String isin) {
        final ConvensysRawdataImpl c = new ConvensysRawdataImpl(null);
        try {
            addPdfFileAttributes(c, isin);
            readAttributes(c, pdfBasedir, isin);
        }
        catch(Exception e) {
            logger.warn("<getAdditionalInformation>", e);
        }
        return c.getAdditionalInformation();
    }

    private File getRawdataFile(String isin) {
        return new File(this.rawdataBasedir, isin + ".xml");
    }

    private File getPdfFile(String prefix, String isin) {
        return new File(this.pdfBasedir, prefix + isin + ".pdf");
    }

    private ConvensysRawdata readRawdata(String isin) {
        final File xmlFile = getRawdataFile(isin);
        if (!xmlFile.exists()) {
            return null;
        }

        try {
            final String s = FileUtils.readFileToString(xmlFile, "UTF-8");
            final ConvensysRawdataImpl rawdata = new ConvensysRawdataImpl(s);

            addPdfFileAttributes(rawdata, isin);
            readAttributes(rawdata, this.pdfBasedir, isin);

            return rawdata;
        } catch (Exception e) {
            this.logger.error("<readRawdata> failed", e);
            return null;
        }
    }

    private void addPdfFileAttributes(ConvensysRawdataImpl rawdata, String isin) {
        final File orgStructurePdfFile = getPdfFile(ORG_PDF_PREFIX, isin);
        if (orgStructurePdfFile.exists()) {
            rawdata.addAdditionalInformation("org_pdf_exists", true);
        }

        final File pdfFile = getPdfFile("", isin);
        if (pdfFile.exists()) {
            rawdata.addAdditionalInformation("pdf_exists", true);
        }
    }

    private void readAttributes(ConvensysRawdataImpl rawdata, File pdfBasedir,
            String isin) throws Exception {
        final SAXBuilder builder = new SAXBuilder();
        final Document document = builder.build(new File(pdfBasedir, "indice_studi.xml"));
        @SuppressWarnings({"unchecked"})
        final List<Element> items = document.getRootElement().getChild("Tabella").getChildren("Societa");
        for (final Element item : items) {
            if (isin.equals(item.getAttributeValue("Codice"))) {
                @SuppressWarnings({"unchecked"})
                final List<Element> attributes = item.getChildren("Valore");
                if (attributes.size() == 4) {
                    rawdata.addAdditionalInformation("pdf_sector", attributes.get(1).getTextTrim());
                    rawdata.addAdditionalInformation("pdf_market", attributes.get(2).getTextTrim());
                    final String dateStr = attributes.get(3).getTextTrim();
                    rawdata.addAdditionalInformation("pdf_date", dtf.parseDateTime(dateStr));
                    break;
                }
            }
        }
    }

    @Override
    public ConvensysRawdata getPortraitData(String isin) {
        return readRawdata(isin);
    }

    @Override
    public byte[] getOrganisationStructurePdf(String isin) {
        return getPdf(ORG_PDF_PREFIX, isin);
    }

    @Override
    public byte[] getPortraitPdf(String isin) {
        return getPdf("", isin);
    }

    private byte[] getPdf(String prefix, String isin) {
        final File pdfFile = getPdfFile(prefix,isin);
        if (!pdfFile.exists()) {
            return null;
        }

        try {
            return FileUtils.readFileToByteArray(pdfFile);
        } catch (Exception e) {
            this.logger.error("<getPortraitPdf> failed", e);
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        final CompanyFundamentalsProviderVwdIt cp = new CompanyFundamentalsProviderVwdIt();
        cp.setRawdataBasedir(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/vwdit/company/xml"));
        cp.setPdfBasedir(new File("d:/"));
        final ConvensysRawdata data = cp.getPortraitData("IT0001049623");
        System.out.println(data.getContent());
        System.out.println("exists: " + data.getAdditionalInformation().get("pdf-exists"));
        System.out.println("sector: " + data.getAdditionalInformation().get("pdf-sector"));
        System.out.println("market: " + data.getAdditionalInformation().get("pdf-market"));
        System.out.println("date: " + data.getAdditionalInformation().get("pdf-date"));

//        <bean id="vwditCompanyDataProvider" class="de.marketmaker.istar.merger.provider.vwdit.CompanyFundamentalsProviderVwdIt">
//        <property name="rawdataBasedir" value="${vwditCompanyDataProvider.rawdataBasedir}"/>
//        <property name="pdfBasedir" value="${vwditCompanyDataProvider.pdfBasedir}"/>
//        </bean>

    }
}
