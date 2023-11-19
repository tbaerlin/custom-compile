package de.marketmaker.istar.domainimpl.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.FileCopyUtils;

import de.marketmaker.istar.domain.data.ConvensysRawdata;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ConvensysRawdataImpl implements ConvensysRawdata, Serializable {
    protected static final long serialVersionUID = 1L;

    private final String rawContent;

    private Map<String, Object> additionalInformation;

    public ConvensysRawdataImpl(String rawContent) {
        this.rawContent = rawContent;
    }

    public String getContent() {
        return this.rawContent;
    }

    public String getContent(String xsdSource) {
        final StringBuilder stb = new StringBuilder(this.rawContent.length() + 200 + xsdSource.length());

        final int doctype = this.rawContent.indexOf("<!DOCTYPE");
        if (doctype < 0) {
            return this.rawContent;
        }
        final int doctypeEnd = this.rawContent.indexOf('>', doctype);

        final int start = this.rawContent.indexOf("<", doctypeEnd);
        final int endOfStart = this.rawContent.indexOf(">", start);

        return stb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>") // use hard-coded version as Convensys publishes a BOM at the beginning of the file and market manager cannot consume this
                .append(this.rawContent, start, endOfStart)
                .append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"")
                .append(xsdSource).append('"')
                .append(this.rawContent, endOfStart, this.rawContent.length()).toString();
    }

    public String toString() {
        return "ConvensysRawdataImpl["
                + (this.rawContent == null ? "null"
                : this.rawContent.substring(0, Math.min(this.rawContent.length(), 100)))
                + "]";
    }

    public void addAdditionalInformation(String key, Object value) {
        if (this.additionalInformation == null) {
            this.additionalInformation = new HashMap<>();
        }

        this.additionalInformation.put(key, value);
    }

    @Override
    public Map<String, Object> getAdditionalInformation() {
        if (this.additionalInformation == null) {
            return Collections.emptyMap();
        }
        return additionalInformation;
    }

    public static void main(String[] args) throws IOException {
        final ConvensysRawdataImpl rd = new ConvensysRawdataImpl(FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream("/Users/oflege/tmp/DE000A1EWWW0.xml"), "UTF-8")));
        System.out.println(rd.getContent("foo"));
    }
}
