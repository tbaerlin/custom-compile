/*
 * MoleculeRequest.java
 *
 * Created on 04.07.2006 15:47:49
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MoleculeRequest implements InitializingBean {
    public static final String REQUEST_ATTRIBUTE_NAME
            = MoleculeRequest.class.getName() + ".moleculeRequest";

    public static final String REQUEST_TYPE_ATTRIBUTE_NAME
            = MoleculeRequest.class.getName() + ".requestType";

    public static final String VIEW_ATTRIBUTE_NAME
            = MoleculeRequest.class.getName() + ".moleculeView";

    private String key;

    private String authentication;

    private String authenticationType;

    private List<Locale> locales;

    private boolean withDependencies = false;

    private boolean skipRequestRendering = false;

    /**
     * total time spent on processing this request, includes rendering/sending the response,
     * expected to be filled in by the servlet that dispatches this request
     */
    private int ms = -1;

    private final List<AtomRequest> blocks = new ArrayList<>();

    private static final Comparator<AtomRequest> ATOM_COMPARATOR = (o1, o2) -> {
        final String did1 = o1.getDependsOnId();
        final String did2 = o2.getDependsOnId();

        if (did1 == null && did2 == null) {
            return 0;
        }

        if (did1 != null && did1.equals(o2.getId())) {
            return 1;
        }

        if (did2 != null && did2.equals(o1.getId())) {
            return -1;
        }

        return 0;
    };

    private static final Pattern XSI_TYPE = Pattern.compile("xsi:type=\"(.*?)\"");

    public static class ParameterInfo {
        private String key;

        private String value;

        // needed for gson, do not delete
        public ParameterInfo() {
        }

        public ParameterInfo(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public boolean isComplexValue() {
            return value.startsWith("<parameter") && value.endsWith("</parameter>");
        }

        public String getComplexType() {
            final Matcher m = XSI_TYPE.matcher(this.value);
            if (m.find()) {
                return m.group(1);
            }
            return null;
        }
    }

    public static class AtomRequest {
        private String id;

        private String name;

        private Map<String, String[]> parameters;

        private String dependsOnId;

        // transient to avoid gson serialization in molecule.log
        private transient AtomRequest dependsOn;

        /**
         * total time spent on executing this atom, does not include rendering/submitting
         * the response.
         * If it is still -1 after returning this means the result came from cache instead
         * of fetching it from backend.
         */
        private int ms = -1;

        /**
         * Time it took between this atom was prepared for processing and the point it actually started
         * being processed.
         */
        private int waitTime = -1;

        // needed for gson, do not delete
        public AtomRequest() {
        }

        public AtomRequest(String id, String name,
                Map<String, String[]> parameters, String dependsOnId) {
            if (id != null && dependsOnId != null && id.equals(dependsOnId)) {
                throw new BadRequestException("id == dependsOnId for " + name);
            }
            this.id = id;
            this.name = name;
            this.parameters = parameters;
            this.dependsOnId = dependsOnId;
        }

        public void set_HACK_Name(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public int getMs() {
            return ms;
        }

        public void setMs(int ms) {
            this.ms = ms;
        }

        public int getWaitTime() {
            return waitTime;
        }

        public void setWaitTime(int waitTime) {
            this.waitTime = waitTime;
        }

        public String getId() {
            return this.id;
        }

        public String getDependsOnId() {
            return dependsOnId;
        }

        public AtomRequest getDependsOn() {
            return dependsOn;
        }

        public Map<String, String[]> getParameterMap() {
            return this.parameters;
        }

        public List<ParameterInfo> getParameterInfos() {
            final List<ParameterInfo> result = new ArrayList<>(this.parameters.size());
            for (Map.Entry<String, String[]> e : this.parameters.entrySet()) {
                for (String s : e.getValue()) {
                    result.add(new ParameterInfo(e.getKey(), s));
                }
            }
            return result;
        }

        public String toString() {
            final StringBuilder sb = new StringBuilder(20 + this.parameters.size() * 20);
            sb.append(this.name).append("(");
            if (this.id != null) {
                sb.append("id=").append(this.id).append(", ");
            }
            if (this.dependsOnId != null) {
                sb.append("dependsOnId=").append(this.dependsOnId).append(", ");
            }
            sb.append("[");
            int n = 0;
            for (Map.Entry<String, String[]> entry : this.parameters.entrySet()) {
                if (n++ > 0) {
                    sb.append(", ");
                }
                sb.append(entry.getKey()).append(":").append(Arrays.toString(entry.getValue()));
            }
            sb.append("])");
            return sb.toString();
        }

        /**
         * Returns a key that can be used for caching the results of this atom<br>
         * <b>Important</b> the result is only a partial key, it has to be enhanced to distinguish
         * requests for different profiles.
         * @return cache key
         */
        public String getCacheKey() {
            final StringBuilder sb = new StringBuilder(20 + this.parameters.size() * 20);
            sb.append(this.name);
            final ArrayList<String> keys = new ArrayList<>(this.parameters.keySet());
            keys.sort(null);
            for (final String key : keys) {
                sb.append(",").append(key).append(":").append(Arrays.toString(this.parameters.get(key)));
            }
            return sb.toString();
        }
    }

    // needed for GSON, do not remove
    public MoleculeRequest() {
    }

    public MoleculeRequest(MoleculeRequest mr) {
        this.key = mr.key;
        this.authentication = mr.authentication;
        this.authenticationType = mr.authenticationType;
        this.locales = mr.locales;
        this.withDependencies = mr.withDependencies;
    }

    public void afterPropertiesSet() {
        for (AtomRequest block : this.blocks) {
            this.withDependencies |= (block.dependsOnId != null);
        }
        if (this.withDependencies) {
            this.blocks.sort(ATOM_COMPARATOR);
            assignDependencies();
            checkForCyclicDependencies();
        }
    }

    private void checkForCyclicDependencies() {
        for (AtomRequest block : this.blocks) {
            if (block.dependsOn == null) {
                continue;
            }
            AtomRequest tmp = block.dependsOn;
            while (tmp != null) {
                if (tmp == block) {
                    throw new BadRequestException("cyclic dependency: " + dependencyString(block));
                }
                tmp = tmp.dependsOn;
            }
        }
    }

    private String dependencyString(AtomRequest block) {
        StringBuilder sb = new StringBuilder(40);
        sb.append(block.getId());
        AtomRequest tmp = block.dependsOn;
        while (tmp != block) {
            sb.append(" => ").append(tmp.getId());
            tmp = tmp.dependsOn;
        }
        sb.append(" => ").append(block.getId());
        return sb.toString();
    }

    private void assignDependencies() {
        final Map<String, AtomRequest> byId = mapRequestsById();
        for (AtomRequest block : this.blocks) {
            if (StringUtils.hasText(block.getDependsOnId())) {
                final AtomRequest dependsOn = byId.get(block.getDependsOnId());
                if (dependsOn == null) {
                    throw new BadRequestException("block " + block.getId()
                            + ": dependsOn not found: " + block.getDependsOnId());
                }
                block.dependsOn = dependsOn;
            }
        }
    }

    private Map<String, AtomRequest> mapRequestsById() {
        final Map<String, AtomRequest> byId = new HashMap<>();
        for (AtomRequest block : blocks) {
            String blockId = block.getId();
            if (blockId == null) {
                continue;
            }
            final String[] ids = blockId.split(",");
            for (String id : ids) {
                byId.put(id, block);
            }
        }
        return byId;
    }

    public void addAtom(String id, String name, Map<String, String[]> parameterMap,
            String dependsOnId) {
        this.blocks.add(new AtomRequest(id, name, parameterMap, dependsOnId));
    }

    public boolean isWithDependencies() {
        return withDependencies;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public String getAuthentication() {
        return this.authentication;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public boolean isSkipRequestRendering() {
        return skipRequestRendering;
    }

    public void setSkipRequestRendering(boolean skipRequestRendering) {
        this.skipRequestRendering = skipRequestRendering;
    }

    public List<Locale> getLocales() {
        return locales;
    }

    public void setLocales(List<Locale> locales) {
        this.locales = locales;
    }

    public int getMs() {
        return ms;
    }

    public void setMs(int ms) {
        this.ms = ms;
    }

    public List<AtomRequest> getAtomRequests() {
        return Collections.unmodifiableList(this.blocks);
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(this.blocks.size() * 20);
        sb.append("Molecule[");
        for (int i = 0; i < this.blocks.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(this.blocks.get(i).getName());
        }
        sb.append("]");
        return sb.toString();
    }
}
