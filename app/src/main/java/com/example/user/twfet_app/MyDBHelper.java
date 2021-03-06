package com.example.user.twfet_app;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Jeff.
 */
public class MyDBHelper extends SQLiteOpenHelper {
    protected ArrayList<String[]> Result = null;
    protected String CREATE_CSPSINFO = "create table if not exists cSpsInfo(SPS_ID text NOT NULL, SPS_NAME text, SPS_SDATE text,SPS_EDATE text, SYNCTIME text, CREATEDT text, CREATEID text, MODIFYDT text, MODIFYID text,PRIMARY KEY (SPS_ID))";
    protected String CREATE_CSTATIONCONF = "create table if not exists cStationConf(DEVICE_ID text NOT NULL, DeviceTypeID text, SPS_ID text,MCNO text, IP text,MACMFRC text, IMEI_CODE text,TRANSFER_STATUS text, SYNCTIME text, CREATEID text, CREATEDT text, MODIFYID text,MODIFYDT text,PRIMARY KEY (DEVICE_ID))";
    protected String CREATE_CTicketKind = "create table if not exists cTicketKind(TK_CODE text NOT NULL, TK_NAME text NOT NULL, TK_NAME_ENG text, TK_NAME_JAP text, TK_PRICE Integer,TK_BACK_FEE Integer,TK_DAYS text,TK_START_TM text,TK_UNTIL_TM text,SP_MEMO text, SYNCTIME text, CREATEDT text, CREATEID text, MODIFYDT text,MODIFYID text,PRIMARY KEY (TK_CODE))";
    protected String CREATE_ConnectIP = "create table if not exists cConnectIP(IP text NOT NULL,UN text NOT NULL,PASSWORD text NOT NULL,PRIMARY KEY (IP))";
    protected String CREATE_PULTRALIGHT03 = "create table if not exists pUltraLight03(TICKET_TYPE text NOT NULL, TICKET_NO text, SPS_ID text NOT NULL, TK_ENTER_DT text NOT NULL, IN_OUT_TYPE text NOT NULL, DEVICE_ID text NOT NULL, TK_CODE text, QRCODE text , INSERT_DB_DATETIME text, TRANSFER_STATUS text, CREATEID text, CREATEDT text, MODIFYID text,MODIFYDT text,FT_SERIALNO text,Rec INTEGER PRIMARY KEY AUTOINCREMENT)";
    protected String CREATE_PULTRALIGHT03_EXP = "create table if not exists pUltraLight03Exp(TICKET_TYPE text NOT NULL, TICKET_NO text, SPS_ID text NOT NULL, TK_ENTER_DT text NOT NULL, IN_OUT_TYPE text NOT NULL, DEVICE_ID text NOT NULL, TK_CODE text, QRCODE text , INSERT_DB_DATETIME text, TRANSFER_STATUS text, CREATEID text, CREATEDT text, MODIFYID text,MODIFYDT text,FT_SERIALNO text,Rec INTEGER PRIMARY KEY AUTOINCREMENT)";

    private final static String DATABASE_NAME = "mydata.db";
    private final static int DATABASE_VERSION = 1;

    public MyDBHelper(Context context) {
        //create database
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase Database) {
        // TODO Auto-generated method stub
        //create table
        Database.execSQL(CREATE_CSPSINFO);
        Database.execSQL(CREATE_CSTATIONCONF);
        Database.execSQL(CREATE_PULTRALIGHT03);
        Database.execSQL(CREATE_PULTRALIGHT03_EXP);
        Database.execSQL(CREATE_CTicketKind);
        Database.execSQL(CREATE_ConnectIP);

        WriteLog.appendLog("MyDBHelper.java/SQLITE的TABLE建立成功");
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
    }

