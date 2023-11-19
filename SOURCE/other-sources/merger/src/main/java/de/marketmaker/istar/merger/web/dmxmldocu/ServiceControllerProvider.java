/*
 * FndFindersuchergebnis.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.dmxmldocu;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.merger.web.dmxmldocu.jaxb.rawdoclet.RawDocletService;
import de.marketmaker.istar.merger.web.easytrade.block.AtomController;

/**
 * @author zzhao
 */
@MmInternal
public class ServiceControllerProvider extends WebApplicationObjectSupport
        implements AtomController, BeanNameAware {

    private Map<String, AtomController> serviceMap;

    private Map<String, String> services;

    private String beanName;

    private DmxmlJavadocRepository docRepository;

    public void setDocResource(DmxmlJavadocRepository docRepository) {
        this.docRepository = docRepository;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    protected void initApplicationContext(ApplicationContext context) {
        try {
            final Map atomControllers
                    = context.getBeansOfType(AtomController.class, false, false);
            atomControllers.remove(this.beanName); // remove this controller

            this.serviceMap = new TreeMap<>();
            for (Object o : atomControllers.entrySet()) {
                @SuppressWarnings("unchecked")
                final Map.Entry<String, AtomController> entry = (Map.Entry<String, AtomController>) o;
                if (!isMmInternal(entry.getValue())) {
                    this.serviceMap.put(entry.getKey(), entry.getValue());
                }
            }
            this.services = new TreeMap<>();
            for (Map.Entry<String, AtomController> entry : this.serviceMap.entrySet()) {
                final String blockName = entry.getKey();
                final RawDocletService service = this.docRepository.getServiceByName(entry.getValue().getClass().getName());
                if (service != null) {
                    this.services.put(blockName, service.getFirstSentence());
                } else {
                    this.logger.warn("<setApplicationContext> No documentation found for block " + blockName);
                }
            }
            this.logger.info("<setApplicationContext> service names extracted");
        } catch (Exception e) {
            this.logger.warn("<setApplicationContext> Error filling service name map", e);
        }
    }

    private boolean isMmInternal(AtomController value) {
        return value.getClass().isAnnotationPresent(MmInternal.class);
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        final HashMap<String, Object> model = new HashMap<>(5, 1f);
        model.put("services", this.services);
        return new ModelAndView("servicefinder", model);
    }

    /**
     * @return controller for the given bean name (= block name)
     */
    public AtomController getController(String name) {
        return this.serviceMap.get(name);
    }
}