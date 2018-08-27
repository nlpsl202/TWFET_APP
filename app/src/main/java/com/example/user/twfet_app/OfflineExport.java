package com.example.user.twfet_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.twfet_app.R;

import java.sql.Connection;

/**
 * Created by USER on 2015/11/20.
 */
public class OfflineExport extends Activity {
    Button ImportBtn,ReturnBtn,HomeBtn;
    TextView ResultTxt,ResultTxt2;
    private MyDBHelper mydbHelper;
    //SQL SERVER //建立連線
    ConnectionClass connectionClass;
    Connection con;
    Dialog alertDialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.offline_export);

        ReturnBtn=(Button)findViewById(R.id.ReturnBtn);
        ImportBtn=(Button)findViewById(R.id.ImportBtn);
        HomeBtn=(Button)findViewById(R.id.HomeBtn);
        ResultTxt=(TextView) findViewById(R.id.ResultTxt);
        ResultTxt2=(TextView) findViewById(R.id.ResultTxt2);

        //SQLITE
        mydbHelper = new MyDBHelper(this);

        //連接中央資料庫
        connectionClass = new ConnectionClass();
        con= connectionClass.CONN();

        ImportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkInternetConnect()) {
                    //判斷SQLITE內是否有UltraLight03內TRANSFER_STATUS不為OK的資料（待上傳的意思）
                    if (mydbHelper.GetUltraLight03NOTOKNumber() > 0) {
                        alertDialog = new Dialog(OfflineExport.this);
                        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        alertDialog.setContentView(R.layout.offline_export_alert);
                        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        Button a =(Button)alertDialog.findViewById(R.id.ConfirmBtn);
                        a.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int getNumber=0;
                                mydbHelper.SelectFromUltraLight03();
                                getNumber=mydbHelper.GetUltraLight03ExpNmber();
                                if(getNumber<=0){
                                    ResultTxt.setText("匯出失敗！");
                                    ResultTxt2.setText("無有效上傳資料！");
                                }else{
                                    ResultTxt.setText("成功匯出！");
                                    ResultTxt2.setText("成功上傳離線驗票資料 " + getNumber + " 筆！");
                                }
                                //AlertDialog.Builder alertad = new AlertDialog.Builder(OfflineExport.this);
                                //alertad.setTitle("離線資料上傳");
                                //alertad.setMessage("成功上傳離線驗票資料" + getNumber + "筆！");
                                WriteLog.appendLog("OfflineExport.java/成功上傳離線驗票資料" + getNumber + "筆！");
                                //alertad.setPositiveButton("關閉", new DialogInterface.OnClickListener() {
                                //    public void onClick(DialogInterface dialog, int i) { }
                                //});
                                //alertad.show();//顯示對話框
                                mydbHelper.DeleteUltraLight03Exp();//清除已顯示過的匯出紀錄
                                alertDialog.cancel();
                            }
                        });
                        Button b =(Button)alertDialog.findViewById(R.id.CancelBtn);
                        b.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.cancel();
                            }
                        });
                        alertDialog.show();

                        /*AlertDialog.Builder alertad = new AlertDialog.Builder(OfflineExport.this);
                        alertad.setTitle("離線資料上傳");
                        alertad.setMessage("是否將離線資料上傳，上傳後將清除出入館紀錄！");
                        alertad.setPositiveButton("是", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int i) {
                                int getNumber=0;
                                mydbHelper.SelectFromUltraLight03();
                                getNumber=mydbHelper.GetUltraLight03ExpNmber();
                                AlertDialog.Builder alertad = new AlertDialog.Builder(OfflineExport.this);
                                alertad.setTitle("離線資料上傳");
                                alertad.setMessage("成功上傳離線驗票資料" + getNumber + "筆！");
                                WriteLog.appendLog("OfflineExport.java/成功上傳離線驗票資料" + getNumber + "筆！");
                                alertad.setPositiveButton("關閉", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int i) { }
                                });
                                alertad.show();//顯示對話框
                                mydbHelper.DeleteUltraLight03Exp();//請除已顯示過的匯出紀錄
                            }
                        });
                        alertad.setNegativeButton("否", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int i) { }
                        });
                        alertad.show();//顯示對話框*/
                    }else{
                        ResultTxt.setText("無法匯出！");
                        ResultTxt2.setText("目前無離線驗票資料！");
                        /*AlertDialog.Builder alertad = new AlertDialog.Builder(OfflineExport.this);
                        alertad.setTitle("離線資料上傳");
                        alertad.setMessage("目前無離線驗票資料！");
                        alertad.setPositiveButton("關閉", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int i) { }
                        });
                        alertad.show();//顯示對話框*/
                    }
                }else
                {
                    AlertDialog.Builder alertad = new AlertDialog.Builder(OfflineExport.this);
                    alertad.setTitle("無法匯出");
                    alertad.setMessage("請確認網路狀態！");
                    alertad.setPositiveButton("關閉", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int i) { }
                    });
                    alertad.show();//顯示對話框
                }
            }
        });

        ReturnBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        HomeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectionReceiver, intentFilter);
    }

    //檢查網路是否連線
    public boolean checkInternetConnect(){
        ConnectivityManager cManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        boolean isWifi=cManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        return isWifi;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(connectionReceiver);
    }

    private final BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = connectMgr.getActiveNetworkInfo();
            if (mNetworkInfo == null) {
                Toast.makeText(OfflineExport.this, "連線中斷", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    };
}
