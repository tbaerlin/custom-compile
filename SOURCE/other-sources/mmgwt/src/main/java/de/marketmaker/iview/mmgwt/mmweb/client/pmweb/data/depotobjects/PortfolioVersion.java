/*
 * PortfolioVersion.java
 *
 * Created on 22.03.13 16:32
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.Formula;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkNodeMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMTable;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.*;

/**
 * @author Markus Dick
 */
public class PortfolioVersion extends PortfolioProfileBase {
    private String versionValidFromDate;
    private String name;
    private PortfolioProfile profile;
    private String profileKey;
    private String versionName;

    public static MmTalkWrapper<PortfolioVersion> createWrapper(Formula formula) {
        return createWrapper(MmTalkWrapper.create(formula, PortfolioVersion.class));
    }

    protected static MmTalkWrapper<PortfolioVersion> createWrapper(MmTalkWrapper<PortfolioVersion> cols) {
        cols = PortfolioProfileBase.appendMappers(cols);

        cols.appendColumnMapper(new MmTalkColumnMapper<PortfolioVersion>("EffectiveFrom") { // $NON-NLS$
            @Override
            public void setValue(PortfolioVersion pv, MM item) {
                pv.versionValidFromDate = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<PortfolioVersion>("Name") { // $NON-NLS$
            @Override
            public void setValue(PortfolioVersion pv, MM item) {
                pv.name = asString(item);
            }
        }).appendNodeMapper(new MmTalkNodeMapper<PortfolioVersion, PortfolioProfile>(PortfolioProfile.createWrapper("Profile")) { // $NON-NLS$     //TODO:Komplexes Objekt BenchmarkHistorie
            @Override
            public void setValue(PortfolioVersion pv, MmTalkWrapper<PortfolioProfile> pp, MMTable table) {
                pv.profile = pp.createResultObject(table);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<PortfolioVersion>("ProfileKey") { // $NON-NLS$
            @Override
            public void setValue(PortfolioVersion pv, MM item) {
                pv.profileKey = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<PortfolioVersion>("VersionsName") { // $NON-NLS$
            @Override
            public void setValue(PortfolioVersion pv, MM item) {
                pv.versionName = asString(item);
            }
        });

        return cols;
    }

    public String getVersionValidFromDate() {
        return versionValidFromDate;
    }

    public String getName() {
        return name;
    }

    public PortfolioProfile getProfile() {
        return profile;
    }

    public String getProfileKey() {
        return profileKey;
    }

    public String getVersionName() {
        return versionName;
    }

    public boolean hasProfile() {
        return profile != null;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PortfolioVersion)) return false;
        if (!super.equals(o)) return false;

        final PortfolioVersion that = (PortfolioVersion) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (profile != null ? !profile.equals(that.profile) : that.profile != null) return false;
        if (profileKey != null ? !profileKey.equals(that.profileKey) : that.profileKey != null) return false;
        if (versionName != null ? !versionName.equals(that.versionName) : that.versionName != null) return false;
        if (versionValidFromDate != null ? !versionValidFromDate.equals(that.versionValidFromDate) : that.versionValidFromDate != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (versionValidFromDate != null ? versionValidFromDate.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (profile != null ? profile.hashCode() : 0);
        result = 31 * result + (profileKey != null ? profileKey.hashCode() : 0);
        result = 31 * result + (versionName != null ? versionName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PortfolioVersion{" +  // $NON-NLS$
                "versionValidFromDate='" + versionValidFromDate + '\'' +  // $NON-NLS$
                ", name='" + name + '\'' + // $NON-NLS$
                ", profile=" + profile +  // $NON-NLS$
                ", profileKey='" + profileKey + '\'' + // $NON-NLS$
                ", versionName='" + versionName + '\'' +  // $NON-NLS$
                "} " + super.toString();
    }
}
