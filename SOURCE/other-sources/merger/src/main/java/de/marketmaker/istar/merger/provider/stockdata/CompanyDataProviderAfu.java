package de.marketmaker.istar.merger.provider.stockdata;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.monitor.Resource;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.common.xml.AbstractSaxReader;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.AnnualReportData;
import de.marketmaker.istar.domain.data.CompanyProfile;
import de.marketmaker.istar.domainimpl.data.CompanyProfileImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CompanyDataProviderAfu implements CompanyDataProvider, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ActiveMonitor activeMonitor;

    private File afuDataFile;

    private final Map<Long, CompanyProfile> afuDataMap = new HashMap<>();

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setAfuDataFile(File afuDataFile) {
        this.afuDataFile = afuDataFile;
    }

    @Override
    public List<AnnualReportData> getAnnualReportData(CompanyDataRequest request) {
        return null;
    }

    @Override
    public CompanyProfile getCompanyProfile(CompanyDataRequest request) {
        return afuDataMap.get(request.getInstrumentId());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (Objects.nonNull(this.activeMonitor)) {
            final FileResource afuDataResource = new FileResource(this.afuDataFile);
            afuDataResource.addPropertyChangeListener(evt -> readAfuData());
            this.activeMonitor.addResources(new Resource[]{afuDataResource});

            this.logger.info("<afterPropertiesSet> updates enabled " + this.afuDataFile);
        }
        else {
            this.logger.warn("<afterPropertiesSet> updates disabled " + this.afuDataFile);
        }

        readAfuData();
    }

    private void readAfuData() {
        try {
            final TimeTaker tt = new TimeTaker();
            final AfuDataReader ar = new AfuDataReader();
            ar.read(this.afuDataFile);

            synchronized (this) {
                this.afuDataMap.clear();
                this.afuDataMap.putAll(ar.getValues());
            }

            this.logger.info("<readAfuData> read " + this.afuDataFile + ", took " + tt);
        } catch (Exception e) {
            this.logger.error("<readAfuData> failed", e);
        }
    }

    public static class AfuDataReader extends AbstractSaxReader {

        /**
         * Add two list properties for shareholder names and shares, before validating
         * and transforming those to the map of {@link CompanyProfile#getShareholders()}.
         */
        private class Builder extends CompanyProfileImpl.Builder {

            private List<String> shareholderNames;

            private List<BigDecimal> shareholderShares;

            public void setShareholderNames(List<String> names) {
                this.shareholderNames = names;
            }

            public void setShareholderShares(List<BigDecimal> shares) {
                this.shareholderShares = shares;
            }

            @Override
            public boolean isValid() {
                if (super.isValid() && hasSameShareholderSize()) {
                    this.setShareholders(createShareholderValues());
                    return true;
                }
                return false;
            }

            private boolean hasSameShareholderSize() {
                return Objects.nonNull(this.shareholderNames) && Objects.nonNull(this.shareholderShares)
                        && this.shareholderNames.size() == this.shareholderShares.size();
            }

            private List<CompanyProfile.Shareholder> createShareholderValues() {
                return IntStream.range(0, shareholderNames.size())
                        .mapToObj(i -> new CompanyProfileImpl.ShareholderImpl(shareholderNames.get(i), shareholderShares.get(i)))
                        .collect(Collectors.toList());
            }
        }

        private final Map<Long, CompanyProfile> values = new HashMap<>();

        private final String listSeparator = "__,__";

        private final Pattern pattern = Pattern.compile("([^_]*)__,__(\\d*)\\s(.*)");

        private Builder builder = new Builder();

        public void endElement(String uri, String localName, String tagName) throws SAXException {
            try {
                if (tagName.equals("ROW")) {
                    // i have finished a new row => process
                    storeFields();
                }
                else if (tagName.equals("INSTRUMENTID")) {
                    this.builder.setInstrumentid(getCurrentLong());
                }
                else if (tagName.equals("NAME")) {
                    this.builder.setName(getCurrentString());
                }
                else if (tagName.equals("PHONE")) {
                    this.builder.setTelephone(getCurrentString());
                }
                else if (tagName.equals("FAX")) {
                    this.builder.setFax(getCurrentString());
                }
                else if (tagName.equals("WEBSITE")) {
                    this.builder.setUrl(getCurrentString());
                }
                else if (tagName.equals("EMAIL")) {
                    this.builder.setEmail(getCurrentString());
                }
                else if (tagName.equals("PROFILE")) {
                    this.builder.setPortrait(getCurrentString(), Language.de);
                }
                else if (tagName.equals("PROFILE_EN")) {
                    this.builder.setPortrait(getCurrentString(), Language.en);
                }
                else if (tagName.equals("PROFILE_NL")) {
                    this.builder.setPortrait(getCurrentString(), Language.nl);
                }
                else if (tagName.equals("PROFILE_FR")) {
                    this.builder.setPortrait(getCurrentString(), Language.fr);
                }
                else if (tagName.equals("COUNTRY")) {
                    this.builder.setCountry(getCurrentString());
                }
                else if (tagName.equals("CURRENCY")) {
                    this.builder.setAnnualReportCurrency(getCurrentString());
                }
                else if (tagName.equals("ADRESS")) {
                    final Matcher m = this.pattern.matcher(getCurrentString());
                    if (m.matches()) {
                        this.builder.setStreet(m.group(1));
                        this.builder.setPostalcode(m.group(2));
                        this.builder.setCity(m.group(3));
                    }
                }
                else if (tagName.equals("SHAREHOLDER_NAMES")) {
                    this.builder.setShareholderNames(readDelimitedList(getCurrentString()));
                }
                else if (tagName.equals("SHAREHOLDER_PERCENTAGES")) {
                    this.builder.setShareholderShares(readDelimitedList(getCurrentString()).stream()
                            .map(v -> toDecimal(v))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));
                }
                else if (tagName.equals("MANAGERS")) {
                    readDelimitedList(getCurrentString()).stream()
                            .map(v -> new CompanyProfileImpl.BoardMemeberImpl(v, null, CompanyProfile.BoardMember.Job.MANAGEMENT))
                            .forEach(this.builder::add);
                }
                else if (tagName.equals("SUPERVISORS")) {
                    readDelimitedList(getCurrentString()).stream()
                            .map(v -> new CompanyProfileImpl.BoardMemeberImpl(v, null, CompanyProfile.BoardMember.Job.SUPERVISORY))
                            .forEach(this.builder::add);
                }
                else {
                    notParsed(tagName);
                }
            } catch (Exception e) {
                this.logger.error("<endElement> error in " + tagName, e);
                this.errorOccured = true;
            }
        }

        private List<String> readDelimitedList(String value) {
            final String[] names = StringUtils.delimitedListToStringArray(value, this.listSeparator);
            return Arrays.stream(names).filter(StringUtils::hasText).collect(Collectors.toList());
        }

        /**
         * A bit expensive in case of NFE due to stack unwinding.
         */
        private BigDecimal toDecimal(String value) {
            try {
                return new BigDecimal(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        private void storeFields() {
            this.limiter.ackAction();

            if (this.errorOccured) {
                reset();
                return;
            }

            if (this.builder.isValid()) {
                final CompanyProfileImpl data = this.builder.build();
                this.values.put(data.getInstrumentid(), data);
            }

            reset();
        }

        protected void reset() {
            this.builder = new Builder();
            this.errorOccured = false;
        }

        public Map<Long, CompanyProfile> getValues() {
            return values;
        }
    }
}
