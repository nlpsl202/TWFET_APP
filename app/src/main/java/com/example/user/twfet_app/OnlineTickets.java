package com.example.user.twfet_app;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.afc_nmp.R;

import java.security.spec.AlgorithmParameterSpec;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by USER on 2015/11/19.
 */
public class OnlineTickets extends Activity {
    private static final String key="SET31275691$00000000000000000000";
    TextView ResultTxt,ResultTxt2,ResultTxt3,PeopleNumTxt;
    ImageView FtPhotoImage;
    String result="",DEVICE_ID,SPS_ID;
    Button ReturnBtn,HomeBtn;
    LinearLayout FailedLayout,wifiLayout,rfidLayout;

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
    Bitmap bitmap;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.online_tickets);

        //取得上個頁面傳來的值
        Intent intent = getIntent();
        DEVICE_ID=intent.getStringExtra("DEVICE_ID");
        SPS_ID=intent.getStringExtra("SPS_ID");

        ResultTxt=(TextView) findViewById(R.id.ResultTxt);
        ResultTxt2=(TextView) findViewById(R.id.ResultTxt2);
        ResultTxt3=(TextView) findViewById(R.id.ResultTxt3);
        PeopleNumTxt=(TextView) findViewById(R.id.PeopleNumTxt);
        ReturnBtn=(Button)findViewById(R.id.ReturnBtn);
        HomeBtn=(Button)findViewById(R.id.HomeBtn);
        FailedLayout=(LinearLayout) findViewById(R.id.FailedLayout);
        wifiLayout=(LinearLayout) findViewById(R.id.wifiLayout);
        rfidLayout=(LinearLayout) findViewById(R.id.rfidLayout);
        FtPhotoImage=(ImageView) findViewById(R.id.FtPhotoImage);

        //SQLITE
        mydbHelper = new MyDBHelper(this);

        //SQL SERVER
        connectionClass = new ConnectionClass();
        con= connectionClass.CONN();

        //查詢館內人數
        PeopleNumTxt.setText("目前園內人數 "+mydbHelper.executePeopleNumStoredProcedure(con,SPS_ID)+" 人");
        PeopleNumTxt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                PeopleNumTxt.setText("目前園內人數 "+mydbHelper.executePeopleNumStoredProcedure(con,SPS_ID)+" 人");
            }
        });

        FailedLayout.setVisibility(View.GONE);
        wifiLayout.setVisibility(View.GONE);
        rfidLayout.setVisibility(View.GONE);

        //掃描驗票
        cbMgr=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        mPrimaryClipChangedListener =new ClipboardManager.OnPrimaryClipChangedListener(){
            public void onPrimaryClipChanged() {
                WriteLog.appendLog("testtt");
                wifiLayout.setVisibility(View.VISIBLE);
                rfidLayout.setVisibility(View.GONE);
                try{
                    setVibrate(100);
                    String qr=cbMgr.getPrimaryClip().getItemAt(0).getText().toString();
                    String a=qr.substring(0,qr.length()-16);
                    String iv=qr.substring(qr.length() - 16);
                    byte[] descryptBytes=decryptAES(iv.getBytes("UTF-8"),key.getBytes("UTF-8"), Base64.decode(a, Base64.DEFAULT));
                    String getdata = new String(descryptBytes);
                    String inTime="";
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df4 = new SimpleDateFormat("HHmm");
                    if((getdata.split("@").length>=7)){
                        inTime=getdata.split("@")[6];
                        if(inTime != null && !inTime.equals("") && Integer.parseInt(inTime.replace(":","")) > Integer.parseInt(df4.format(c.getTime()))){
                            FailedLayout.setVisibility(View.VISIBLE);
                            setResultText(result = "票券狀態    ");
                            setResultText2(result = "未到入場時間！");
                            ResultTxt2.setTextColor(Color.RED);
                            return;
                        }
                    }
                    String TICKET_NO=getdata.split("@")[4];
                    String TK_CODE=getdata.split("@")[5];

                    connectionClass = new ConnectionClass();
                    con= connectionClass.CONN();
                    CallableStatement cstmt = con.prepareCall("{ call dbo.SP_GATE_CHKCMD(?,?,?,?,?,?,?,?,?,?,?,?)}");
                    cstmt.setString(1,TICKET_NO);
                    cstmt.setString(2,DEVICE_ID.replace('G','H'));
                    cstmt.setString(3,SPS_ID);
                    cstmt.setString(4,"");
                    cstmt.setString(5,"");
                    cstmt.registerOutParameter(6, java.sql.Types.VARCHAR);
                    cstmt.registerOutParameter(7, java.sql.Types.VARCHAR);
                    cstmt.registerOutParameter(8, java.sql.Types.VARCHAR);
                    cstmt.registerOutParameter(9, java.sql.Types.VARCHAR);
                    cstmt.registerOutParameter(10, java.sql.Types.VARCHAR);
                    cstmt.registerOutParameter(11, java.sql.Types.VARCHAR);
                    cstmt.registerOutParameter(12, java.sql.Types.VARCHAR);
                    cstmt.execute();
                    String RETURN_MSG = cstmt.getString(6);
                    String TK_NAME = cstmt.getString(7);
                    //String RETURN_MSG_DATETIME = cstmt.getString(8);
                    cstmt.close();

                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMddHHmmss");

                    String[] ResultArray=new String[10];
                    if (RETURN_MSG.indexOf("OPEN") > -1) {
                        FailedLayout.setVisibility(View.GONE);
                        if(TICKET_NO.substring(0,2).equals("OG")||TICKET_NO.substring(0,2).equals("OK")||TICKET_NO.substring(0,2).equals("OJ")){
                            setResultText(result = "票券狀態    驗票成功" + "\n\n票券號碼    " +TICKET_NO+ "\n\n票券種類    " +TK_NAME+ "\n\n人數            "+ getdata.split("@")[7] +"\n\n票券入場紀錄\n\n"+df.format(c.getTime()));
                        }else{
                            setResultText(result = "票券狀態    驗票成功" + "\n\n票券號碼    " +TICKET_NO+ "\n\n票券種類    " +TK_NAME+ "\n\n票券入場紀錄\n\n"+df.format(c.getTime()));
                        }
                        ResultArray[0]="A";
                        ResultArray[1]=TICKET_NO;
                        ResultArray[2]=SPS_ID;
                        ResultArray[3]="I";
                        ResultArray[4]=DEVICE_ID.replace('G','H');
                        ResultArray[5]=TK_CODE;
                        ResultArray[6]=qr;
                        ResultArray[7]=df2.format(c.getTime());
                        ResultArray[8]="";
                        ResultArray[9]=getDateTime();
                        mydbHelper.InsertToSQLiteUltraLight03(ResultArray, "OK");
                    }else{
                        FailedLayout.setVisibility(View.VISIBLE);
                        setResultText(result = "票券狀態    ");
                        setResultText2(result = RETURN_MSG);
                    }
                }catch(Exception ex){
                    try {
                        String qr=cbMgr.getPrimaryClip().getItemAt(0).getText().toString();
                        connectionClass = new ConnectionClass();
                        con= connectionClass.CONN();
                        CallableStatement cstmt = con.prepareCall("{ call dbo.SP_GATE_CHKCMD(?,?,?,?,?,?,?,?,?,?,?,?)}");
                        cstmt.setString(1,qr);
                        cstmt.setString(2,DEVICE_ID.replace('G','H'));
                        cstmt.setString(3,SPS_ID);
                        cstmt.setString(4,"");
                        cstmt.setString(5,"");
                        cstmt.registerOutParameter(6, java.sql.Types.VARCHAR);
                        cstmt.registerOutParameter(7, java.sql.Types.VARCHAR);
                        cstmt.registerOutParameter(8, java.sql.Types.VARCHAR);
                        cstmt.registerOutParameter(9, java.sql.Types.VARCHAR);
                        cstmt.registerOutParameter(10, java.sql.Types.VARCHAR);
                        cstmt.registerOutParameter(11, java.sql.Types.VARCHAR);
                        cstmt.registerOutParameter(12, java.sql.Types.VARCHAR);
                        cstmt.execute();
                        String RETURN_MSG = cstmt.getString(6);
                        String TICKET_NO=qr;
                        String TK_NAME = cstmt.getString(7);
                        String TK_CODE = cstmt.getString(9);
                        cstmt.close();

                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                        SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMddHHmmss");
                        Calendar c = Calendar.getInstance();

                        String[] ResultArray=new String[10];
                        FailedLayout.setVisibility(View.VISIBLE);
                        wifiLayout.setVisibility(View.VISIBLE);
                        rfidLayout.setVisibility(View.GONE);
                        if (RETURN_MSG.indexOf("OPEN") > -1) {
                            FailedLayout.setVisibility(View.GONE);
                            setResultText(result = "票券狀態    驗票成功" + "\n\n票券種類    " +TK_NAME+ "\n\n票券入場紀錄\n\n"+df.format(c.getTime()));
                            ResultArray[0]="C";
                            ResultArray[1]=TICKET_NO;
                            ResultArray[2]=SPS_ID;
                            ResultArray[3]="I";
                            ResultArray[4]=DEVICE_ID.replace('G','H');
                            ResultArray[5]=TK_CODE;
                            ResultArray[6]=qr;
                            ResultArray[7]=df2.format(c.getTime());
                            ResultArray[8]="";
                            ResultArray[9]=getDateTime();
                            mydbHelper.InsertToSQLiteUltraLight03(ResultArray, "OK");
                        }else{
                            FailedLayout.setVisibility(View.VISIBLE);
                            setResultText(result = "票券狀態    ");
                            setResultText2(result = RETURN_MSG);
                        }
                    }catch(Exception e) {
                        FailedLayout.setVisibility(View.VISIBLE);
                        setResultText(result = "票券狀態    ");
                        setResultText2(result = "非花博票券條碼！");
                        WriteLog.appendLog("OnlineTickets.java/ticket/Exception:" + ex.toString());
                    }
                }
            }
        };
        cbMgr.addPrimaryClipChangedListener(mPrimaryClipChangedListener);

        //回上頁
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

        //註冊網路狀態監聽
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
        cbMgr.removePrimaryClipChangedListener(mPrimaryClipChangedListener);
        cbMgr.addPrimaryClipChangedListener(mPrimaryClipChangedListener);
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

        SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar c = Calendar.getInstance();
        String[] ResultArray=new String[10];
        try{
            connectionClass = new ConnectionClass();
            con= connectionClass.CONN();
            CallableStatement cstmt = con.prepareCall("{ call dbo.SP_GATE_CHKCMD(?,?,?,?,?,?,?,?,?,?,?,?)}");
            cstmt.setString(1,"");
            cstmt.setString(2,DEVICE_ID.replace('G','H'));
            cstmt.setString(3,SPS_ID);
            cstmt.setString(4,tagNo.toUpperCase());
            cstmt.setString(5,"");
            cstmt.registerOutParameter(6, java.sql.Types.VARCHAR);
            cstmt.registerOutParameter(7, java.sql.Types.VARCHAR);
            cstmt.registerOutParameter(8, java.sql.Types.VARCHAR);
            cstmt.registerOutParameter(9, java.sql.Types.VARCHAR);
            cstmt.registerOutParameter(10, java.sql.Types.VARCHAR);
            cstmt.registerOutParameter(11, java.sql.Types.VARCHAR);
            cstmt.registerOutParameter(12, java.sql.Types.VARCHAR);
            cstmt.execute();
            String RETURN_MSG = cstmt.getString(6);
            String TK_NAME = cstmt.getString(7);
            String TICKET_NO = cstmt.getString(11);
            String FT_NAME=cstmt.getString(12);
            byte[] fileBytes=mydbHelper.GetByte(tagNo.toUpperCase().replace(" ",""));
            cstmt.close();
            if (RETURN_MSG.indexOf("OPEN") > -1) {
                wifiLayout.setVisibility(View.GONE);
                rfidLayout.setVisibility(View.VISIBLE);
                bitmap = BitmapFactory.decodeByteArray(fileBytes, 0, fileBytes.length);
                FtPhotoImage.setImageBitmap(bitmap);
                ResultTxt3.setText("姓　　名    "+FT_NAME+"\n\n票券狀態    驗票成功" + "\n\n票券號碼    " +TICKET_NO+ "\n\n票券種類    " +TK_NAME);
                ResultArray[0]="D";
                ResultArray[1]=tagNo.toUpperCase();
                ResultArray[2]=SPS_ID;
                ResultArray[3]="I";
                ResultArray[4]=DEVICE_ID.replace('G','H');
                ResultArray[5]=TICKET_NO.substring(0,2);
                ResultArray[6]="";
                ResultArray[7]=df2.format(c.getTime());
                ResultArray[8]="";
                ResultArray[9]=getDateTime();
                mydbHelper.InsertToSQLiteUltraLight03(ResultArray, "OK");
            }else{
                wifiLayout.setVisibility(View.VISIBLE);
                rfidLayout.setVisibility(View.GONE);
                FailedLayout.setVisibility(View.VISIBLE);
                setResultText(result = "票券狀態    ");
                setResultText2(result = RETURN_MSG);
            }
        }catch(Exception ex){
            wifiLayout.setVisibility(View.VISIBLE);
            rfidLayout.setVisibility(View.GONE);
            FailedLayout.setVisibility(View.VISIBLE);
            setResultText(result = "票券狀態    ");
            setResultText2(result = "非花博票券條碼！");
            WriteLog.appendLog("OnlineTickets.java/getTagInfo/Exception:" + ex.toString());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cbMgr.removePrimaryClipChangedListener(mPrimaryClipChangedListener);
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(connectionReceiver);
    }

    //QRCODE解碼
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

    //設定票券狀態文字
    private void setResultText(String text) {
        ResultTxt.setText(text);
    }

    //票券狀態文字
    private void setResultText2(String text) {
        ResultTxt2.setText(text);
    }

    //取得現在時間
    public String getDateTime(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Calendar c = Calendar.getInstance();
        String str = df.format(c.getTime());
        return str;
    }

    //震動
    public void setVibrate(int time){
        Vibrator myVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        myVibrator.vibrate(time);
    }

    //監控網路狀態
    private final BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = connectMgr.getActiveNetworkInfo();
            if (mNetworkInfo == null) {
                Toast.makeText(OnlineTickets.this, "連線中斷", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    };
}