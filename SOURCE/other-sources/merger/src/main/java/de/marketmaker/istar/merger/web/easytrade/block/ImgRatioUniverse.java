/*
 * FndInvestments.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.profile.PermissionType;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.merger.web.easytrade.chart.ImgRatioUniverseCommand;
import de.marketmaker.istar.ratios.RatioFieldDescription;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Returns the url of a chart that shows the distribution of instrument subtypes with
 * a certain instrument type.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@MmInternal
public class ImgRatioUniverse extends EasytradeCommandController {
    private RatiosProvider ratiosProvider;

    public ImgRatioUniverse() {
        super(ImgRatioUniverseCommand.class);
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final ImgRatioUniverseCommand cmd = (ImgRatioUniverseCommand) o;

        final InstrumentTypeEnum type = cmd.getType();

        final PermissionType pt = getPermissionType(type);

        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields
                = AbstractFindersuchergebnis.getFields(type, cmd.getProviderPreference(), pt);

        final RatioFieldDescription.Field field
                = AbstractFindersuchergebnis.getField(fields, cmd.getField());

        if (field == null || !field.isEnum()) {
            errors.reject("ImgRatiosUniverse.failed", "no enum field: " + cmd.getField());
            return null;
        }

        final Map<Object, Integer> counts
                = de.marketmaker.istar.merger.web.easytrade.chart.ImgRatioUniverse.getCounts(
                this.ratiosProvider, cmd.getProviderPreference(), type, field, cmd.getQuery(),
                cmd.getMinCount(), cmd.getNumElements());

        if (counts == null) {
            errors.reject("ImgRatiosUniverse.failed", "invalid search response");
            return null;
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("counts", counts);
        model.put("request", toRequest(cmd));
        return new ModelAndView("imgratiouniverse", model);
    }

    private PermissionType getPermissionType(InstrumentTypeEnum type) {
        if (type == InstrumentTypeEnum.FND) {
            return PermissionType.FUNDDATA;
        }
        return null;
    }

    private String toRequest(ImgRatioUniverseCommand cmd) {
        return new StringBuilder("ratioUniverse.png?").append(cmd.getParameterString()).toString();
    }
}