package com.example.user.twfet_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jeff.
 */
public class MainActivity extends Activity {
    private EditText DEVICE_ID_Edt;
    private Button loginBtn;
    private String SPS_ID, DEVICE_ID;

    //SQLite
    private MyDBHelper mydbHelper;

    //建立連線
    private Connection con;
    private Statement stmt;

    //剪貼簿
    private ClipboardManager cbMgr;
    private ClipboardManager.OnPrimaryClipChangedListener mPrimaryClipChangedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        if (shouldAskPermissions()) {
            askPermissions();
        }

        DEVICE_ID_Edt = (EditText) findViewById(R.id.DEVICE_ID_Edt);
        loginBtn = (Button) findViewById(R.id.LoginBtn);
        cbMgr = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        mydbHelper = new MyDBHelper(this);

        Cursor cursor = mydbHelper.GetConnectInfo();
        if (cursor.moveToNext()) {
            ConnectionClass.ip = cursor.getString(cursor.getColumnIndex("IP"));
            ConnectionClass.un = cursor.getString(cursor.getColumnIndex("UN"));
            ConnectionClass.password = cursor.getString(cursor.getColumnIndex("PASSWORD"));
        }
        cursor.close();

        if (!pingIP(ConnectionClass.ip)) {
            Toast.makeText(MainActivity.this, "無法更新資料庫", Toast.LENGTH_SHORT).show();
        } else {
            con = ConnectionClass.CONN();
            if (!CheckSpsInfoData()) {
                Toast.makeText(MainActivity.this, "無法更新資料庫", Toast.LENGTH_SHORT).show();
            } else if (!CheckStationConfData()) {
                Toast.makeText(MainActivity.this, "無法更新資料庫", Toast.LENGTH_SHORT).show();
            } else if (!CheckTicketKindData()) {
                Toast.makeText(MainActivity.this, "無法更新資料庫", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "資料庫已更新。", Toast.LENGTH_SHORT).show();
            }
        }

