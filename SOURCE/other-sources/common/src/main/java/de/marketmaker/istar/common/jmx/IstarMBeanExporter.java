/*
 * MBeanExporter.java
 *
 * Created on 10.07.15 09:00
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.jmx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler;

/**
 * A {@link MBeanExporter} with sensible default configuration. Additionally, it is a
 * ManagedResource itself which exposes a single attribute, namely the "Version" of the program.
 * That is especially useful for webapps, so that a running tomcat can be queried for which version
 * of a webapp it is running with s.th. like <pre>
 * curl -su 'ttool:merger' 'http://localhost:8080/manager/jmxproxy/?get=de.marketmaker.istar:type=IstarMBeanExporter,webapp=dmxml-1&att=Version'
 * </pre>
 * @author oflege
 */
@ManagedResource
public class IstarMBeanExporter extends MBeanExporter {

    private static String parseVersion() {
        // the "version.txt" file will be created by the pack.py script in the devops repository
        // which is responsible for assembling programs/webapps for deployment
        final ClassPathResource cp = new ClassPathResource("/version.txt");
        try (InputStream is = cp.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            return br.readLine();
        } catch (IOException e) {
            return "unknown";
        }
    }

    private final ObjectName dataSourceNamePattern;

    private final String version;

    public IstarMBeanExporter() {
        setNamingStrategy(new IstarNamingStrategy());
        setAutodetect(true);
        final MetadataMBeanInfoAssembler assembler = new MetadataMBeanInfoAssembler();
        assembler.setAttributeSource(new AnnotationJmxAttributeSource());
        setAssembler(assembler);

        try {
            this.dataSourceNamePattern = IstarNamingStrategy.byType("BasicDataSource");
        } catch (MalformedObjectNameException e) {
            throw new IllegalStateException(e);
        }

        this.version = parseVersion();
    }

    @Override
    protected void doUnregister(ObjectName objectName) {
        if (this.dataSourceNamePattern.apply(objectName)) {
            // org.apache.commons.dbcp2.BasicDataSource instances want to unregister themselves
            // and log an error otherwise
            return;
        }
        super.doUnregister(objectName);
    }

    @ManagedAttribute
    public String getVersion() {
        return this.version;
    }
}
