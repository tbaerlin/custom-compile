/*
 * UserDbCli.java
 *
 * Created on 26.09.11 10:50
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.object.BatchSqlUpdate;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.instrument.export.InstrumentDirDao;

/**
 * Utility for manipulating a merger user database
 * Usage: UserDbCli [options] command [command-options]
 * options are:<ul>
 *     <li>-h database host, default localhost</li>
 *     <li>-url database url, default </li>
 *     <li>-h database host, "jdbc:mysql://localhost/merger"</li>
 *     <li>-u username, default merger</li>
 *     <li>-p password, default merger</li>
 * </ul>
 * command is one of the folowing:<ul>
 *     <li>addInstrumenttypes<br>command-options:
 *     <ul>
 *         <li>name of instrument data dir for InstrumentDirDao</li>
 *     </ul>
 *     </li>
 * </ul>
 * @author oflege
 */
public class UserDbCli {
    public static void main(String[] args) throws Exception {
        String driver = "com.mysql.jdbc.Driver";
        String host = "localhost";
        String url = null;
        String user = "merger";
        String password = "merger";

        int n = 0;
        while (n < args.length && args[n].startsWith("-")) {
            if ("-h".equals(args[n])) {
                host = args[++n];
            }
            else if ("-url".equals(args[n])) {
                url = args[++n];
            }
            else if ("-u".equals(args[n])) {
                user = args[++n];
            }
            else if ("-p".equals(args[n])) {
                password = args[++n];
            }
            else {
                throw new IllegalArgumentException("invalid argument " + args[n]);
            }
            n++;
        }

        if (url == null) {
            url = "jdbc:mysql://" + host + "/merger";
        }

        DriverManagerDataSource ds = new DriverManagerDataSource(url, user, password);
        ds.setDriverClassName(driver);

        if ("addInstrumenttypes".startsWith(args[n])) {
            addInstrumenttypes(ds, new File(args[++n]));
        }
    }

    private static void addInstrumenttypes(DataSource ds, File insDir) throws Exception {
        JdbcTemplate t = new JdbcTemplate(ds);
        t.afterPropertiesSet();

        InstrumentDirDao dao = new InstrumentDirDao(insDir);

        List iids = t.query("select distinct instrumentid from portfoliopositions where instrumenttype = 0", new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getLong(1);
            }
        });
        System.err.println("Found " + iids.size() + " iids");

        BatchSqlUpdate update = new BatchSqlUpdate(ds,
                "update portfoliopositions set instrumenttype = ? where instrumentid = ?",
                new int[]{Types.TINYINT, Types.INTEGER}, 10);


        int x = 0, y = 0;
        for (Object o : iids) {
            ++y;
            Long iid = (Long) o;
            Instrument instrument = dao.getInstrument(iid);
            if (instrument == null) {
                System.err.println(" no instrument for " + iid);
                continue;
            }
            update.update(new Object[] { instrument.getInstrumentType().getId(), instrument.getId()});
            if (++x == 10) {
                x = 0;
                System.err.println(y + "/" + iids.size());
                TimeUnit.SECONDS.sleep(2);
            }
        }
        update.flush();
        dao.close();
    }
}