        //掃描事件
        mPrimaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
            public void onPrimaryClipChanged() {
                try {
                    setVibrate(100);
                    DEVICE_ID_Edt.setText("");
                    DEVICE_ID = cbMgr.getPrimaryClip().getItemAt(0).getText().toString().trim();
                    if (DEVICE_ID.split("@").length == 3) {
                        if (isIP(DEVICE_ID.split("@")[0])) {
                            mydbHelper.InsertToConnectIP(DEVICE_ID.split("@")[0], DEVICE_ID.split("@")[1], DEVICE_ID.split("@")[2]);
                            Toast.makeText(MainActivity.this, "連線資料更新成功！", Toast.LENGTH_SHORT).show();
                        }
                    } else if (!mydbHelper.CheckExistDEVICE_ID(DEVICE_ID)) {
                        Toast.makeText(MainActivity.this, "無此設備代碼", Toast.LENGTH_SHORT).show();
                    } else {
                        SPS_ID = DEVICE_ID.substring(0, 4);
                        Intent intenting = new Intent();
                        intenting.setClass(MainActivity.this, AfterLogin.class);
                        intenting.putExtra("DEVICE_ID", DEVICE_ID);//傳遞DEVICE_ID給登入後的頁面
                        intenting.putExtra("SPS_ID", SPS_ID);//傳遞SPS_ID給登入後的頁面
                        startActivityForResult(intenting, 0);
                    }
                } catch (Exception ex) {
                    WriteLog.appendLog("MainActivity.java/onPrimaryClipChanged/Exception:" + ex.toString());
                    Toast.makeText(MainActivity.this, "登入錯誤", Toast.LENGTH_SHORT).show();
                }
            }
        };
        cbMgr.addPrimaryClipChangedListener(mPrimaryClipChangedListener);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    DEVICE_ID = DEVICE_ID_Edt.getText().toString().trim();
                    if (DEVICE_ID.split("@").length == 3) {
                        if (isIP(DEVICE_ID.split("@")[0])) {
                            mydbHelper.InsertToConnectIP(DEVICE_ID.split("@")[0], DEVICE_ID.split("@")[1], DEVICE_ID.split("@")[2]);
                            Toast.makeText(MainActivity.this, "連線資料更新成功！", Toast.LENGTH_SHORT).show();
                        }
                    } else if (!mydbHelper.CheckExistDEVICE_ID(DEVICE_ID)) {
                        Toast.makeText(MainActivity.this, "無此設備代碼", Toast.LENGTH_SHORT).show();
                    } else {
                        SPS_ID = DEVICE_ID.substring(0, 4);
                        Intent intenting = new Intent();
                        intenting.setClass(MainActivity.this, AfterLogin.class);
                        intenting.putExtra("DEVICE_ID", DEVICE_ID);//傳遞DEVICE_ID給登入後的頁面
                        intenting.putExtra("SPS_ID", SPS_ID);//傳遞SPS_ID給登入後的頁面
                        startActivityForResult(intenting, 0);
                    }
                } catch (Exception ex) {
                    WriteLog.appendLog("MainActivity.java/onPrimaryClipChanged/Exception:" + ex.toString());
                    Toast.makeText(MainActivity.this, "登入錯誤", Toast.LENGTH_SHORT).show();
                }
            }
        });//END BTNLOGIN
    }//END ONCREATE

    @Override
    protected void onResume() {
        super.onResume();
        cbMgr.removePrimaryClipChangedListener(mPrimaryClipChangedListener);
        cbMgr.addPrimaryClipChangedListener(mPrimaryClipChangedListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cbMgr.removePrimaryClipChangedListener(mPrimaryClipChangedListener);
    }

    //檢查場站的cSpsInfo是否有進行更新
    public boolean CheckSpsInfoData() {
        try {
            if (con == null) {
                WriteLog.appendLog("MainActivity.java/CheckSpsInfoData/con為null");
                return false;
            } else {
                //讀取中央cSpsInfo資料數
                String query = "SELECT COUNT(*) AS rowcounts from cSpsInfo";
                stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                rs.next();
                int ResultCount = rs.getInt("rowcounts");
                rs.close();
                //假如目前SQLite內的cSpsInfo數量和中央的不同，則刪除SQLITE的cSpsInfo資料，重新插入
                if (mydbHelper.GetSpsInfoNumber() != ResultCount) {
                    mydbHelper.DeleteSpsInfo();
                    mydbHelper.InsertToSpsInfo();
                    return true;
                } else {
                    //更新MODIFYDT不為NULL的資料
                    mydbHelper.UpdateSpsInfo();
                    return true;
                }
            }
        } catch (Exception ex) {
            WriteLog.appendLog("MainActivity.java/CheckSpsInfoData/Exception:" + ex.toString());
            return false;
        }
    }

    //檢查場站的cStatioConf是否有進行更新
    public boolean CheckStationConfData() {
        try {
            if (con == null) {
                WriteLog.appendLog("MainActivity.java/CheckStationConfData/con為null");
                return false;
            } else {
                //讀取場站cStationConf資料數
                String query = "SELECT COUNT(*) AS rowcounts from cStationConf";
                stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                rs.next();
                int ResultCount = rs.getInt("rowcounts");
                rs.close();
                //假如目前SQLite內的cStationConf數量和中央的不同，則刪除SQLITE的cStationConf資料，重新插入
                if (mydbHelper.GetStationConfNumber() != ResultCount) {
                    mydbHelper.DeleteStationConf();
                    mydbHelper.InsertToStationConf();
                    return true;
                } else {
                    //更新MODIFYDT不為NULL的資料
                    mydbHelper.UpdateStationConf();
                    return true;
                }
            }
        } catch (Exception ex) {
            WriteLog.appendLog("MainActivity.java/CheckStationConfData/Exception:" + ex.toString());
            return false;
        }
    }

    //檢查場站的cTicketKind是否有進行更新
    public boolean CheckTicketKindData() {
        try {
            if (con == null) {
                WriteLog.appendLog("MainActivity.java/CheckTicketKindData/con為null");
                return false;
            } else {
                //讀取場站cTicketKind資料數
                String query = "SELECT COUNT(*) AS rowcounts from cTicketKind";
                stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                rs.next();
                int ResultCount = rs.getInt("rowcounts");
                rs.close();
                //假如目前SQLite內的cTicketKind數量和中央的不同，則刪除SQLITE的cTicketKind資料，重新插入
                if (mydbHelper.GetTicketKindNumber() != ResultCount) {
                    mydbHelper.DeleteTicketKind();
                    mydbHelper.InsertTocTicketKind();
                    return true;
                } else {
                    //更新MODIFYDT不為NULL的資料
                    mydbHelper.UpdateTicketKind();
                    return true;
                }
            }
        } catch (Exception ex) {
            WriteLog.appendLog("MainActivity.java/CheckTicketKindData/Exception:" + ex.toString());
            return false;
        }
    }

    //於登入頁面按下返回鍵後跳出確認視窗
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            ConfirmExit();//按返回鍵，則執行退出確認
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void ConfirmExit() {//退出確認
        AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
        ad.setTitle("離開");
        ad.setMessage("確定要離開此程式嗎?");
        ad.setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
            public void onClick(DialogInterface dialog, int i) {
                WriteLog.appendLog("MainActivity.java/應用程式關閉");
                MainActivity.this.finish();//關閉activity
                System.exit(0);
            }
        });
        ad.setNegativeButton("否", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                //不退出不用執行任何操作
            }
        });
        ad.show();//顯示對話框
    }

    public boolean isIP(String addr) {
        if (addr.length() < 7 || addr.length() > 15) {
            return false;
        }
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pat = Pattern.compile(rexp);
        Matcher mat = pat.matcher(addr);
        boolean isipAddress = mat.find();
        return isipAddress;
    }

    //震動
    public void setVibrate(int time) {
        Vibrator myVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        myVibrator.vibrate(time);
    }

    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }

    private boolean pingIP(String IP) {
        try {
            Process process = new ProcessBuilder().command("/system/bin/ping", "-c 2", IP)
                    .redirectErrorStream(true)
                    .start();
            try {
                int status = process.waitFor();
                if (status == 0) {
                    //ping的通就繼續
                    return true;
                } else {
                    return false;
                }
            } catch (Exception ex) {
                WriteLog.appendLog("MainActivity.java/ping/Exception:" + ex.toString());
                return false;
            } finally {
                // 記得要釋放掉 process
                process.destroy();
            }
        } catch (Exception ex) {
            WriteLog.appendLog("MainActivity.java/ping/Exception:" + ex.toString());
            return false;
        }
    }

    //複製db到電腦檢查資料用，如果無法直接存取裝置的database資料夾的話就可以用到
    /*private void copyDbToExternal(Context context) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + context.getApplicationContext().getPackageName() + "//databases//"
                        + "mydata.db";
                String backupDBPath = "mydata.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}//END CLASS
