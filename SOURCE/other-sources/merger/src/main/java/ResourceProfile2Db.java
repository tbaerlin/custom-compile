import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.profile.PermissionType;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.provider.profile.ResourceProfileSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.util.BitSet;
import java.util.Collection;
import java.util.Set;


public class ResourceProfile2Db {

    static SingleConnectionDataSource ds = new SingleConnectionDataSource(
            "jdbc:mysql://neutron/fitusers?autoReconnect=true&amp;useUnicode=true&amp;characterEncoding=ISO-8859-1",
            "merger", "merger", true);
    static {
        ds.setDriverClassName("com.mysql.jdbc.Driver");
    }

    static JdbcTemplate jt = new JdbcTemplate(ds);

    public static void main(String[] args) {
        ResourceProfileSource rps = new ResourceProfileSource();
        Set<String> profileNames = null;
        try {
            profileNames = rps.loadProviders_Impl();
        }
        catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        for (String profileName : profileNames) {

            Profile profile = rps.getProfile("resource", profileName);
            System.out.println("profileName = " + profileName);
            System.out.println("FUNDDATA = " + profile.getPermission(PermissionType.FUNDDATA));
            System.out.println("FACTSET = " + profile.getPermission(PermissionType.FACTSET));
            System.out.println("QIDS_DELAY = " + profile.getPermission(PermissionType.QIDS_DELAY));
            System.out.println("---------------------------");
        }


//        System.exit(0);

        for (String profileName : profileNames) {
            Profile profile = rps.getProfile("resource", profileName);
            System.out.println(profile);
//            System.out.println("RT: " + EntitlementsVwd.asString(profile.toEntitlements(Profile.Aspect.PRICE, PriceQuality.REALTIME)));
//            System.out.println("RT: " + entBit2Str("realtimeplace", profile.toEntitlements(Profile.Aspect.PRICE, PriceQuality.REALTIME)));
//            System.out.println("NT: " + EntitlementsVwd.asString(profile.toEntitlements(Profile.Aspect.PRICE, PriceQuality.DELAYED)));
//            System.out.println("NT: " + entBit2Str("delayedplace", profile.toEntitlements(Profile.Aspect.PRICE, PriceQuality.DELAYED)));

            // Bis hier sind alle permissions angelegt
            // JETZT die permissionsets bauen

            profileName = profileName.concat("_db");

            int userid = selectIdOrZero("SELECT userid FROM users where login='" + profileName + "' and companyid=28 and productid=33");
            if (userid == 0) {
                jt.execute("INSERT INTO users (login, companyid, productid, externaluserid) VALUES ('" + profileName + "',28,33,'resource')"); //xml, xml
                userid = selectIdOrZero("SELECT userid FROM users where login='" + profileName + "'");
            }

            int profileid = selectIdOrZero("SELECT id FROM profile where description='" + profileName + "'");
            if (profileid == 0) {
                jt.execute("INSERT INTO profile (description) VALUES ('" + profileName + "')");
                profileid = selectIdOrZero("SELECT id FROM profile where description='" + profileName + "'");
            }

            int permissionsetid = selectIdOrZero("SELECT id FROM permissionset where description='" + profileName + "'");
            if (permissionsetid == 0) {
                jt.execute("INSERT INTO permissionset (description) VALUES ('" + profileName + "')");
                permissionsetid = selectIdOrZero("SELECT id FROM permissionset where description='" + profileName + "'");
            }


            jt.execute("DELETE FROM profile_users where profileid=" + profileid + " and userid=" + userid);
            jt.execute("INSERT INTO profile_users (profileid, userid, effectiveon) VALUES (" + profileid + "," + userid + ", '2009-01-01')");

            jt.execute("DELETE FROM permissionset_profile where permissionsetid=" + permissionsetid + " and profileid=" + profileid);
            jt.execute("INSERT INTO permissionset_profile (permissionsetid, profileid) VALUES (" + permissionsetid + "," + profileid + ")");

            jt.execute("DELETE FROM permission_permissionset where permissionsetid=" + permissionsetid);

            // JETZT permissions an p_ser haengen
            attach(permissionsetid, profile.toEntitlements(Profile.Aspect.PRICE, PriceQuality.REALTIME), "realtimeplace");
            attach(permissionsetid, profile.toEntitlements(Profile.Aspect.PRICE, PriceQuality.DELAYED), "delayedplace");

            attach(permissionsetid, profile.getPermission(PermissionType.FUNDDATA), "FUNDDATA");
            attach(permissionsetid, profile.getPermission(PermissionType.FACTSET), "FACTSET");
            attach(permissionsetid, profile.getPermission(PermissionType.QIDS_DELAY), "QIDS_DELAY");

            // todo: kill permission orphans
        }
    }

    private static void attach(int permissionsetid, Collection<String> tools, String toolValue) {
        for (String tool : tools) {
            String value = toolValue + "=" + tool;
            if ("FUNDDATA=FERI".equals(value)) {
                value = "FUND_DATA";
            }
            else if ("FUNDDATA=FWW".equals(value)) {
                value = "FUND_DATA_FWW";
            }
            int permissionid = makeDBidForSelector(value, "tool");
            jt.execute("INSERT INTO permission_permissionset (permissionid, permissionsetid) VALUES (" + permissionid + "," + permissionsetid + ")");
            System.out.println(value + "->" + permissionid + "  ");
        }

    }


    private static void attach(int permissionsetid, BitSet bs, String type) {
        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
            String ent = EntitlementsVwd.toEntitlement(i);
            int permissionid = makeDBidForSelector(ent, type);
            jt.execute("INSERT INTO permission_permissionset (permissionid, permissionsetid) VALUES (" + permissionid + "," + permissionsetid + ")");
            System.out.println(ent + "->" + permissionid + "  ");
        }

    }

    private static int selectIdOrZero(String sql) {
        try {
            return (Integer) jt.queryForObject(sql, Integer.class);
        }
        catch (DataAccessException e) {
            return 0;
        }
    }

    private static String entBit2Str(String type, BitSet bs) {
        StringBuffer sb = new StringBuffer();
        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
            String ent = EntitlementsVwd.toEntitlement(i);
            int dbId = makeDBidForSelector(ent, type);
            sb.append(ent + "->" + dbId + "  ");
        }

        return sb.toString();
    }

    private static int giveDBidForSelector(String selector, String type) {
        try {
            return (Integer) jt.queryForObject("SELECT id FROM permission WHERE value='" + selector + "' and type='" + type + "' ", Integer.class);
        }
        catch (DataAccessException e) {
            return 0;
        }
    }

    private static int makeDBidForSelector(String selector, String type) {
        try {
            return (Integer) jt.queryForObject("SELECT id FROM permission WHERE value='" + selector + "' and type='" + type + "' ", Integer.class);
        }
        catch (DataAccessException e) {
            jt.execute("INSERT INTO permission (type, value) VALUES ('" + type + "','" + selector + "')");
            return giveDBidForSelector(selector, type);
        }
    }


    public static void main2(String[] args) {
        int fehlt = 0;
        for (int i = 1; i < 21; i++) {
            for (int k = 'A'; k <= 'Z'; k++) {
                String selector = "" + i + (char) k;

                int id = 0;
                try {
                    id = (Integer) jt.queryForObject("SELECT id FROM permission WHERE value='" + selector + "' and type='delayedplace' ", Integer.class);
                    System.out.println(selector + "  ->  " + id);
                }
                catch (DataAccessException e) {
                    System.out.println(selector + "  ->  FEHLT");
                    fehlt++;
                }

            }
        }
        System.out.println("fehlt = " + fehlt);

    }

}
