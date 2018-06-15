package com.example.user.afc_nmp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class MainActivity extends Activity {
    EditText SPS_ID_Edt;
    Button btnlogin,btnDBIPBtn;
    //建立連線
    Connection con;
    Statement stmt;
    //SQLite
    private MyDBHelper mydbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("MainActivity.java","應用程式開啟");
        WriteLog.appendLog("MainActivity.java/應用程式開啟");

        //設定自訂的TITLE BAR
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

        //SQLite
        mydbHelper = new MyDBHelper(this);
        mydbHelper.InsertToConnectIP("172.16.30.181");
        if(mydbHelper.GetConnectIP()==""){
            Toast.makeText(MainActivity.this, "請先進行資料庫連線設定！", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity.java","尚未設定資料庫連線IP位置！");
            WriteLog.appendLog("MainActivity.java/尚未設定資料庫連線IP位置！");
        }else {
            ConnectionClass.ip = mydbHelper.GetConnectIP().trim();
            con = ConnectionClass.CONN();
            if(!CheckSpsInfoData()){
                Toast.makeText(MainActivity.this, "無法更新資料庫", Toast.LENGTH_SHORT).show();
            }else if (!CheckStationConfData()) {
                Toast.makeText(MainActivity.this, "無法更新資料庫", Toast.LENGTH_SHORT).show();
            }else if (!CheckTicketKindData()) {
                Toast.makeText(MainActivity.this, "無法更新資料庫", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "資料庫已更新。", Toast.LENGTH_SHORT).show();
            }
        }

        SPS_ID_Edt = (EditText) findViewById(R.id.SPS_ID_Edt);
        btnlogin = (Button) findViewById(R.id.LoginBtn);
        //btnDBIPBtn=(Button) findViewById(R.id.DBIPBtn);

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mydbHelper.CheckExistSPS_ID(SPS_ID_Edt.getText().toString())){
                    Toast.makeText(MainActivity.this, "無此園區代碼", Toast.LENGTH_SHORT).show();
                }else{
                    Intent intenting = new Intent();
                    intenting.setClass(MainActivity.this, AfterLogin.class);
                    intenting.putExtra("SPS_ID",SPS_ID_Edt.getText().toString());//傳遞SPS_ID給登入後的葉面
                    startActivityForResult(intenting,0);
                    //MainActivity.this.finish();
                }
            }
        });//END BTNLOGIN

        /*btnDBIPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callSub = new Intent();
                callSub.setClass(MainActivity.this, ConnectSetting.class);
                startActivityForResult(callSub, 0);
            }
        });*/

        //產生db用來檢視資料
        copyDbToExternal(this);
    }//END ONCREATE

    @Override
    public void onRestart(){
        super.onRestart();
        ConnectionClass.ip = mydbHelper.GetConnectIP().trim();
        con = ConnectionClass.CONN();
        if(!CheckSpsInfoData()){
            Toast.makeText(MainActivity.this, "無法更新資料庫", Toast.LENGTH_SHORT).show();
        }else if (!CheckStationConfData()) {
            Toast.makeText(MainActivity.this, "無法更新資料庫", Toast.LENGTH_SHORT).show();
        }else if (!CheckTicketKindData()) {
            Toast.makeText(MainActivity.this, "無法更新資料庫", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "資料庫已更新。", Toast.LENGTH_SHORT).show();
        }
    }

    //檢查場站的cSpsInfo是否有進行更新
    public boolean CheckSpsInfoData(){
        try {
            if (con == null) {
                Log.d("MainActivity.java","CON為NULL");
                WriteLog.appendLog("MainActivity.java/SPSINFO/con為null");
                return false;
                //Toast.makeText(MainActivity.this,  "離線作業，無法取得網路資料庫", Toast.LENGTH_SHORT).show();
            } else {
                //讀取場站SpsInfo資料數
                String query = "SELECT COUNT(*) AS rowcounts from cSpsInfo";
                stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                rs.next();
                // Get the rowcount column value.
                int ResultCount = rs.getInt("rowcounts") ;
                rs.close() ;
                //假如目前SQLite內的StatioConf數量和場站的不同，則DROP掉SQLITE的StatioConf，重新產生StatioConf
                if(mydbHelper.GetSpsInfoNumber()!=ResultCount)
                {
                    //刪除SQLITE
                    mydbHelper.DeleteSpsInfo();
                    //重新插入
                    mydbHelper.InsertToSpsInfo();
                    return true;
                    //Toast.makeText(MainActivity.this,  "AFCAC資料庫已更新。", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //更新MODIFYDT不為NULL的資料
                    mydbHelper.UpdateSpsInfo();
                    return  true;
                    //Toast.makeText(MainActivity.this,  "AFCAC資料庫已更新2。", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception ex) {
            Log.d("MainActivity.java","Exception:"+ex);
            WriteLog.appendLog("MainActivity.java/SPSINFO/Exception:"+ex);
            return false;
            //Toast.makeText(MainActivity.this,  "Exception "+ex, Toast.LENGTH_SHORT).show();
        }
    }

    //檢查場站的cStatioConf是否有進行更新
    public boolean CheckStationConfData(){
        try {
            if (con == null) {
                Log.d("MainActivity.java","CON為NULL");
                WriteLog.appendLog("MainActivity.java/STATIONCONF/con為null");
                return false;
                //Toast.makeText(MainActivity.this,  "離線作業，無法取得網路資料庫", Toast.LENGTH_SHORT).show();
            } else {
                //讀取場站StatioConf資料數
                String query = "SELECT COUNT(*) AS rowcounts from cStationConf";
                stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                rs.next();
                // Get the rowcount column value.
                int ResultCount = rs.getInt("rowcounts") ;
                rs.close() ;
                //假如目前SQLite內的StatioConf數量和場站的不同，則DROP掉SQLITE的StatioConf，重新產生StatioConf
                if(mydbHelper.GetStationConfNumber()!=ResultCount)
                {
                    //刪除SQLITE
                    mydbHelper.DeleteStationConf();
                    //重新插入
                    mydbHelper.InsertToStationConf();
                    return true;
                    //Toast.makeText(MainActivity.this,  "AFCAC資料庫已更新。", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //更新MODIFYDT不為NULL的資料
                    mydbHelper.UpdateStationConf();
                    return  true;
                    //Toast.makeText(MainActivity.this,  "AFCAC資料庫已更新2。", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception ex) {
            Log.d("MainActivity.java","Exception:"+ex);
            WriteLog.appendLog("MainActivity.java/STATIONCONF/Exception:"+ex);
            return false;
            //Toast.makeText(MainActivity.this,  "Exception "+ex, Toast.LENGTH_SHORT).show();
        }
    }

    //檢查場站的cTicketKind是否有進行更新
    public boolean CheckTicketKindData(){
        try {
            if (con == null) {
                Log.d("MainActivity.java","CON為NULL");
                WriteLog.appendLog("MainActivity.java/TICKETKIND/con為null");
                return false;
                //Toast.makeText(MainActivity.this,  "離線作業，無法取得網路資料庫", Toast.LENGTH_SHORT).show();
            } else {
                //讀取場站StatioConf資料數
                String query = "SELECT COUNT(*) AS rowcounts from cTicketKind";
                stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                rs.next();
                // Get the rowcount column value.
                int ResultCount = rs.getInt("rowcounts") ;
                rs.close() ;
                //假如目前SQLite內的cTicketKind數量和場站的不同，則DROP掉SQLITE的cTicketKind，重新產生cTicketKind
                if(mydbHelper.GetTicketKindNumber()!=ResultCount)
                {
                    //刪除SQLITE
                    mydbHelper.DeleteTicketKind();
                    //重新插入
                    mydbHelper.InsertTocTicketKind();
                    return true;
                    //Toast.makeText(MainActivity.this,  "AFCAC資料庫已更新。", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //更新MODIFYDT不為NULL的資料
                    mydbHelper.UpdateTicketKind();
                    return  true;
                    //Toast.makeText(MainActivity.this,  "AFCAC資料庫已更新2。", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception ex) {
            Log.d("MainActivity.java","Exception:"+ex);
            WriteLog.appendLog("MainActivity.java/TICKETKIND/Exception:"+ex);
            return false;
            //Toast.makeText(MainActivity.this,  "Exception "+ex, Toast.LENGTH_SHORT).show();
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

    public void ConfirmExit(){//退出確認
        AlertDialog.Builder ad=new AlertDialog.Builder(MainActivity.this);
        ad.setTitle("離開");
        ad.setMessage("確定要離開此程式嗎?");
        ad.setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
            public void onClick(DialogInterface dialog, int i) {
                // TODO Auto-generated method stub
                Log.d("MainActivity.java","應用程式關閉");
                WriteLog.appendLog("MainActivity.java/應用程式關閉");
                MainActivity.this.finish();//關閉activity
            }
        });
        ad.setNegativeButton("否",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                //不退出不用執行任何操作
            }
        });
        ad.show();//顯示對話框
    }

    //複製db到電腦檢查資料用
    private void copyDbToExternal(Context context) {
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
    }
}//END CLASS