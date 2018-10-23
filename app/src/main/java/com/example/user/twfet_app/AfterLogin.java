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
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Jeff.
 */
public class AfterLogin extends Activity {
    private Button OnlineTicketBtn, OffineTicketBtn, OnlineTicketCheckBtn, OfflineExportBtn;
    private TextView UserDeviceTxt, InternetStatusTxt;
    private String SPS_ID, DEVICE_ID;

    //SQLite
    private MyDBHelper mydbHelper;

    //彈出視窗
    private Dialog alertDialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.after_login);

        UserDeviceTxt = (TextView) findViewById(R.id.UserDeviceTxt);
        InternetStatusTxt = (TextView) findViewById(R.id.InternetStatusTxt);
        OnlineTicketBtn = (Button) findViewById(R.id.OnlineTicketBtn);
        OffineTicketBtn = (Button) findViewById(R.id.OffineTicketBtn);
        OnlineTicketCheckBtn = (Button) findViewById(R.id.OnlineTicketCheckBtn);
        OfflineExportBtn = (Button) findViewById(R.id.OfflineExportBtn);

        mydbHelper = new MyDBHelper(this);

        //取得從登入頁面傳送來的使用者帳號
        Intent intent = getIntent();
        SPS_ID = intent.getStringExtra("SPS_ID");
        DEVICE_ID = intent.getStringExtra("DEVICE_ID");

        UserDeviceTxt.setText("園區代碼：  " + SPS_ID + "\n" + "閘門代碼：  " + DEVICE_ID);

        //切換至連線驗票
        OnlineTicketBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mydbHelper.GetUltraLight03NotOkNumber() > 0) {
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
                } catch (Exception ex) {
                    WriteLog.appendLog("AfterLogin.java/insertToHandInCount/Exception:" + ex.toString());
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

        //資料上傳
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
        //註冊網路狀態監聽
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NetworkChangeReceiver.NETWORK_CHANGE_ACTION);
        registerReceiver(connectionReceiver, intentFilter);
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(connectionReceiver);
    }

    //檢查網路是否連線
    public boolean checkInternetConnect() {
        ConnectivityManager cManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        return cManager.getActiveNetworkInfo() != null;
    }

    private final BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getStringExtra("status").contains("Not")) {
                OnlineTicketBtn.setEnabled(true);
                OnlineTicketCheckBtn.setEnabled(true);
                OfflineExportBtn.setEnabled(true);
                InternetStatusTxt.setText("已連線");
                InternetStatusTxt.setTextColor(Color.parseColor("#4caf50"));
                Toast.makeText(AfterLogin.this, "已連線", Toast.LENGTH_SHORT).show();
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

