/*
 * FndFindersuchergebnis.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.dmxmldocu;

import java.io.BufferedInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.merger.web.dmxmldocu.jaxb.repo.DmxmlBlockDocumentation;
import de.marketmaker.istar.merger.web.dmxmldocu.jaxb.repo.DmxmlDocumentationRepository;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeCommandController;

/**
 * Preliminary (non-zone-specific) documentation provider.
 * @author zzhao
 */
@MmInternal
public class ServiceDocumentationProvider extends EasytradeCommandController implements
        InitializingBean {

    private static final String VIEW_NAME = "servicedocfinder";

    private Resource blocksFile;

    private Unmarshaller unmarshaller;

    private Map<String, DmxmlBlockDocumentation> repository = new HashMap<>();

    public ServiceDocumentationProvider() {
        super(Command.class);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            final DmxmlDocumentationRepository repo;
            //noinspection SynchronizeOnNonFinalField
            synchronized (this.unmarshaller) {
                @SuppressWarnings("unchecked")
                final JAXBElement<DmxmlDocumentationRepository> element =
                        (JAXBElement<DmxmlDocumentationRepository>) unmarshaller.unmarshal(
                        new StreamSource(new BufferedInputStream(blocksFile.getInputStream())));

                repo =
                       element.getValue();
            }
            for (DmxmlBlockDocumentation blockDocumentation : repo.getDmxmlBlocks().getBlock()) {
                repository.put(blockDocumentation.getBlockName(), blockDocumentation);
            }
        } catch (Exception e) {
            this.logger.warn("<afterPropertiesSet> Could not load blocks file " + blocksFile, e);
        }
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        final Command cmd = (Command) command;
        final HashMap<String, Object> model = new HashMap<>(7);
        model.put("serviceName", cmd.getServiceName());
        final DmxmlBlockDocumentation blockDocumentation = this.repository.get(cmd.getServiceName());
        if (null != blockDocumentation) {
            model.put("description", blockDocumentation.getDescription());
            model.put("firstSentence", blockDocumentation.getFirstSentence());
            model.put("parameters", blockDocumentation.getParameters().getParameter());
        }

        return new ModelAndView(VIEW_NAME, model);
    }

    public void setBlocksFile(Resource blocksFile) {
        this.blocksFile = blocksFile;
    }

    public void setUnmarshaller(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    public static class Command {
        private String serviceName;

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }
    }
}