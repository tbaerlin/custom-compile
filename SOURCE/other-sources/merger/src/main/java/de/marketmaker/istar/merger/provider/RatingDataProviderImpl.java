/*
 * RatingDataProviderImpl.java
 *
 * Created on 02.11.11 15:56
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.io.File;
import java.util.HashMap;

import com.google.protobuf.InvalidProtocolBufferException;

import de.marketmaker.istar.domain.data.RatingData;
import de.marketmaker.istar.merger.provider.protobuf.ProtobufDataReader;
import de.marketmaker.istar.merger.provider.protobuf.ProviderProtos;

/**
 * @author tkiesgen
 */
public class RatingDataProviderImpl extends ProtobufDataReader implements RatingDataProvider {
    public RatingDataProviderImpl() {
        super(ProviderProtos.RatingData.getDescriptor());
    }

    public RatingData getRatingData(long id) throws InvalidProtocolBufferException {
        ProviderProtos.RatingData.Builder builder = ProviderProtos.RatingData.newBuilder();
        if (!build(id, builder)) {
            return null;
        }
        return deserialize(id, builder.build());
    }

    private RatingData deserialize(long id, ProviderProtos.RatingData rd) {
        return RatingDataImpl.create(id,
                rd.getRatingFitchSt(), toLocalDate(rd.getRatingFitchStDate()), rd.getRatingFitchStAction(),
                rd.getRatingFitchLt(), toLocalDate(rd.getRatingFitchLtDate()), rd.getRatingFitchLtAction(),
                rd.getRatingFitchIssuerSt(), toLocalDate(rd.getRatingFitchIssuerStDate()), rd.getRatingFitchIssuerStAction(),
                rd.getRatingFitchIssuerLt(), toLocalDate(rd.getRatingFitchIssuerLtDate()), rd.getRatingFitchIssuerLtAction(),
                rd.getRatingMoodysSt(), toLocalDate(rd.getRatingMoodysStDate()), rd.getRatingMoodysStAction(),
                rd.getRatingMoodysLt(), toLocalDate(rd.getRatingMoodysLtDate()), rd.getRatingMoodysLtAction(),
                rd.getRatingSnpSt(), toLocalDate(rd.getRatingSnpStDate()), rd.getRatingSnpStAction(), rd.getRatingSnpStSource(),
                rd.getRatingSnpLt(), toLocalDate(rd.getRatingSnpLtDate()), rd.getRatingSnpLtAction(), rd.getRatingSnpLtSource(),
                rd.getRatingSnpLtRegulatoryid(), rd.getRatingSnpLtQualifier(),
                rd.getRatingSnpLocalLt(), toLocalDate(rd.getRatingSnpLocalLtDate()), rd.getRatingSnpLocalLtAction(), rd.getRatingSnpLocalLtSource(),
                rd.getRatingSnpLocalSt(), toLocalDate(rd.getRatingSnpLocalStDate()), rd.getRatingSnpLocalStAction(), rd.getRatingSnpLocalStSource());
    }

    @Override
    public RatingDataResponse getData(RatingDataRequest request) {
        try {
            final HashMap<Long, RatingData> map = new HashMap<>();
            for (Long iid : request.getIids()) {
                final RatingData data = getRatingData(iid);
                if (data != null) {
                    map.put(iid, data);
                }
            }
            return new RatingDataResponse(map);
        } catch (InvalidProtocolBufferException e) {
            this.logger.warn("<getData> failed", e);
            return new RatingDataResponse(null);
        }
    }

    public static void main(String[] args) throws Exception {
        RatingDataProviderImpl rdp = new RatingDataProviderImpl();
        rdp.setFile(new File("/home/vwdgroup.net/wiegelt/produktion/var/data/provider/istar-rating-data.buf.gz"));
        rdp.afterPropertiesSet();
        RatingData data = rdp.getData(new RatingDataRequest(211530185L)).getData(211530185L);
        System.out.println(data);
    }
}
