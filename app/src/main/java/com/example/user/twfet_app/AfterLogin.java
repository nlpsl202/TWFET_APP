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

import com.example.user.afc_nmp.R;

import java.sql.CallableStatement;
import java.sql.Connection;

/**
 * Created by USER on 2015/11/17.
 */
public class AfterLogin  extends Activity {

    Button OnlineTicketBtn,OffineTicketBtn,OnlineTicketCheckBtn,OfflineExportBtn,BluetoothConBtn,BluetoothTicketBtn;
    TextView UserDeviceTxt,InternetStatusTxt,BluetoothStatusTxt;
    String SPS_ID,DEVICE_ID;
    private MyDBHelper mydbHelper;

    //SQL SERVER //建立連線
    ConnectionClass connectionClass;
    Connection con;
    Dialog alertDialog;

    //定時上傳驗票人數
    private static long mainTimerInterval = 10000; //預設300秒
    private Handler handler = new Handler();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.after_login);

        UserDeviceTxt=(TextView)findViewById(R.id.UserDeviceTxt);
        InternetStatusTxt=(TextView)findViewById(R.id.InternetStatusTxt);
        BluetoothStatusTxt=(TextView)findViewById(R.id.BluetoothStatusTxt);

        //取得從登入頁面傳送來的使用者帳號
        Intent intent = getIntent();
        SPS_ID = intent.getStringExtra("SPS_ID");
        DEVICE_ID= intent.getStringExtra("DEVICE_ID");

        UserDeviceTxt.setText("園區代碼：  "+SPS_ID+"\n"+"閘門代碼：  "+DEVICE_ID);

        //SQLITE
        mydbHelper = new MyDBHelper(this);

        connectionClass = new ConnectionClass();
        con= connectionClass.CONN();

        OnlineTicketBtn=(Button)findViewById(R.id.OnlineTicketBtn);
        OffineTicketBtn=(Button)findViewById(R.id.OffineTicketBtn);
        OnlineTicketCheckBtn=(Button)findViewById(R.id.OnlineTicketCheckBtn);
        OfflineExportBtn=(Button)findViewById(R.id.OfflineExportBtn);

        //切換至連線驗票
        OnlineTicketBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mydbHelper.GetUltraLight03NOTOKNumber() > 0){
                    alertDialog = new Dialog(AfterLogin.this);
                    alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    alertDialog.setContentView(R.layout.after_login_alert);
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    Button a =(Button)alertDialog.findViewById(R.id.ConfirmBtn);
                    a.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.cancel();
                        }
                    });
                    alertDialog.show();
                }else{
                    Intent callSub = new Intent();
                    callSub.setClass(AfterLogin.this, OnlineTickets.class);
                    callSub.putExtra("SPS_ID", SPS_ID);
                    callSub.putExtra("DEVICE_ID",DEVICE_ID);
                    startActivityForResult(callSub, 0);
                    //startActivity(callSub);
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
                callSub.putExtra("DEVICE_ID",DEVICE_ID);
                startActivityForResult(callSub, 0);
            }

        });

        //票券狀態查詢
        OnlineTicketCheckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callSub = new Intent();
                callSub.setClass(AfterLogin.this, OnlineTicketsCheck.class);
                callSub.putExtra("SPS_ID", SPS_ID);
                startActivityForResult(callSub, 0);
            }
        });

        //資料匯出
        OfflineExportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callSub = new Intent();
                callSub.setClass(AfterLogin.this, OfflineExport.class);
                callSub.putExtra("SPS_ID", SPS_ID);
                startActivityForResult(callSub, 0);
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectionReceiver, intentFilter);

        //設定定時要執行的方法
        handler.removeCallbacks(insertToHandInCount);
        //設定Delay的時間
        handler.postDelayed(insertToHandInCount, 5000);
    }//END ONCREATE

    //定時計算驗票人數
    private Runnable insertToHandInCount = new Runnable() {
        public void run() {
            insertToHandInCount();
            handler.postDelayed(this, mainTimerInterval);
        }
    };

    //定時計算驗票人數
    private void insertToHandInCount()
    {
        try {
            CallableStatement cstmtUDS = con.prepareCall("{ call dbo.SP_GATE_HandInCounting(?)}");
            cstmtUDS.setString("DEVICE_ID",DEVICE_ID);
            cstmtUDS.execute();
        }catch (Exception e) {
            WriteLog.appendLog("AfterLogin.java/insertToHandInCount/Exception:" + e.toString());
        }
    }

    //於登入頁面按下返回鍵後跳出確認視窗
    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            ConfirmExit();//按返回鍵，則執行退出確認
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void ConfirmExit(){//退出確認
        AlertDialog.Builder ad=new AlertDialog.Builder(AfterLogin.this);
        ad.setTitle("離開");
        ad.setMessage("確定要離開此程式嗎?");
        ad.setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
            public void onClick(DialogInterface dialog, int i) {
                // TODO Auto-generated method stub
                Log.d("AfterLogin.java", "應用程式關閉");
                WriteLog.appendLog("AfterLogin.java/應用程式關閉");
                AfterLogin.this.finish();//關閉activity
            }
        });
        ad.setNegativeButton("否",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                //不退出不用執行任何操作
            }
        });
        ad.show();//顯示對話框
    }*/

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
            if (mNetworkInfo != null) {
                OnlineTicketBtn.setEnabled(true);
                OnlineTicketCheckBtn.setEnabled(true);
                OfflineExportBtn.setEnabled(true);
                InternetStatusTxt.setText("已連線");
                InternetStatusTxt.setTextColor(Color.parseColor("#4caf50"));
                //Toast.makeText(AfterLogin.this, "已連線", Toast.LENGTH_SHORT).show();
            }else{
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