    //從場站資料庫插入SpsInfo至SQLITE
    public boolean InsertToSpsInfo() {
        try {
            Connection con = ConnectionClass.CONN();
            if (con == null) {
                Log.d("MyDB.java/InsertToSpsIn", "con為Null");
                WriteLog.appendLog("MyDBHelper.java/InsertToSpsInfo/con為Null");
                return false;
            } else {
                String query = "select SPS_ID,convert(nvarchar(50),SPS_NAME) as SPS_NAME,SPS_SDATE,SPS_EDATE,SYNCTIME,CREATEDT,CREATEID,MODIFYDT,MODIFYID FROM cSpsInfo";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {   //從場站資料庫抓資料儲存至SQLITE
                    String statement = "insert into cSpsInfo (SPS_ID,SPS_NAME,SPS_SDATE,SPS_EDATE,SYNCTIME,CREATEDT,CREATEID,MODIFYDT,MODIFYID)" +
                            "values('" + rs.getString("SPS_ID") + "','" + rs.getString("SPS_NAME") + "','" + rs.getString("SPS_SDATE") + "','" + rs.getString("SPS_EDATE") + "','" + rs.getString("SYNCTIME") + "','" + rs.getString("CREATEDT") + "','" + rs.getString("CREATEID") + "','" + rs.getString("MODIFYDT") + "','" + rs.getString("MODIFYID") + "')";
                    super.getWritableDatabase().execSQL(statement);
                }
                super.close();
                return true;
            }
        } catch (Exception ex) {
            Log.d("MyDB.java/InsertToStCf", ex.toString());
            WriteLog.appendLog("MyDBHelper.java/InsertToStationConf/Exception:" + ex.toString());
            return false;
        }
    }

    //從場站資料庫插入StationConf至SQLITE
    public boolean InsertToStationConf() {
        try {
            Connection con = ConnectionClass.CONN();
            if (con == null) {
                Log.d("MyDB.java/InsertToStCf", "con為Null");
                WriteLog.appendLog("MyDBHelper.java/InsertToStationConf/con為Null");
                return false;
            } else {
                String query = "select DEVICE_ID,DeviceTypeID,SPS_ID,MCNO,IP,MACMFRC,IMEI_CODE,TRANSFER_STATUS,SYNCTIME,CREATEID,CREATEDT,MODIFYID,MODIFYDT FROM cStationConf";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {   //從場站資料庫抓資料儲存至SQLITE
                    String statement = "insert into cStationConf (DEVICE_ID,DeviceTypeID,SPS_ID,MCNO,IP,MACMFRC,IMEI_CODE,TRANSFER_STATUS,SYNCTIME,CREATEDT,CREATEID,MODIFYDT,MODIFYID) values('" + rs.getString("DEVICE_ID") + "','" + rs.getString("DeviceTypeID") + "','" + rs.getString("SPS_ID") + "','" + rs.getString("MCNO") + "','" + rs.getString("IP") + "','" + rs.getString("MACMFRC") + "','" + rs.getString("IMEI_CODE") + "','" + rs.getString("TRANSFER_STATUS") + "','" + rs.getString("SYNCTIME") + "','" + rs.getString("CREATEDT") + "','" + rs.getString("CREATEID") + "','" + rs.getString("MODIFYDT") + "','" + rs.getString("MODIFYID") + "')";
                    super.getWritableDatabase().execSQL(statement);
                }
                super.close();
                return true;
            }
        } catch (Exception ex) {
            Log.d("MyDB.java/InsertToStCf", ex.toString());
            WriteLog.appendLog("MyDBHelper.java/InsertToStationConf/Exception:" + ex.toString());
            return false;
        }
    }

    //從場站資料庫插入今日日期的UltraLight03至SQLITE
    public int InsertToUltraLight03() {
        int insertNo = 0;
        try {
            Connection con = ConnectionClass.CONN();
            if (con == null) {
                Log.d("MyDB.java/InsertToUt03", "con為Null");
                WriteLog.appendLog("MyDBHelper.java/InsertToUltraLight03/con為Null");
                return -1;
            } else {
                String query = "SELECT TICKET_TYPE,TICKET_NO,SPS_ID,TK_ENTER_DT,IN_OUT_TYPE,DEVICE_ID,TK_CODE,QRCODE,INSERT_DB_DATETIME,TRANSFER_STATUS,CREATEDT,CREATEID,MODIFYDT,MODIFYID " +
                        "FROM pUltraLight03 " +
                        "where CONVERT(char(6), TK_ENTER_DT, 12)=CONVERT(char(6), getdate(), 12)";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {   //從場站資料庫抓資料儲存至SQLITE
                    insertNo++;//數量+1
                    String statement = "insert into pUltraLight03 (TICKET_TYPE,TICKET_NO,SPS_ID,TK_ENTER_DT,IN_OUT_TYPE,DEVICE_ID,TK_CODE,QRCODE,INSERT_DB_DATETIME,TRANSFER_STATUS,CREATEDT,CREATEID,MODIFYDT,MODIFYID) values('" + rs.getString("TICKET_TYPE") + "','" + rs.getString("TICKET_NO") + "','" + rs.getString("SPS_ID") + "','" + rs.getString("TK_ENTER_DT") + "','" + rs.getString("IN_OUT_TYPE") + "','" + rs.getString("DEVICE_ID") + "','" + rs.getString("TK_CODE") + "','" + rs.getString("QRCODE") + "','" + rs.getString("INSERT_DB_DATETIME") + "','OK','" + rs.getString("CREATEID") + "','" + rs.getString("CREATEDT") + "','" + rs.getString("MODIFYID") + "','" + rs.getString("MODIFYDT") + "')";
                    super.getWritableDatabase().execSQL(statement);
                }
                super.close();
                return insertNo;
            }
        } catch (Exception ex) {
            Log.d("MyDB.java/InsertToUt03", ex.toString());
            WriteLog.appendLog("MyDBHelper.java/InsertToUltraLight03/Exception:" + ex.toString());
            return -1;
        }
    }

