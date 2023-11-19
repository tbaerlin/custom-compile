package de.marketmaker.istar.merger.provider;

import java.sql.Types;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.SqlFunction;
import org.springframework.jdbc.object.SqlUpdate;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IntradayReportingDaoDb extends JdbcDaoSupport implements IntradayReportingDao {

    private static final Logger logger = LoggerFactory.getLogger(IntradayReportingDaoDb.class);

    private InsertRealtimeAccess insertRealtimeAccess;
    private SelectCountSelector selectCountSelector;

    private class InsertRealtimeAccess extends SqlUpdate {
        public InsertRealtimeAccess() {
            super(getDataSource(), "INSERT INTO realtimeaccesses " +
                    "(accessdate, profilename, quoteid, vwdsymbol, selector)" +
                    " VALUES (now(), ?, ?, ?, ?)");
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        public void insert(Quote quote, Profile profile) {
            this.update(profile.getName(),
                    quote.getId(),
                    quote.getSymbolVwdfeed(),
                    getEntitlements(quote));
        }

        private String getEntitlements(Quote quote) {
            final String[] entitlements = quote.getEntitlement().getEntitlements(KeysystemEnum.VWDFEED);
            if (entitlements.length == 1) {
                return entitlements[0];
            }

            final StringBuilder sb = new StringBuilder(entitlements.length * 3);
            for (String entitlement : entitlements) {
                sb.append(entitlement);
            }
            return sb.toString();
        }
    }

    private class SelectCountSelector extends SqlFunction {

        public SelectCountSelector() {
            super(getDataSource(), "SELECT count(*) FROM realtimeaccesses "
                    + "WHERE accessdate >= ? AND accessdate <= ? AND selector LIKE ?");
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        protected int getCount(String selector, DateTime from, DateTime to) {
            return run(from.toDate(), to.toDate(), selector);
        }
    }

    protected void initDao() throws Exception {
        super.initDao();

        this.insertRealtimeAccess = new InsertRealtimeAccess();
        this.selectCountSelector = new SelectCountSelector();
    }

    @Override
    public void insertAccess(Quote quote, Profile profile) {
        try {
            this.insertRealtimeAccess.insert(quote, profile);
        } catch (Exception e) {
            logger.error("<insertAccess> failed for quote={}, profile={}", quote, profile, e);
        }
    }

    public int getCount(String selector, DateTime from, DateTime to) {
        return this.selectCountSelector.getCount(selector, from, to);
    }
}
