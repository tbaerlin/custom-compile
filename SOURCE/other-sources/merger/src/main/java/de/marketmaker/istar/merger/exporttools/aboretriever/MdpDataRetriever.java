package de.marketmaker.istar.merger.exporttools.aboretriever;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.zip.GZIPOutputStream;

import javax.sql.DataSource;

import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MdpDataRetriever  {
    private DataSource dataSource;

    private File baseDir;

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void readData() throws Exception {
        this.baseDir.mkdir();
        readSubscriptionQuotes();
        readIidQidVwdsymbol();
    }

    private void readSubscriptionQuotes() {
        final JdbcTemplate jt = new JdbcTemplate(this.dataSource);
        jt.afterPropertiesSet();
        //noinspection unchecked
        jt.query("select * from subscriptionquote sq,quotesymbol qs where subscription in (select subscriptionid from subscription where subscriptiongroup=1) and sq.quote=qs.quote(+) and qs.keysystem(+)=2",
                new ResultSetExtractor() {
                    public Object extractData(
                            ResultSet rs) throws SQLException, DataAccessException {
                        try {
                            final PrintWriter writer = new PrintWriter(new GZIPOutputStream(new FileOutputStream(new File(baseDir, "subscription-quotes.txt.gz"))));
                            while (rs.next()) {
                                writer.write(rs.getString("subscription") + "," + Long.parseLong(rs.getString("quote")) + "," + rs.getString("symbol") + "\n");
                            }
                            writer.close();
                            return null;
                        }
                        catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    private void readIidQidVwdsymbol() {
        final JdbcTemplate jt = new JdbcTemplate(this.dataSource);
        jt.afterPropertiesSet();
        //noinspection unchecked
        jt.query("select securityid as iid, s.securitytype as type, s.name as name, quoteid as qid, qs.symbol as vwdsymbol " +
                "from security s,quotesymbol qs,quote q " +
                "where securityid=security and q.quoteid=qs.quote(+) and qs.keysystem(+)=2",
                new ResultSetExtractor() {
                    public Object extractData(
                            ResultSet rs) throws SQLException, DataAccessException {
                        try {
                            final PrintWriter writer = new PrintWriter(new GZIPOutputStream(new FileOutputStream(new File(baseDir, "iid-qid-vwdsymbol.txt.gz"))));
                            while (rs.next()) {
                                final String vwdsymbol = rs.getString("vwdsymbol");
                                writer.write(rs.getString("iid") + "\t" + rs.getString("qid") + "\t" + (vwdsymbol == null ? "-" : vwdsymbol)
                                        + "\t" + rs.getInt("type") + "\t" + rs.getString("name")
                                        + "\n");
                            }
                            writer.close();
                            return null;
                        }
                        catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    public static void main(String[] args) throws Exception {
        final FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext(args[0]);
        final MdpDataRetriever generator = (MdpDataRetriever) context.getBean("mdpDataRetriever");
        generator.readData();
        context.destroy();
    }
}