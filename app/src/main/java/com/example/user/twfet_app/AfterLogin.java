package com.example.user.twfet_app;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.CallableStatement;
import java.sql.Connection;

/**
 * Created by jeff.
 */
public class AfterLogin extends Activity {

    Button OnlineTicketBtn, OffineTicketBtn, OnlineTicketCheckBtn, OfflineExportBtn;
    TextView UserDeviceTxt, InternetStatusTxt, BluetoothStatusTxt;
    String SPS_ID, DEVICE_ID;
    MyDBHelper mydbHelper;

    //SQL SERVER
    ConnectionClass connectionClass;
    Connection con;

    //彈出視窗
    Dialog alertDialog;

    //定時上傳驗票人數
    static long mainTimerInterval = 10000; //預設300秒
    Handler handler = new Handler();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.after_login);

        UserDeviceTxt = (TextView) findViewById(R.id.UserDeviceTxt);
        InternetStatusTxt = (TextView) findViewById(R.id.InternetStatusTxt);
        BluetoothStatusTxt = (TextView) findViewById(R.id.BluetoothStatusTxt);
        OnlineTicketBtn = (Button) findViewById(R.id.OnlineTicketBtn);
        OffineTicketBtn = (Button) findViewById(R.id.OffineTicketBtn);
        OnlineTicketCheckBtn = (Button) findViewById(R.id.OnlineTicketCheckBtn);
        OfflineExportBtn = (Button) findViewById(R.id.OfflineExportBtn);

        //SQLITE
        mydbHelper = new MyDBHelper(this);

        //SQL SERVER
        connectionClass = new ConnectionClass();
        con = connectionClass.CONN();

        //取得從登入頁面傳送來的使用者帳號
        Intent intent = getIntent();
        SPS_ID = intent.getStringExtra("SPS_ID");
        DEVICE_ID = intent.getStringExtra("DEVICE_ID");

        UserDeviceTxt.setText("園區代碼：  " + SPS_ID + "\n" + "閘門代碼：  " + DEVICE_ID);

        if (checkInternetConnect()) {
            OnlineTicketBtn.setEnabled(true);
            OnlineTicketCheckBtn.setEnabled(true);
            OfflineExportBtn.setEnabled(true);
            InternetStatusTxt.setText("已連線");
            InternetStatusTxt.setTextColor(Color.parseColor("#4caf50"));
        } else {
            OnlineTicketBtn.setEnabled(false);
            OnlineTicketCheckBtn.setEnabled(false);
            OfflineExportBtn.setEnabled(false);
            InternetStatusTxt.setText("未連線");
            InternetStatusTxt.setTextColor(Color.RED);
        }

        //設定定時要執行的方法
        handler.removeCallbacks(insertToHandInCount);
        //設定Delay的時間
        handler.postDelayed(insertToHandInCount, 5000);

        //切換至連線驗票
        OnlineTicketBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mydbHelper.GetUltraLight03NOTOKNumber() > 0) {
                        alertDialog = new Dialog(AfterLogin.this);
                        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        alertDialog.setContentView(R.layout.after_login_alert);
                        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        Button a = (Button) alertDialog.findViewById(R.id.ConfirmBtn);
                        a.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.cancel();
                            }
                        });
                        alertDialog.show();
                    } else {
                        Intent callSub = new Intent();
                        callSub.setClass(AfterLogin.this, OnlineTickets.class);
                        callSub.putExtra("SPS_ID", SPS_ID);
                        callSub.putExtra("DEVICE_ID", DEVICE_ID);
                        startActivity(callSub);
                    }
                } catch (Exception e) {
                    WriteLog.appendLog("AfterLogin.java/insertToHandInCount/Exception:" + e.toString());
                }
            }

        });

        //切換至離線驗票
        OffineTicketBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callSub = new Intent();
                callSub.setClass(AfterLogin.this, OfflineTickets.class);
                callSub.putExtra("SPS_ID", SPS_ID);
                callSub.putExtra("DEVICE_ID", DEVICE_ID);
                startActivity(callSub);
            }

        });

        //票券狀態查詢
        OnlineTicketCheckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callSub = new Intent();
                callSub.setClass(AfterLogin.this, OnlineTicketsCheck.class);
                callSub.putExtra("SPS_ID", SPS_ID);
                startActivity(callSub);
            }
        });

        //資料匯出
        OfflineExportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callSub = new Intent();
                callSub.setClass(AfterLogin.this, OfflineExport.class);
                callSub.putExtra("SPS_ID", SPS_ID);
                startActivity(callSub);
            }
        });
    }//END ONCREATE

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectionReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(connectionReceiver);
    }

    //定時計算驗票人數
    private Runnable insertToHandInCount = new Runnable() {
        public void run() {
            insertToHandInCount();
            handler.postDelayed(this, mainTimerInterval);
        }
    };

    //定時計算驗票人數
    private void insertToHandInCount() {
        try {
            CallableStatement cstmtUDS = con.prepareCall("{ call dbo.SP_GATE_HandInCounting(?)}");
            cstmtUDS.setString("DEVICE_ID", DEVICE_ID);
            cstmtUDS.execute();
        } catch (Exception e) {
            WriteLog.appendLog("AfterLogin.java/insertToHandInCount/Exception:" + e.toString());
        }
    }

    //檢查網路是否連線
    public boolean checkInternetConnect() {
        ConnectivityManager cManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        return cManager.getActiveNetworkInfo() != null;
    }

    private final BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = connectMgr.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                OnlineTicketBtn.setEnabled(true);
                OnlineTicketCheckBtn.setEnabled(true);
                OfflineExportBtn.setEnabled(true);
                InternetStatusTxt.setText("已連線");
                InternetStatusTxt.setTextColor(Color.parseColor("#4caf50"));
                //Toast.makeText(AfterLogin.this, "已連線", Toast.LENGTH_SHORT).show();
            } else {
                OnlineTicketBtn.setEnabled(false);
                OnlineTicketCheckBtn.setEnabled(false);
                OfflineExportBtn.setEnabled(false);
                InternetStatusTxt.setText("未連線");
                InternetStatusTxt.setTextColor(Color.RED);
                Toast.makeText(AfterLogin.this, "連線中斷", Toast.LENGTH_SHORT).show();
            }
        }
    };
}

