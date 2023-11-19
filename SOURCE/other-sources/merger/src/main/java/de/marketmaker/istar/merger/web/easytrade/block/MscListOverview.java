/*
 * StkStammdaten.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.iview.dmxml.ListOverviewColumn;
import de.marketmaker.iview.dmxml.ListOverviewType;
import de.marketmaker.istar.merger.provider.listoverview.ListOverviewProvider;
import de.marketmaker.istar.merger.util.JaxbContextCache;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@MmInternal
public class MscListOverview extends EasytradeCommandController {
    public MscListOverview() {
        super(Command.class);
    }

    protected ListOverviewProvider listOverviewProvider;

    public void setListOverviewProvider(ListOverviewProvider listOverviewProvider) {
        this.listOverviewProvider = listOverviewProvider;
    }

    public static class Command {
        private String id;
        private String variant;

        @NotNull
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getVariant() {
            return variant;
        }

        public void setVariant(String variant) {
            this.variant = variant;
        }
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final Command cmd = (Command) o;

        final ListOverviewType structure = this.listOverviewProvider.getStructure(RequestContextHolder.getRequestContext().getLocales(), cmd.getVariant());

        final Map<String, Object> model = new HashMap<>();
        model.put("xml", buildXml(structure));
        return new ModelAndView("msclistoverview", model);
    }

    private String buildXml(ListOverviewType structure) {
        try {
            final JAXBContext jc = JaxbContextCache.INSTANCE.getContext("de.marketmaker.iview.dmxml");
            final Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, XMLConstants.NULL_NS_URI);
            final StringWriter writer = new StringWriter();
            for (final ListOverviewColumn column : structure.getColumn()) {
                marshaller.marshal(new JAXBElement<>(new QName(XMLConstants.NULL_NS_URI, "column"),
                        ListOverviewColumn.class, column), writer);
                writer.write("\n");
            }

            writer.close();
            final StringBuffer buffer = writer.getBuffer();
            return buffer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}