    //插入至SQLITE的UltraLight03
    public void InsertToSQLiteUltraLight03(String[] ResultArray, String TRS) {
        if (TRS.equals("OK")) {//連線驗票TICKET_TYPE text NOT NULL, TICKET_NO text NOT NULL, SPS_ID text NOT NULL, TK_ENTER_DT text NOT NULL, IN_OUT_TYPE text NOT NULL, DEVICE_ID text NOT NULL, TK_CODE text NOT NULL, QRCODE text , INSERT_DB_DATETIME text, TRANSFER_STATUS text, CREATEID text, CREATEDT text, MODIFYID text,MODIFYDT text
            String statement = "insert into pUltraLight03 (TICKET_TYPE,TICKET_NO,SPS_ID,TK_ENTER_DT,IN_OUT_TYPE,DEVICE_ID,TK_CODE,QRCODE,INSERT_DB_DATETIME,TRANSFER_STATUS,CREATEID,CREATEDT, MODIFYID ,MODIFYDT)" +
                    "values('" + ResultArray[0] + "','" + ResultArray[1] + "','" + ResultArray[2] + "','" + ResultArray[9] + "','" + ResultArray[3] + "','" + ResultArray[4] + "','" + ResultArray[5] + "','" + ResultArray[6] + "','" + ResultArray[7] + "','OK','" + ResultArray[8] + "','" + ResultArray[9] + "','" + ResultArray[8] + "','" + ResultArray[9] + "')";
            super.getWritableDatabase().execSQL(statement);
            super.close();
        } else {//離線驗票
            String statement = "insert into pUltraLight03 (TICKET_TYPE,TICKET_NO,SPS_ID,TK_ENTER_DT,IN_OUT_TYPE,DEVICE_ID,TK_CODE,QRCODE,INSERT_DB_DATETIME,TRANSFER_STATUS,CREATEID,CREATEDT, MODIFYID ,MODIFYDT,FT_SERIALNO)" +
                    "values('" + ResultArray[0] + "','" + ResultArray[1] + "','" + ResultArray[2] + "','" + ResultArray[9] + "','" + ResultArray[3] + "','" + ResultArray[4] + "','" + ResultArray[5] + "','" + ResultArray[6] + "','" + ResultArray[7] + "','','" + ResultArray[8] + "','" + ResultArray[9] + "','" + ResultArray[8] + "','" + ResultArray[9] + "','" + ResultArray[10] + "')";
            super.getWritableDatabase().execSQL(statement);
            super.close();
        }
    }

