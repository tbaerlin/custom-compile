/*
 * AbstractDp2Reader.java
 *
 * Created on 27.02.12 09:15
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domainimpl.DomainContextImpl;
import de.marketmaker.istar.domainimpl.ItemWithSymbolsDp2;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.TreeSet;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @author oflege
 * @author mcoenen
 */
class AbstractDp2Reader {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final DataSource dataSource;

    protected final int fetchSize;

    protected final DomainContextImpl domainContext;

    protected final boolean keepDuplicates;

    public AbstractDp2Reader(DataSource dataSource, DomainContextImpl domainContext,
            boolean keepDuplicates, int fetchSize) {
        this.dataSource = dataSource;
        this.domainContext = domainContext;
        this.keepDuplicates = keepDuplicates;
        this.fetchSize = fetchSize;
    }

    protected String addSymbol(ResultSet rs, final ItemWithSymbolsDp2 item, final String columnName,
            final KeysystemEnum kse) throws SQLException {
        final String symbol = rs.getString(columnName);
        return addSymbol(item, kse, symbol);
    }

    protected String addSymbol(final ItemWithSymbolsDp2 item, final KeysystemEnum kse,
            final String symbol) {
        if (StringUtils.hasText(symbol)) {
            item.setSymbol(kse, symbol.trim());
        }
        return symbol;
    }

    protected boolean isColumnAvailable(ResultSetMetaData metaData,
            String columnName) throws SQLException {
        final int columnCount = metaData.getColumnCount();
        TreeSet<String> columnNames = new TreeSet<>();
        for (int i = 1; i <= columnCount; i++) {
            if (columnName.equalsIgnoreCase(metaData.getColumnName(i))) {
                this.logger.info("<isColumnAvailable> '" + columnName + "': yes");
                return true;
            }
            columnNames.add(metaData.getColumnName(i));
        }
        this.logger.info("<isColumnAvailable> '" + columnName + "' not in " + columnNames);
        return false;
    }
}
