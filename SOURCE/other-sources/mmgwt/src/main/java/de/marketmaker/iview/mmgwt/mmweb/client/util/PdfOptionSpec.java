package de.marketmaker.iview.mmgwt.mmweb.client.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author umaurer
 */
public class PdfOptionSpec {
    private final String linkFile;
    private Map<String, String> mapDefaultParameters;
    private final String guidefOptionsId;
    private Set<String> disabledSet = null;

    public PdfOptionSpec(String linkFile, String guidefOptionsId) {
        this(linkFile, null, guidefOptionsId);
    }

    public PdfOptionSpec(String linkFile, Map<String, String> mapDefaultParameters, String guidefOptionsId) {
        this.linkFile = linkFile;
        this.mapDefaultParameters = mapDefaultParameters;
        this.guidefOptionsId = guidefOptionsId;
    }

    public void setDefaultParameter(String id, String value) {
        _getMapDefaultParameters().put(id, value);
    }

    private Map<String, String> _getMapDefaultParameters() {
        if (this.mapDefaultParameters == null) {
            this.mapDefaultParameters = new HashMap<String, String>();
        }
        return this.mapDefaultParameters;
    }

    public String getLinkFile() {
        return linkFile;
    }

    public Map<String, String> getMapDefaultParameters() {
        return mapDefaultParameters;
    }

    public String getGuidefOptionsId() {
        return guidefOptionsId;
    }

    public boolean isWithOptions() {
        return this.guidefOptionsId != null || PdfOptionHelper.isDefaultOptionsAvailable();
    }

    public void setDisabled(String option, boolean disabled) {
        if (this.disabledSet == null) {
            this.disabledSet = new HashSet<String>();
        }
        if (disabled) {
            this.disabledSet.add(option);
        }
        else {
            this.disabledSet.remove(option);
        }
    }

    public boolean isDisabled(String option) {
        return this.disabledSet != null && this.disabledSet.contains(option);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PdfOptionSpec)) {
            return false;
        }
        final PdfOptionSpec spec = (PdfOptionSpec) obj;
        return equals(this.linkFile, spec.linkFile)
                && equals(this.guidefOptionsId, spec.guidefOptionsId)
                && (this.mapDefaultParameters == null
                ? spec.mapDefaultParameters == null
                : this.mapDefaultParameters.equals(spec.mapDefaultParameters));
    }

    private boolean equals(String s1, String s2) {
        return s1 == null ? s2 == null : s1.equals(s2);
    }
}