    //從場站資料庫插入TicketKind至SQLITE
    public boolean InsertTocTicketKind() {
        try {
            Connection con = ConnectionClass.CONN();
            if (con == null) {
                Log.d("MyDB.java/InsertToTkKd", "con為Null");
                WriteLog.appendLog("MyDBHelper.java/InsertTocTicketKind/con為Null");
                return false;
            } else {
                String query = "select TK_CODE,convert(nvarchar(50),TK_NAME)as TK_NAME,convert(nvarchar(50),TK_NAME_ENG)as TK_NAME_ENG,convert(nvarchar(50),TK_NAME_JAP) as TK_NAME_JAP,TK_PRICE,TK_BACK_FEE,TK_DAYS,TK_START_TM,TK_UNTIL_TM,SP_MEMO,SYNCTIME,CREATEDT,CREATEID,MODIFYDT,MODIFYID  from cTicketKind";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {   //從場站資料庫抓資料儲存至SQLITE
                    String statement = "insert into cTicketKind (TK_CODE, TK_NAME, TK_NAME_ENG, TK_NAME_JAP , TK_PRICE,TK_BACK_FEE,TK_DAYS,TK_START_TM,TK_UNTIL_TM,SP_MEMO,SYNCTIME,CREATEDT,CREATEID,MODIFYDT,MODIFYID) values('" + rs.getString("TK_CODE") + "','" + rs.getString("TK_NAME") + "','" + rs.getString("TK_NAME_ENG") + "','" + rs.getString("TK_NAME_JAP") + "','" + rs.getString("TK_PRICE") + "','" + rs.getString("TK_BACK_FEE") + "','" + rs.getString("TK_DAYS") + "','" + rs.getString("TK_START_TM") + "','" + rs.getString("TK_UNTIL_TM") + "','" + rs.getString("SP_MEMO") + "','" + rs.getString("SYNCTIME") + "','" + rs.getString("CREATEDT") + "','" + rs.getString("CREATEID") + "','" + rs.getString("MODIFYDT") + "','" + rs.getString("MODIFYID") + "')";
                    super.getWritableDatabase().execSQL(statement);
                }
                super.close();
                return true;
            }
        } catch (Exception ex) {
            Log.d("MyDB.java/InsertToTkKd", ex.toString());
            WriteLog.appendLog("MyDBHelper.java/InsertTocTicketKind/Exception:" + ex.toString());
            return false;
        }
    }

