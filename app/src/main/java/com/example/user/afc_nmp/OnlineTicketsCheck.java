package com.example.user.afc_nmp;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.security.spec.AlgorithmParameterSpec;
import java.sql.CallableStatement;
import java.sql.Connection;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by USER on 2015/11/19.
 */
public class OnlineTicketsCheck extends Activity {
    private static String key="SET31275691$00000000000000000000";
    TextView ResultTxt,PeopleNumTxt;
    String result="",SPS_ID;
    Button ReturnBtn,HomeBtn;

    //SQL SERVER
    ConnectionClass connectionClass;
    Connection con;

    //SQLITE
    private MyDBHelper mydbHelper;

    //剪貼簿
    private ClipboardManager cbMgr;
    private ClipboardManager.OnPrimaryClipChangedListener mPrimaryClipChangedListener;

    //RFID
    NfcAdapter mNfcAdapter;
    PendingIntent mPendingIntent;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.online_tickets_check);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

        //取得上個頁面傳來的值
        Intent intent = getIntent();
        SPS_ID=intent.getStringExtra("SPS_ID");

        ResultTxt=(TextView) findViewById(R.id.ResultTxt);
        PeopleNumTxt=(TextView) findViewById(R.id.PeopleNumTxt);
        ReturnBtn=(Button)findViewById(R.id.ReturnBtn);
        HomeBtn=(Button)findViewById(R.id.HomeBtn);

        //SQLITE
        mydbHelper = new MyDBHelper(this);

        //SQL SERVER
        connectionClass = new ConnectionClass();
        con= connectionClass.CONN();

        //查詢館內人數
        PeopleNumTxt.setText("目前館內人數 "+mydbHelper.executePeopleNumStoredProcedure(con)+" 人");
        PeopleNumTxt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                PeopleNumTxt.setText("目前館內人數 "+mydbHelper.executePeopleNumStoredProcedure(con)+" 人");
            }
        });

        //掃描驗票
        cbMgr=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        mPrimaryClipChangedListener =new ClipboardManager.OnPrimaryClipChangedListener(){
            public void onPrimaryClipChanged() {
                try{
                    setVibrate(100);
                    String qr=cbMgr.getPrimaryClip().getItemAt(0).getText().toString();
                    String a=qr.substring(0,qr.length()-16);
                    String iv=qr.substring(qr.length() - 16);
                    byte[] descryptBytes=decryptAES(iv.getBytes("UTF-8"),key.getBytes("UTF-8"), Base64.decode(a, Base64.DEFAULT));
                    String getdata = new String(descryptBytes);
                    String TICKET_NO=getdata.split("@")[4];
                    connectionClass = new ConnectionClass();
                    con= connectionClass.CONN();
                    CallableStatement cstmt = con.prepareCall("{ call dbo.SP_TVM_TicketStateQuery(?,?,?,?,?,?,?,?)}");
                    cstmt.setString(1,TICKET_NO);
                    cstmt.setString(2,SPS_ID);
                    cstmt.setString(3,"");
                    cstmt.registerOutParameter(4, java.sql.Types.VARCHAR);
                    cstmt.registerOutParameter(5, java.sql.Types.VARCHAR);
                    cstmt.registerOutParameter(6, java.sql.Types.VARCHAR);
                    cstmt.registerOutParameter(7, java.sql.Types.VARCHAR);
                    cstmt.registerOutParameter(8, java.sql.Types.VARCHAR);
                    cstmt.execute();
                    String RETURN_MSG = cstmt.getString(4);
                    String RETURN_MSG_DATETIME = cstmt.getString(5);
                    String TK_NAME = cstmt.getString(6);
                    cstmt.close();
                    if (RETURN_MSG.indexOf("可入") > -1) {
                        setResultText(result = "票券狀態    " + RETURN_MSG + "\n\n票券號碼    " +TICKET_NO + "\n\n票券種類    " + TK_NAME);
                    }else{
                        ResultTxt.setTextColor(Color.BLACK);
                        String text="";
                        if(RETURN_MSG.indexOf("已入場") > -1){
                            text = "票券狀態    "+RETURN_MSG+"\n\n票券號碼    "+TICKET_NO+"\n\n票券種類    "+TK_NAME+"\n\n票券入場紀錄\n\n"+RETURN_MSG_DATETIME;
                        }else{
                            text = "票券狀態    "+RETURN_MSG;
                        }
                        Spannable spannable = new SpannableString(text);
                        spannable.setSpan(new ForegroundColorSpan(Color.RED), 8, 8+RETURN_MSG.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ResultTxt.setText(spannable, TextView.BufferType.SPANNABLE);
                    }
                }catch(Exception ex){
                    try{
                        String qr=cbMgr.getPrimaryClip().getItemAt(0).getText().toString();
                        connectionClass = new ConnectionClass();
                        con= connectionClass.CONN();
                        CallableStatement cstmt = con.prepareCall("{ call dbo.SP_TVM_TicketStateQuery(?,?,?,?,?,?,?,?)}");
                        cstmt.setString(1,qr);
                        cstmt.setString(2,SPS_ID);
                        cstmt.setString(3,"");
                        cstmt.registerOutParameter(4, java.sql.Types.VARCHAR);
                        cstmt.registerOutParameter(5, java.sql.Types.VARCHAR);
                        cstmt.registerOutParameter(6, java.sql.Types.VARCHAR);
                        cstmt.registerOutParameter(7, java.sql.Types.VARCHAR);
                        cstmt.registerOutParameter(8, java.sql.Types.VARCHAR);
                        cstmt.execute();
                        String RETURN_MSG = cstmt.getString(4);
                        String RETURN_MSG_DATETIME = cstmt.getString(5);
                        String TK_NAME = cstmt.getString(6);
                        String TICKET_NO=cstmt.getString(7);
                        cstmt.close();
                        if (RETURN_MSG.indexOf("可入") > -1) {
                            setResultText(result = "票券狀態    " + RETURN_MSG + "\n\n票券號碼    " +TICKET_NO + "\n\n票券種類    " + TK_NAME);
                        }else{
                            ResultTxt.setTextColor(Color.BLACK);
                            String text="";
                            if(RETURN_MSG.indexOf("已入場") > -1){
                                text = "票券狀態    "+RETURN_MSG+"\n\n票券號碼    "+TICKET_NO+"\n\n票券種類    "+TK_NAME+"\n\n票券入場紀錄\n\n"+RETURN_MSG_DATETIME;
                            }else{
                                text = "票券狀態    "+RETURN_MSG;
                            }
                            Spannable spannable = new SpannableString(text);
                            spannable.setSpan(new ForegroundColorSpan(Color.RED), 8, 8+RETURN_MSG.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            ResultTxt.setText(spannable, TextView.BufferType.SPANNABLE);
                        }
                    }
                    catch(Exception x){
                        ResultTxt.setTextColor(Color.BLACK);
                        String text = "票券狀態    非花博票券條碼！";
                        Spannable spannable = new SpannableString(text);
                        spannable.setSpan(new ForegroundColorSpan(Color.RED), 8, 16, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ResultTxt.setText(spannable, TextView.BufferType.SPANNABLE);
                    }
                }
            }
        };
        cbMgr.addPrimaryClipChangedListener(mPrimaryClipChangedListener);

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

        //RFID
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            //nfc not support your device.
            return;
        }
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }//End ON CREATE

    @Override
    protected void onResume() {
        super.onResume();
        mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    @Override
    protected void onNewIntent(Intent intent){
        getTagInfo(intent);
    }
    private void getTagInfo(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String tagNo="";
        byte[] tagId = tag.getId();
        for(int i=0; i<tagId.length; i++){
            if(Integer.toHexString(tagId[i] & 0xFF).length()<2){
                tagNo +="0";
            }
            tagNo += Integer.toHexString(tagId[i] & 0xFF);
        }

        try{
            connectionClass = new ConnectionClass();
            con= connectionClass.CONN();
            CallableStatement cstmt = con.prepareCall("{ call dbo.SP_TVM_TicketStateQuery(?,?,?,?,?,?,?,?)}");
            cstmt.setString(1,"");
            cstmt.setString(2,SPS_ID);
            cstmt.setString(3,tagNo.toUpperCase());
            cstmt.registerOutParameter(4, java.sql.Types.VARCHAR);
            cstmt.registerOutParameter(5, java.sql.Types.VARCHAR);
            cstmt.registerOutParameter(6, java.sql.Types.VARCHAR);
            cstmt.registerOutParameter(7, java.sql.Types.VARCHAR);
            cstmt.registerOutParameter(8, java.sql.Types.VARCHAR);
            cstmt.execute();
            String RETURN_MSG = cstmt.getString(4);
            String TK_NAME = cstmt.getString(6);
            String TICKET_NO=cstmt.getString(7);
            String FT_NAME=cstmt.getString(8);
            cstmt.close();
            if (RETURN_MSG.indexOf("可入") > -1) {
                setResultText(result = "姓　　名    "+FT_NAME+"\n\n票券狀態    " + RETURN_MSG + "\n\n票券號碼    " +TICKET_NO + "\n\n票券種類    " + TK_NAME);
            }else{
                ResultTxt.setTextColor(Color.BLACK);
                String text = "票券狀態    "+RETURN_MSG;
                Spannable spannable = new SpannableString(text);
                spannable.setSpan(new ForegroundColorSpan(Color.RED), 8, 8+RETURN_MSG.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ResultTxt.setText(spannable, TextView.BufferType.SPANNABLE);
            }
        }catch(Exception ex){
            ResultTxt.setTextColor(Color.BLACK);
            String text = "票券狀態    非花博票券條碼！";
            Spannable spannable = new SpannableString(text);
            spannable.setSpan(new ForegroundColorSpan(Color.RED), 8, 16, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ResultTxt.setText(spannable, TextView.BufferType.SPANNABLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(connectionReceiver);
        cbMgr.removePrimaryClipChangedListener(mPrimaryClipChangedListener);
    }

    public static byte[] decryptAES (byte[] ivBytes, byte[] keyBytes,byte[] textBytes) {
        try {
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
            return cipher.doFinal(textBytes);
        } catch (Exception ex) {
            return null;
        }
    }

    private void setResultText(String text) {
        ResultTxt.setText(text);
    }

    //震動
    public void setVibrate(int time){
        Vibrator myVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        myVibrator.vibrate(time);
    }

    private final BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = connectMgr.getActiveNetworkInfo();
            if (mNetworkInfo == null) {
                Toast.makeText(OnlineTicketsCheck.this, "連線中斷", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    };
}