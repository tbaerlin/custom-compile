/*
 * Starter.java
 *
 * Created on 25.10.2004 14:16:47
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.lifecycle;

import de.marketmaker.istar.common.spring.ApplicationObjectSupport;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import de.marketmaker.istar.common.Constants;

/**
 * Once we start a java application that runs a spring ApplicationContext, we need a way to detect
 * that it has actually started, which may actually take several minutes. This component creates
 * a file once it receives a {@link org.springframework.context.event.ContextRefreshedEvent} and
 * deletes that file on {@link org.springframework.context.event.ContextClosedEvent}. The file
 * can either be set explicitly or, if not set, <code>${java.io.tmpdir}/&lt;pid&gt;.started</code>
 * will be used, where <em>pid</em> is the process id of the java process.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StartFileCreator extends ApplicationObjectSupport implements ApplicationListener {

    private File f;

    public void setFile(File f) {
        this.f = f;
    }

    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if ((this.f != null && this.f.canRead())
                || applicationEvent.getSource() != getApplicationContext()) {
            return;
        }
        if (applicationEvent instanceof org.springframework.context.event.ContextStartedEvent) {
            createFile();
        }
        else if (applicationEvent instanceof ContextClosedEvent) {
            removeFile();
        }
    }

    private void removeFile() {
        if (!this.f.exists()) {
            this.logger.warn("<removeFile> no such file: " + this.f.getAbsolutePath());
            return;
        }
        if (!this.f.delete()) {
            this.logger.error("<removeFile> failed for " + this.f.getAbsolutePath());
            return;
        }
        this.logger.info("<removeFile> " + this.f.getAbsolutePath());
    }

    private void createFile() {
        if (this.f == null) {
            this.f = new File(System.getProperty("java.io.tmpdir"), Constants.PID + ".started");
            this.logger.info("<createFile> " + this.f.getAbsolutePath());
        }
        try {
            Files.createFile(this.f.toPath());
        } catch (IOException e) {
            throw new RuntimeException("createFile failed for " + this.f.getAbsolutePath());
        }
        // just in case we don't get a ContextClosedEvent
        this.f.deleteOnExit();
    }

    public static void main(String[] args) {
        new StartFileCreator().createFile();
    }
}
