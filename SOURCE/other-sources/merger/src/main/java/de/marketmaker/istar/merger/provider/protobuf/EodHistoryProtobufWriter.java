/*
 * EodHistoryProtobufWriter.java
 *
 * Created on 08.03.13 12:00
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.protobuf;

import java.sql.SQLException;
import java.util.Date;

import org.joda.time.LocalDate;

import de.marketmaker.istar.common.util.DateUtil;

/**
 * @author zzhao
 */
public class EodHistoryProtobufWriter extends ProtobufDataWriter {

    private int lastDateVar;

    public EodHistoryProtobufWriter() throws Exception {
    }

    @Override
    protected void beforeFirstRow() throws Exception {
        super.beforeFirstRow();
        this.lastDateVar = 0;
    }

    @Override
    protected Object getObject(int index) throws SQLException {
        final Object obj = super.getObject(index);
        if (null != obj && Date.class.isAssignableFrom(obj.getClass())) {
            final Date date = (Date) obj;
            final int yyyyMMdd = DateUtil.toYyyyMmDd(new LocalDate(date.getTime()));
            final int dateVar;
            if (this.lastDateVar == 0) {
                dateVar = yyyyMMdd;
            }
            else {
                dateVar = yyyyMMdd - this.lastDateVar;
            }
            this.lastDateVar = yyyyMMdd;
            return dateVar == 0 ? null : dateVar;
        }

        return obj;
    }
}