    //取得SQLite的UltraLight03資料數量
    public int GetUltraLight03Number() {
        int result = 0;
        Cursor cursor = super.getReadableDatabase().rawQuery("SELECT  COUNT(*) FROM pUltraLight03 ", null);
        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }
        return result;
    }

    //取得SQLite的UltraLight03內TRANSFER_STATUS不為OK資料數量
    public int GetUltraLight03NotOkNumber() {
        int result = 0;
        Cursor cursor = super.getReadableDatabase().rawQuery("SELECT  COUNT(*) FROM pUltraLight03 where TRANSFER_STATUS<>'OK' ", null);
        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }
        return result;
    }

    //取得SQLite的cTicketKind資料數量
    public int GetTicketKindNumber() {
        int result = 0;
        Cursor cursor = super.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM cTicketKind ", null);

        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }
        return result;
    }

    //取得SQLite的cStationConf資料數量
    public int GetStationConfNumber() {
        int result = 0;
        Cursor cursor = super.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM cStationConf ", null);

        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }
        return result;
    }

    //取得SQLite的cStationConf資料數量
    public int GetSpsInfoNumber() {
        int result = 0;
        Cursor cursor = super.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM cSpsInfo ", null);

        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }
        return result;
    }

    //取得SQLite的對應票種的TK_NAME
    public String GetTKName(String TK_CODE) {
        String TK_NAME = "";
        //使用 rawQuery 方法
        Cursor cursor = super.getReadableDatabase().rawQuery("select TK_NAME from cTicketKind where TK_CODE=?",
                new String[]{TK_CODE});
        while (cursor.moveToNext()) {
            TK_NAME = cursor.getString(0);
        }
        cursor.close();
        return TK_NAME;
    }

    //刪除StationConf資料庫
    public void DeleteStationConf() {
        String statement = "delete from cStationConf";
        Log.d("Delete", statement);
        super.getWritableDatabase().execSQL(statement);
        super.close();
    }

    //刪除StationConf資料庫
    public void DeleteSpsInfo() {
        String statement = "delete from cSpsInfo";
        Log.d("Delete", statement);
        super.getWritableDatabase().execSQL(statement);
        super.close();
    }

    //刪除UltraLight03資料庫
    public void DeleteUltraLight03() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String statement = "delete from pUltraLight03 where TK_ENTER_DT not like '" + df.format(c.getTime()) + "%'";
        super.getWritableDatabase().execSQL(statement);
        super.close();
    }

    //刪除UltraLight03Exp資料庫
    public void DeleteUltraLight03Exp() {
        String statement = "delete from pUltraLight03Exp";
        super.getWritableDatabase().execSQL(statement);
        super.close();
    }

    //刪除TicketKind資料庫
    public void DeleteTicketKind() {
        String statement = "delete from cTicketKind";
        Log.d("Delete", statement);
        super.getWritableDatabase().execSQL(statement);
        super.close();
    }

    //更新StationConf的MODIFY不為NULL的資料
    public void UpdateSpsInfo() {
        try {
            Connection con = ConnectionClass.CONN();
            String SPS_ID = "", SPS_NAME = "", SPS_SDATE = "", SPS_EDATE = "", SYNCTIME = "", CREATEDT = "", CREATEID = "", MODIFYDT = "", MODIFYID = "";
            if (con == null) {
                Log.d("MyDB.java/UpdateSps", "con為Null");
                WriteLog.appendLog("MyDBHelper.java/UpdateSpsInfo/con為Null");
            } else {
                String query = "select SPS_ID,convert(nvarchar(50),SPS_NAME) as SPS_NAME,SPS_SDATE,SPS_EDATE,SYNCTIME,CREATEDT,CREATEID,MODIFYDT,MODIFYID FROM cSpsInfo where MODIFYDT is not null";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {   //從場站資料庫抓資料儲存至SQLITE
                    SPS_ID = rs.getString("SPS_ID");
                    SPS_NAME = rs.getString("SPS_NAME");
                    SPS_SDATE = rs.getString("SPS_SDATE");
                    SPS_EDATE = rs.getString("SPS_EDATE");
                    SYNCTIME = rs.getString("SYNCTIME");
                    CREATEDT = rs.getString("CREATEDT");
                    CREATEID = rs.getString("CREATEID");
                    MODIFYDT = rs.getString("MODIFYDT");
                    MODIFYID = rs.getString("MODIFYID");
                    String statement = "update cSpsInfo set SPS_ID = '" + SPS_ID + "' ,SPS_NAME= '" + SPS_NAME + "' ,SPS_SDATE = '" + SPS_SDATE + "' , SPS_EDATE = '" + SPS_EDATE + "',SYNCTIME = '" + SYNCTIME + "' , CREATEDT = '" + CREATEDT + "' , CREATEID = '" + CREATEID + "' , MODIFYDT = '" + MODIFYDT + "' , MODIFYID = '" + MODIFYID + "' where SPS_ID ='" + SPS_ID + "'";
                    super.getWritableDatabase().execSQL(statement);
                }
                super.close();
            }
        } catch (Exception ex) {
            Log.d("MyDB.java/UpdateSps", ex.toString());
            WriteLog.appendLog("MyDBHelper.java/UpdateSpsInfo/Exception:" + ex.toString());
        }
    }

    //更新StationConf的MODIFY不為NULL的資料
    public void UpdateStationConf() {
        try {
            Connection con = ConnectionClass.CONN();
            String DEVICE_ID = "", DeviceTypeID = "", SPS_ID = "", IP = "", IMEI_CODE = "", SYNCTIME = "", CREATEDT = "", CREATEID = "", MODIFYDT = "", MODIFYID = "";
            if (con == null) {
                Log.d("MyDB.java/UpdateStCf", "con為Null");
                WriteLog.appendLog("MyDBHelper.java/UpdateStationConf/con為Null");
            } else {
                String query = "select   DEVICE_ID,DeviceTypeID,SPS_ID,IP,IMEI_CODE,SYNCTIME,CREATEDT,CREATEID,MODIFYDT,MODIFYID from cStationConf where MODIFYDT is not null";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {   //從場站資料庫抓資料儲存至SQLITE
                    DEVICE_ID = rs.getString("DEVICE_ID");
                    DeviceTypeID = rs.getString("DeviceTypeID");
                    SPS_ID = rs.getString("SPS_ID");
                    IP = rs.getString("IP");
                    IMEI_CODE = rs.getString("IMEI_CODE");
                    SYNCTIME = rs.getString("SYNCTIME");
                    CREATEDT = rs.getString("CREATEDT");
                    CREATEID = rs.getString("CREATEID");
                    MODIFYDT = rs.getString("MODIFYDT");
                    MODIFYID = rs.getString("MODIFYID");
                    String statement = "update cStationConf set DEVICE_ID = '" + DEVICE_ID + "' ,DeviceTypeID= '" + DeviceTypeID + "' ,SPS_ID = '" + SPS_ID + "' , IP = '" + IP + "' , IMEI_CODE = '" + IMEI_CODE + "' ,SYNCTIME = '" + SYNCTIME + "' , CREATEDT = '" + CREATEDT + "' , CREATEID = '" + CREATEID + "' , MODIFYDT = '" + MODIFYDT + "' , MODIFYID = '" + MODIFYID + "' where DEVICE_ID ='" + DEVICE_ID + "'";
                    super.getWritableDatabase().execSQL(statement);
                }
                super.close();
            }
        } catch (Exception ex) {
            Log.d("MyDB.java/UpdateStCf", ex.toString());
            WriteLog.appendLog("MyDBHelper.java/UpdateStationConf/Exception:" + ex.toString());
        }
    }

    //更新TicketKind的MODIFY不為NULL的資料
    public void UpdateTicketKind() {
        try {
            Connection con = ConnectionClass.CONN();
            String TK_CODE = "", TK_NAME = "", TK_NAME_ENG = "", SYNCTIME = "", MODIFYDT = "";
            if (con == null) {
                Log.d("MyDB.java/UpdateTkKd", "con為Null");
                WriteLog.appendLog("MyDBHelper.java/UpdateTicketKind/con為Null");
            } else {
                String query = "select  TK_CODE, convert(nvarchar(50), TK_NAME)as TK_NAME, convert(nvarchar(50), TK_NAME_ENG)as TK_NAME_ENG,SYNCTIME,MODIFYDT from cTicketKind where MODIFYDT is not null";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {   //從場站資料庫抓資料儲存至SQLITE
                    TK_CODE = rs.getString("TK_CODE");
                    TK_NAME = rs.getString("TK_NAME");
                    TK_NAME_ENG = rs.getString("TK_NAME_ENG");
                    SYNCTIME = rs.getString("SYNCTIME");
                    MODIFYDT = rs.getString("MODIFYDT");
                    String statement = "update cTicketKind set TK_CODE = '" + TK_CODE + "' ,TK_NAME= '" + TK_NAME + "' ,TK_NAME_ENG = '" + TK_NAME_ENG + "' , SYNCTIME = '" + SYNCTIME + "' ,MODIFYDT = '" + MODIFYDT + "' where TK_CODE ='" + TK_CODE + "'";
                    super.getWritableDatabase().execSQL(statement);
                }
                super.close();
            }
        } catch (Exception ex) {
            Log.d("MyDB.java/UpdateTkKd", ex.toString());
            WriteLog.appendLog("MyDBHelper.java/UpdateTicketKind/Exception:" + ex.toString());
        }
    }

    //判斷SQLITE的UltraLight03是否已存此TIKCE_NO
    public boolean IsTICKETNOexist(String TICKET_NO, String SPS_ID, String TK_ENTER_DT) {
        //使用 rawQuery 方法
        Cursor cursor = super.getReadableDatabase().rawQuery("select * from pUltraLight03 where TICKET_NO=? and SPS_ID=? and substr(TK_ENTER_DT,1,10)=?",
                new String[]{TICKET_NO, SPS_ID, TK_ENTER_DT});
        while (cursor.moveToNext()) {
            return true;//已存在
        }
        cursor.close();
        return false;
    }

    //判斷SQLITE的UltraLight03是否已存此TKQRCODE
    public boolean IsTKQRCODEexist(String TK_QRCODE, String SPS_ID, String TK_ENTER_DT) {
        //使用 rawQuery 方法
        Cursor cursor = super.getReadableDatabase().rawQuery("select * from pUltraLight03 where QRCODE=? and SPS_ID=? and substr(TK_ENTER_DT,1,10)=?",
                new String[]{TK_QRCODE, SPS_ID, TK_ENTER_DT});
        while (cursor.moveToNext()) {
            return true;//已存在
        }
        cursor.close();
        return false;
    }

    //更新SQLITE的UltraLight03的TRANSFER_STATUS為OK
    public void UpdateUltraLight03TRS(int Rec) {
        //使用 rawQuery 方法
        String statement = "update pUltraLight03 set TRANSFER_STATUS ='OK' where Rec="+Rec;
        super.getWritableDatabase().execSQL(statement);
        super.close();
    }

    //取得SQLite的UltraLight03Exp的總數量
    public int GetUltraLight03ExpNmber() {
        int GetNumber = 0;
        Cursor cursor = super.getReadableDatabase().rawQuery("SELECT count(*) FROM pUltraLight03Exp", null);
        while (cursor.moveToNext()) {
            GetNumber = cursor.getInt(0);
        }
        return GetNumber;
    }

    //從SQLITE抓出ut03的TRANSFER_STATUS不為OK的上傳
    public void SelectFromUltraLight03() {
        Connection con = ConnectionClass.CONN();
        String result_msg = "";
        Cursor cursor = super.getReadableDatabase().rawQuery("SELECT * FROM pUltraLight03 where TRANSFER_STATUS <>'OK'", null);
        while (cursor.moveToNext()) {
            result_msg = executeExportStoredProcedure(con, cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getString(9), cursor.getString(10), cursor.getString(11), cursor.getString(12), cursor.getString(13), cursor.getString(14));
            if (result_msg.indexOf("成功") > -1) {//如果有新增成功
                //新增至EXP資料表，用來計算有幾筆上傳成功
                InsertToUltraLight03Exp(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getString(9), cursor.getString(10), cursor.getString(11), cursor.getString(12), cursor.getString(13), cursor.getString(14));
                //更新此票券的TRANSFER_STATUS為OK
                UpdateUltraLight03TRS(cursor.getInt(15));
            } else if (result_msg.indexOf("失敗") > -1 || result_msg.indexOf("衝突") > -1) {//如果新增失敗
                //刪除此筆資料
                String statement = "delete from pUltraLight03 where Rec=" + cursor.getInt(15);
                super.getWritableDatabase().execSQL(statement);
            }
        }
    }

    //執行匯出SP_
    public String executeExportStoredProcedure(Connection con, String TICKET_TYPE, String TICKET_NO, String SPS_ID, String TK_ENTER_DT, String IN_OUT_TYPE,
                                               String DEVICE_ID, String TK_CODE, String QRCODE, String INSERT_DB_DATETIME, String TRANSFER_STATUS, String CREATEID,
                                               String CREATEDT, String MODIFYID, String MODIFYDT, String FT_SERIALNO) {
        String RETURN_MSG = "";
        try {
            CallableStatement cstmt = con.prepareCall("{ call dbo.SP_HD_UpLoad_pUltraLight03(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
            cstmt.setString("FT_SERIALNO", FT_SERIALNO);
            cstmt.setString("TICKET_TYPE", TICKET_TYPE);
            cstmt.setString("TICKET_NO", TICKET_NO);
            cstmt.setString("SPS_ID", SPS_ID);
            cstmt.setString("TK_ENTER_DT", TK_ENTER_DT);
            cstmt.setString("IN_OUT_TYPE", IN_OUT_TYPE);
            cstmt.setString("DEVICE_ID", DEVICE_ID);
            cstmt.setString("TK_CODE", TK_CODE);
            cstmt.setString("QRCODE", QRCODE.equals("") ? null : QRCODE);
            cstmt.setString("INSERT_DB_DATETIME", INSERT_DB_DATETIME);
            cstmt.setString("TRANSFER_STATUS", TRANSFER_STATUS);
            cstmt.setString("CREATEID", CREATEID);
            cstmt.setString("CREATEDT", CREATEDT);
            cstmt.setString("MODIFYID", MODIFYID);
            cstmt.setString("MODIFYDT", MODIFYDT);
            cstmt.registerOutParameter(16, java.sql.Types.VARCHAR);
            cstmt.execute();
            RETURN_MSG = cstmt.getString(16);
            cstmt.close();
            return RETURN_MSG;
        } catch (Exception ex) {
            Log.d("MyDB.java/匯出SP", ex.toString());
            WriteLog.appendLog("MyDBHelper.java/匯出SP/Exception:" + ex.toString());
            ex.printStackTrace();
        }
        return RETURN_MSG;
    }

    //將已匯出資料儲存至pUltraLight03Exp
    public void InsertToUltraLight03Exp(String TICKET_TYPE, String TICKET_NO, String SPS_ID, String TK_ENTER_DT, String IN_OUT_TYPE, String DEVICE_ID,
                                        String TK_CODE, String QRCODE, String INSERT_DB_DATETIME, String TRANSFER_STATUS, String CREATEID,
                                        String CREATEDT, String MODIFYID, String MODIFYDT, String FT_SERIALNO) {
        String statement = "insert into pUltraLight03Exp (TICKET_TYPE,TICKET_NO,SPS_ID,TK_ENTER_DT,IN_OUT_TYPE,DEVICE_ID,TK_CODE ,QRCODE,INSERT_DB_DATETIME,TRANSFER_STATUS,CREATEID,CREATEDT,MODIFYID,MODIFYDT,FT_SERIALNO)" +
                "values('" + TICKET_TYPE + "','" + TICKET_NO + "','" + SPS_ID + "','" + TK_ENTER_DT + "','" + IN_OUT_TYPE + "','" + DEVICE_ID + "','" + TK_CODE + "','" + QRCODE + "','" + INSERT_DB_DATETIME + "','" + TRANSFER_STATUS + "','" + CREATEID + "','" + CREATEDT + "','" + MODIFYID + "','" + MODIFYDT + "','" + FT_SERIALNO + "')";
        super.getWritableDatabase().execSQL(statement);
    }

    //執行館內人數查詢SP
    public String executePeopleNumStoredProcedure(Connection con, String SPS_ID) {
        String returnNumber = "0";
        try {
            CallableStatement cstmt = con.prepareCall("{ call dbo.SP_GATE_PeopleCount(?)}");
            cstmt.setString("SPS_ID", SPS_ID);
            ResultSet rs = cstmt.executeQuery();
            while (rs.next()) {
                returnNumber = rs.getString("ORG_CAPACITY");
            }
            cstmt.close();
            return returnNumber;
        } catch (Exception ex) {
            return returnNumber;
        }
    }

    //設定資料庫連線資料存至SQLITE
    public void InsertToConnectIP(String ip, String un, String password) {
        try {
            //先刪除SQLITE內的IP
            String statement = "delete from cConnectIP";
            super.getWritableDatabase().execSQL(statement);
            //插入新的IP
            String statement2 = "insert into cConnectIP (IP,UN,PASSWORD) values('" + ip.trim() + "','" + un.trim() + "','" + password.trim() + "')";
            super.getWritableDatabase().execSQL(statement2);
            super.close();
        } catch (Exception ex) {
            Log.d("MyDB.java/資料庫IP存至SQLITE", ex.toString());
            WriteLog.appendLog("MyDBHelper.java/InsertToConnectIP/Exception:" + ex.toString());
        }
    }

    public Cursor GetConnectInfo() {
        Cursor cursor = super.getReadableDatabase().rawQuery("select * from cConnectIP", null);
        return cursor;
    }

    //取得資料庫連線IP
    public String GetConnectIP() {
        String IP = "";
        Cursor cursor = super.getReadableDatabase().rawQuery("select IP from cConnectIP", null);
        while (cursor.moveToNext()) {
            IP = cursor.getString(0);
        }
        cursor.close();
        return IP;
    }

    //取得資料庫連線帳號
    public String GetConnectUN() {
        String UN = "";
        Cursor cursor = super.getReadableDatabase().rawQuery("select UN from cConnectIP", null);
        while (cursor.moveToNext()) {
            UN = cursor.getString(0);
        }
        cursor.close();
        return UN;
    }

    //取得資料庫連線密碼
    public String GetConnectPASSWORD() {
        String PASSWORD = "";
        Cursor cursor = super.getReadableDatabase().rawQuery("select PASSWORD from cConnectIP", null);
        while (cursor.moveToNext()) {
            PASSWORD = cursor.getString(0);
        }
        cursor.close();
        return PASSWORD;
    }

    //檢查設備代碼是否存在
    public boolean CheckExistDEVICE_ID(String DEVICE_ID) {
        boolean bol = false;
        Cursor cursor = super.getReadableDatabase().rawQuery("select * from cStationConf where DEVICE_ID=? and DeviceTypeID ='03'", new String[]{DEVICE_ID});
        while (cursor.moveToNext()) {
            bol = true;
        }
        cursor.close();
        return bol;
    }

    //取得全期間圖片
    public byte[] GetByte(String FT_SERIALNO) {
        byte[] image = null;
        Connection con = ConnectionClass.CONN();
        String query = "select FT_S_IMAGE from rFullTermInformation where FT_SERIALNO='" + FT_SERIALNO + "'";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                image = rs.getBytes(1);
            }
        } catch (Exception ex) {
            WriteLog.appendLog("MyDBHelper.java/GetByte/Exception:" + ex.toString());
        }
        return image;
    }
}
