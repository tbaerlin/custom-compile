/*
 * DmDocResource.java
 *
 * Created on 07.03.12 10:39
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.dmxmldocu;

import java.io.BufferedInputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import de.marketmaker.istar.merger.web.dmxmldocu.jaxb.rawdoclet.RawDocletCommand;
import de.marketmaker.istar.merger.web.dmxmldocu.jaxb.rawdoclet.RawDocletRepository;
import de.marketmaker.istar.merger.web.dmxmldocu.jaxb.rawdoclet.RawDocletService;


/**
 * Reads the javadoc generated info about atom controller class and their command controller classes
 * and offers accessm to them by name.
 * @author zzhao
 */
public class DmxmlJavadocRepository implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Resource javadocXmlFile;

    private Unmarshaller unmarshaller;

    // all in memory?

    private Map<String, RawDocletService> serviceByName = new TreeMap<>();

    private Map<String, RawDocletCommand> commandByName = new TreeMap<>();

    /**
     * Get the javadoc information for controller class
     * <em>Warning: directly returns mutable unmarshalled object; do not modify.</em>
     * @param controllerClassName class name
     * @return javadoc information
     */
    public RawDocletService getServiceByName(String controllerClassName) {
        return serviceByName.get(controllerClassName);
    }

    /**
     * Get the javadoc information for command class
     * <em>Warning: directly returns mutable unmarshalled object; do not modify.</em>
     * @param commandClassName class name
     * @return javadoc information
     */
    public RawDocletCommand getCommandByName(String commandClassName) {
        return commandByName.get(commandClassName);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            final RawDocletRepository repository;
            //noinspection SynchronizeOnNonFinalField
            synchronized (this.unmarshaller) {
                @SuppressWarnings("unchecked")
                final JAXBElement<RawDocletRepository> element =
                        (JAXBElement<RawDocletRepository>) unmarshaller.unmarshal(
                        new StreamSource(new BufferedInputStream(
                                javadocXmlFile.getInputStream())));
                repository = element.getValue();
            }
            initServiceMap(repository.getServices().getService());
            initCommandMap(repository.getCommands().getCommand());
            this.logger.info("<afterPropertiesSet> javadoc repository successfully initialized");
        } catch (Exception e) {
            this.logger.warn("<afterPropertiesSet> Failed to load javadoc info from "
                    + javadocXmlFile.getDescription(), e);
        }
    }

    private void initCommandMap(List<RawDocletCommand> commands) {
        for (RawDocletCommand command : commands) {
            this.commandByName.put(command.getClassName(), command);
        }
        this.logger.info("<initCommandMap> service command map initialized");
    }

    public Resource getJavadocXmlFile() {
        return javadocXmlFile;
    }

    public void setJavadocXmlFile(Resource javadocXmlFile) {
        this.javadocXmlFile = javadocXmlFile;
    }

    private void initServiceMap(List<RawDocletService> services) {
        for (RawDocletService service : services) {
            this.serviceByName.put(service.getClassName(), service);
        }
        this.logger.info("<initServiceMap> service map initialized");
    }

    public Unmarshaller getUnmarshaller() {
        return unmarshaller;
    }

    public void setUnmarshaller(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }
}

