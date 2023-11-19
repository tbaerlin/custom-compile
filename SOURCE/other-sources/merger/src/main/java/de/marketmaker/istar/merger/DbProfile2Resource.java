package de.marketmaker.istar.merger;//import de.marketmaker.istar.common.util.EntitlementsVwd;
//import de.marketmaker.istar.domain.data.PriceQuality;
//import de.marketmaker.istar.domain.profile.PermissionType;
//import de.marketmaker.istar.domain.profile.Profile;
//import de.marketmaker.istar.merger.provider.profile.ResourceProfileSource;
//import org.springframework.dao.DataAccessException;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.datasource.SingleConnectionDataSource;

//import javax.sql.DataSource;
//import javax.naming.Context;
//import javax.naming.InitialContext;
//import java.util.BitSet;
//import java.util.Collection;
//import java.util.Set;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.io.FileWriter;
import java.io.IOException;

public class DbProfile2Resource {

    public static void main(String[] args) {
        int maxselector = 1000;
        Connection con = null;

        try {   // Loading database driver
           Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e) {
          System.err.println("Exception: "+e.getMessage());
        }

        try {   // Connection to database
            con = DriverManager.getConnection("jdbc:mysql://neutron/fitusers?autoReconnect=true&amp;"
                    + "useUnicode=true&amp;characterEncoding=ISO-8859-1","merger", "merger");

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery ("SELECT userid, login FROM users where login " +
                                                "LIKE '%_db' and companyid=28 and productid=33");

            rs.last();
            int rows = rs.getRow();
            rs.beforeFirst();

            ResultSet tmp_rs;

            String Outputfile ="resource-profiles.properties";

            int counter = 0, i=0;
            String[] User= new String [rows];
            int[] userIds = new int[rows];
            int[] profileIds = new int[rows];
            int[] permissionsetIds = new int[rows];
            int[][] permissionIds = new int[rows][maxselector];
            for( int x=0;x < rows; x++){
                for (int y=0; y < maxselector; y++){
                    permissionIds[x][y] = 0;
                }
            }
            String[] prices_realtime = new String [rows];
            String[] prices_delay = new String [rows];
            String[] news_delay = new String [rows];
            String[] factset = new String [rows];
            String[] funddata = new String [rows];
            String[] qids_delay = new String [rows];
            for (int x = 0; x<rows; x++){
                prices_realtime[x]="";
                prices_delay[x]="";
                news_delay[x]="";
                factset[x]="";
                funddata[x]="";
                qids_delay[x]="";
            }
            String permission_Value ="";
            String permission_Type ="";

            while (rs.next() ) {
                userIds[counter] = rs.getInt ("userid") ;
                User[counter]= rs.getString ("login");
                counter ++;
            }
            for (counter=0; counter < rows; counter++)
            {
                tmp_rs =  stmt.executeQuery("SELECT profileid FROM profile_users where userid='"
                                            + userIds[counter] +"' ");
                while (tmp_rs.next() ) {
                    profileIds[counter] =tmp_rs.getInt ("profileid") ;
                }
            }
            for (counter=0; counter < rows; counter++)
            {
                tmp_rs =  stmt.executeQuery("SELECT permissionsetid FROM permissionset_profile " +
                                            "where profileid='"+ profileIds[counter] +"' ");
                while (tmp_rs.next() ) {
                    permissionsetIds[counter] =tmp_rs.getInt ("permissionsetid") ;
                }
            }
            for (counter=0; counter < rows; counter++)
            {
                tmp_rs =  stmt.executeQuery("SELECT permissionid FROM permission_permissionset " +
                                            "where permissionsetid='"+ permissionsetIds[counter] +"' ");
                i=0;
                while (tmp_rs.next() ) {
                    permissionIds[counter][i] =tmp_rs.getInt ("permissionid") ;
                    i++;
                }
            }
            for (counter=0; counter < rows; counter++)
            {
                for (i=0; i<maxselector; i++) {
                    if (permissionIds[counter][i] != 0) {
                        tmp_rs =  stmt.executeQuery("SELECT type, value FROM permission " +
                                "                   where id='"+permissionIds[counter][i]+"' ");
                        while (tmp_rs.next() ) {
                            permission_Value = tmp_rs.getString ("value");
                            permission_Type = tmp_rs.getString ("type");

                           if (permission_Type.equalsIgnoreCase("realtimeplace")) {
                                if(prices_realtime[counter].equalsIgnoreCase("") ) {
                                    prices_realtime[counter]=permission_Value;
                                } else {
                                    prices_realtime[counter]=prices_realtime[counter] + ","
                                                             + permission_Value;
                                }
                            }
                            if (permission_Type.equals("delayedplace")) {
                                if (prices_delay[counter].equalsIgnoreCase("")){
                                    prices_delay[counter]=permission_Value;
                                } else {
                                prices_delay[counter]=prices_delay[counter] + ","
                                                      + permission_Value;
                                }
                            }
                            if (permission_Type.equals("delayedplace")) {
                                if (news_delay[counter].equalsIgnoreCase("")) {
                                    news_delay[counter]=permission_Value;
                                } else {
                                    news_delay[counter]=news_delay[counter] + ","
                                                        + permission_Value;
                                }
                            }
                            if (permission_Type.equals("tool")) {
                                if (permission_Value.startsWith("FACTSET=")) {
                                    permission_Value=permission_Value.replace("FACTSET=", "");
                                    if (factset[counter].equalsIgnoreCase("")) {
                                        factset[counter]=permission_Value;
                                    } else {
                                        factset[counter]=factset[counter] + "," + permission_Value;
                                    }
                                }
                                if (permission_Value.endsWith("FUND_DATA_FWW")){
                                    funddata[counter]="FWW";
                                }
                                if (permission_Value.endsWith("FUND_DATA")){
                                    funddata[counter]="FERI";
                                }
                                if (permission_Value.startsWith("QIDS_DELAY=")) {
                                    permission_Value=permission_Value.replace("QIDS_DELAY=", "");
                                    if (qids_delay[counter].equalsIgnoreCase("")) {
                                        qids_delay[counter]= permission_Value;
                                    } else {
                                        qids_delay[counter]=qids_delay[counter] + ","
                                                            + permission_Value;
                                    }
                                }
                            }
                        }
                    } else {
                        i=maxselector;
                    }
                }
            }
            con.close(); // Close connection to database

            // Output on screen
            /*
            for (counter=0; counter < rows; counter++) {
                User[counter] = User[counter].replace("_db", "");
                if ( ! (prices_realtime[counter].equalsIgnoreCase(""))) {
                    System.out.println(User[counter] + ".PRICES_REALTIME=" + prices_realtime[counter]);
                }
                if ( ! (prices_delay[counter].equalsIgnoreCase(""))) {
                    System.out.println(User[counter] + ".PRICES_DELAY=" + prices_delay[counter]);
                }
                if ( ! (news_delay[counter].equalsIgnoreCase(""))) {
                    System.out.println(User[counter] + ".NEWS_DELAY=" + news_delay[counter]);
                }
                if ( ! (factset[counter].equalsIgnoreCase(""))) {
                    System.out.println(User[counter] + ".FACTSET=" + factset[counter]);
                }
                if ( ! (funddata[counter].equalsIgnoreCase(""))) {
                    System.out.println(User[counter] + ".FUNDDATA=" + funddata[counter]);
                }
                if ( ! (qids_delay[counter].equalsIgnoreCase(""))) {
                    System.out.println(User[counter] + ".QIDS_DELAY=" + qids_delay[counter]);
                }
                System.out.println();
            }
            */

            // Output in file
            try {
                FileWriter fout;
                fout = new FileWriter(Outputfile);
                for (counter=0; counter < rows; counter++) {
                    User[counter] = User[counter].replace("_db", "");
                    if ( ! (prices_realtime[counter].equalsIgnoreCase(""))) {
                        fout.write(User[counter] + ".PRICES_REALTIME="
                                + prices_realtime[counter] + "\n");
                    }
                    if ( ! (prices_delay[counter].equalsIgnoreCase(""))) {
                        fout.write(User[counter] + ".PRICES_DELAY="
                                + prices_delay[counter]  + "\n");
                    }
                    if ( ! (news_delay[counter].equalsIgnoreCase(""))) {
                        fout.write(User[counter] + ".NEWS_DELAY="
                                + news_delay[counter]  + "\n");
                    }
                    if ( ! (factset[counter].equalsIgnoreCase(""))) {
                        fout.write(User[counter] + ".FACTSET=" + factset[counter]  + "\n");
                    }
                    if ( ! (funddata[counter].equalsIgnoreCase(""))) {
                        fout.write(User[counter] + ".FUNDDATA=" + funddata[counter]  + "\n");
                    }
                    if ( ! (qids_delay[counter].equalsIgnoreCase(""))) {
                        fout.write(User[counter] + ".QIDS_DELAY="
                                + qids_delay[counter]  + "\n");
                    }
                    fout.write(" " + "\n");
                }
                fout.close();
                System.out.println("data successfully written to file: "+ Outputfile);

            } catch (IOException e){
                System.out.println("exception while writing file - Please check your Outputfile.");
            }
        }
        catch (Exception e)
        {
            System.out.println("exception while connecting to database");
        }
    }
}
