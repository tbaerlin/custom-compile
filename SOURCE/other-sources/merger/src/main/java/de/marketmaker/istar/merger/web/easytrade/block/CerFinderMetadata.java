/*
 * CerFinderMetadata.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Map;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * Provides meta data and their available values that can be used in {@see CER_Finder} for
 * searching certificates.
 * <p>
 * Each entry of meta data is given by a list of key/name pairs. Additionally an attribute
 * <code>enum</code> indicates whether the values listed by the keys are considered to be enums. A
 * query on meta data of enum values conforms with enum match, e.g. part of string match won't be
 * considered as a valid query result, i.e. a query with <code>issuerCategory=='Finanzinstitute'</code>
 * delivers only certificates whose issuers are categorized in "Finanzinstitute", not those certificates
 * whose issuers are categorized in "Finanzinstitute / Geschäftsbanken".
 * </p>
 * <p>
 * The results can be limited by following parameters:
 * <table border="1">
 * <tr><th>Name</th><th>Effect</th></tr>
 * <tr><td><code>underlyingType</code></td><td>Limit underlying names by the given underlying type</td></tr>
 * </table>
 * </p>
 * <p>
 * Values for certificate type are localized.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CerFinderMetadata extends AbstractFinderMetadata {
    public static class Command extends AbstractFinderMetadata.Command {
        private String underlyingType;

        /**
         * @return type of instrument which a certificate is based on.
         */
        public String getUnderlyingType() {
            return underlyingType;
        }

        public void setUnderlyingType(String underlyingType) {
            this.underlyingType = underlyingType;
        }

        public void setBasiswerttyp(String underlyingType) {
            setUnderlyingType(underlyingType);
        }
    }

    public CerFinderMetadata() {
        super(Command.class, InstrumentTypeEnum.CER,
                RatioDataRecord.Field.type,
                RatioDataRecord.Field.typeKey,
                RatioDataRecord.Field.subtype,
                RatioDataRecord.Field.postbankType,
                RatioDataRecord.Field.certificateType,
                RatioDataRecord.Field.certificateTypeDZBANK,
                RatioDataRecord.Field.underlyingName,
                RatioDataRecord.Field.issuername,
                RatioDataRecord.Field.leverageType);
    }

    protected void onDoHandle(Object o, Map<String, Object> model) {
        final Command cmd = (Command) o;

        if (StringUtils.hasText(cmd.getUnderlyingType())) {
            final Map<String, Map<String, Integer>> nameByType =
                    this.ratiosProvider.getMetaData(InstrumentTypeEnum.CER,
                            RatioFieldDescription.underlyingType.id(),
                            RatioFieldDescription.underlyingName.id(), false);

            model.put("underlyingName", getKeyList(nameByType, cmd.getUnderlyingType()));
        }

        localize(model, RatioDataRecord.Field.certificateType.name());
        localize(model, RatioDataRecord.Field.typeKey.name());
    }
}