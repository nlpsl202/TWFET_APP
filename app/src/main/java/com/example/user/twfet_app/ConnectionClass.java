package com.example.user.twfet_app;

/**
 * Created by USER on 2015/11/17.
 */

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by USER on 2015/11/2.
 */
public class ConnectionClass {
    static String ip="172.16.30.181";
    static String classs = "net.sourceforge.jtds.jdbc.Driver";
    static String db = "TWFET_CDPS";
    static String un = "sa";
    static String password = "hokawaCdps";
    //static String ip="192.168.10.193";
    //static String classs = "net.sourceforge.jtds.jdbc.Driver";
    //static String db = "TWFET_CDPS";
    //static String un = "TWFET";
    //static String password = "Mds31275691$";


    @SuppressLint("NewApi")
    public static Connection CONN() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        String ConnURL;
        try {
            Class.forName(classs);
            ConnURL = "jdbc:jtds:sqlserver://" + ip + ";"
                    + "databaseName=" + db + ";charset=utf8;user=" + un + ";password="
                    + password + ";";
            conn = DriverManager.getConnection(ConnURL);
        } catch (SQLException e) {
            Log.e("ERRO", e.getMessage());
            WriteLog.appendLog("ConnectionClass.java/CONN/Exception:" + e.toString());
        } catch (ClassNotFoundException e) {
            Log.e("ERRO", e.getMessage());
            WriteLog.appendLog("ConnectionClass.java/CONN/Exception:" + e.toString());
        } catch (Exception e) {
            Log.e("ERRO", e.getMessage());
            WriteLog.appendLog("ConnectionClass.java/CONN/Exception:" + e.toString());
        }
        return conn;
    }
}

