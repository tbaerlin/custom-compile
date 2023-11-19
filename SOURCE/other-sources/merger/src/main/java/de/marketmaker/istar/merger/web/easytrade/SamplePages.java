/*
 * SampleForm.java
 *
 * Created on 21.06.2007 17:25:59
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.ZoneDispatcherServlet;
import de.marketmaker.istar.merger.web.ZoneInterceptor;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SamplePages extends AbstractController {

    private final Map<String, Map<String, Block>> blocksByZone = new HashMap<>();

    public void setBlocksByZone(Map<String, List<String>> blocksByZone) {
        for (final Map.Entry<String, List<String>> entry : blocksByZone.entrySet()) {
            final String zone = entry.getKey();

            final Map<String, Block> blocks = new HashMap<>();
            this.blocksByZone.put(zone, blocks);

            for (final String str : entry.getValue()) {
                final String[] definition = str.trim().split("#");
                final String name = definition[0];

                final Block block = new Block(name);
                blocks.put(name, block);

                if (definition.length == 1) {
                    block.add(new Element());
                    continue;
                }

                if (definition[1].endsWith(">")) {
                    block.setXml(definition[1].replaceAll("\\r?\\n", "\\\\n"));
                }
                else {
                    parseConfiguration(block, definition[1]);
                }
            }
        }
    }

    private void parseConfiguration(Block block, String str) {
        final String[] elements = str.replaceAll("\\r?\\n", "").split("\\|");
        for (final String element : elements) {
            final Element e = new Element();
            block.add(e);

            final String[] parameters = element.split(",");
            for (final String parameter : parameters) {
                e.add(parameter.split("=", 2));
            }
        }
    }

    private HttpServletRequest resolveRequest(HttpServletRequest request) {
        final HttpServletRequest zoneRequest = (HttpServletRequest)
                request.getAttribute(ZoneInterceptor.ZONE_REQUEST_ATTRIBUTE);
        return zoneRequest != null ? zoneRequest : request;
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        request = resolveRequest(request);

        final String authentication = request.getParameter("authentication");
        final String authenticationType = request.getParameter("authenticationType");
        final String selected = request.getParameter("selected");
        final String baseUrl = request.getParameter("baseUrl");
        final String namesStr = request.getParameter("blocks");
        final String[] names = StringUtils.hasText(namesStr) ? namesStr.split(",") : new String[0];

        final List<Boolean> selectList = new ArrayList<>();
        final List<Block> blockList = new ArrayList<>();

        final Map<String, Block> blocks = getBlocks(request);

        for (final String name : names) {
            final Block block = blocks.get(name);
            if (block == null) {
                this.logger.warn("<handleRequestInternal> unknown block: " + name);
                continue;
            }
            blockList.add(block);
            selectList.add(block.getName().equals(selected));
        }

        blockList.sort(null);

        final Map<String, Object> model = new HashMap<>();
        model.put("authentication", authentication);
        if (authenticationType != null) {
            model.put("authenticationType", authenticationType);
        }
        model.put("baseUrl", baseUrl);
        model.put("blockList", blockList);
        model.put("selectList", selectList);

        response.setContentType("text/html;charset=UTF-8");
        final String template = request.getParameter("template");
        return new ModelAndView(template, model);
    }

    private Map<String, Block> getBlocks(HttpServletRequest request) {
        final Zone zone = (Zone) request.getAttribute(ZoneDispatcherServlet.ZONE_ATTRIBUTE);

        final Map<String, Block> blocks = this.blocksByZone.get(zone.getName());
        if (blocks != null) {
            return blocks;
        }
        return this.blocksByZone.get("default");
    }

    public static class Block implements Comparable<Block> {
        private final String name;

        private String xml;

        private final List<Element> elements = new ArrayList<>();

        public Block(String name) {
            this.name = name;
        }

        public void add(Element element) {
            this.elements.add(element);
        }

        public String getName() {
            return name;
        }

        public List<Element> getElements() {
            return elements;
        }

        public String getXml() {
            return xml;
        }

        public void setXml(String xml) {
            this.xml = xml;
        }

        public boolean isWithXmlDefinition() {
            return this.xml != null;
        }

        @Override
        public int compareTo(Block o) {
            return this.name.compareTo(o.name);
        }
    }

    public static class Element {
        private final List<String> keys = new ArrayList<>();

        private final List<String> values = new ArrayList<>();

        public void add(String[] keyAndValue) {
            this.keys.add(keyAndValue[0]);
            this.values.add(keyAndValue[1]);
        }

        public List<String> getKeys() {
            return keys;
        }

        public List<String> getValues() {
            final ArrayList<String> result = new ArrayList<>(values.size());
            for (String value : values) {
                result.add(value.replaceAll("%DATE%", new LocalDate().toString()));
            }
            return result;
        }

        public List<Boolean> getComplex() {
            final List<Boolean> result = new ArrayList<>();
            for (final String value : values) {
                result.add(value.startsWith("<"));
            }
            return result;
        }

        public List<String> getValuesEncoded() {
            List<String> ve = new ArrayList<>();
            for (String v : getValues()) {
                try {
                    ve.add(URLEncoder.encode(v, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    ve.add(v);
                }
            }
            return ve;
        }
    }
}
