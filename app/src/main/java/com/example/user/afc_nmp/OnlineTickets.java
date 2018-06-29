package com.example.user.afc_nmp;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    TextView ResultTxt,ResultTxt2,PeopleNumTxt;
    String result="",DEVICE_ID,SPS_ID;
    Button ReturnBtn,HomeBtn;
    LinearLayout FailedLayout;

    //SQL SERVER
    ConnectionClass connectionClass;
    Connection con;

    //SQLITE
    private MyDBHelper mydbHelper;

    //剪貼簿
    private ClipboardManager cbMgr;
    private ClipboardManager.OnPrimaryClipChangedListener mPrimaryClipChangedListener;

    //private NfcAdapter nfcAdapter;
    //private PendingIntent mPendingIntent;
    NfcAdapter mAdapter;
    PendingIntent mPendingIntent;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.online_tickets);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

        //取得上個頁面傳來的值
        Intent intent = getIntent();
        DEVICE_ID=intent.getStringExtra("DEVICE_ID");
        SPS_ID=intent.getStringExtra("SPS_ID");

        ResultTxt=(TextView) findViewById(R.id.ResultTxt);
        PeopleNumTxt=(TextView) findViewById(R.id.PeopleNumTxt);
        ReturnBtn=(Button)findViewById(R.id.ReturnBtn);
        HomeBtn=(Button)findViewById(R.id.HomeBtn);
        ResultTxt2=(TextView) findViewById(R.id.ResultTxt2);
        FailedLayout=(LinearLayout) findViewById(R.id.FailedLayout);

        //SQLITE
        mydbHelper = new MyDBHelper(this);

        //更新設備所在園區
        //mydbHelper.UpdateDeviceSPS_ID(SPS_ID,DEVICE_ID);

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
                    //Toast.makeText(OnlineTickets.this, "OnlineTickets", Toast.LENGTH_SHORT).show();
                    setVibrate(100);
                    String qr=cbMgr.getPrimaryClip().getItemAt(0).getText().toString();
                    String a=qr.substring(0,qr.length()-16);
                    String iv=qr.substring(qr.length() - 16);
                    byte[] descryptBytes=decryptAES(iv.getBytes("UTF-8"),key.getBytes("UTF-8"), Base64.decode(a, Base64.DEFAULT));
                    String getdata = new String(descryptBytes);
                    String TICKET_NO=getdata.split("@")[4];
                    String TK_CODE=getdata.split("@")[5];

                    connectionClass = new ConnectionClass();
                    con= connectionClass.CONN();
                    CallableStatement cstmt = con.prepareCall("{ call dbo.SP_GATE_CHKCMD(?,?,?,?,?,?,?)}");
                    cstmt.setString(1,TICKET_NO);
                    cstmt.setString(2,SPS_ID+DEVICE_ID.replace('G','H'));
                    cstmt.setString(3,SPS_ID);
                    cstmt.setString(4,"");
                    cstmt.registerOutParameter(5, java.sql.Types.VARCHAR);
                    cstmt.registerOutParameter(6, java.sql.Types.VARCHAR);
                    cstmt.registerOutParameter(7, java.sql.Types.VARCHAR);
                    cstmt.execute();
                    String RETURN_MSG = cstmt.getString(5);
                    String TK_NAME = cstmt.getString(6);
                    String RETURN_MSG_DATETIME = cstmt.getString(7);
                    cstmt.close();

                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMddHHmmss");
                    Calendar c = Calendar.getInstance();

                    String[] ResultArray=new String[10];
                    FailedLayout.setVisibility(View.VISIBLE);
                    if (RETURN_MSG.indexOf("OPEN") > -1) {
                        FailedLayout.setVisibility(View.GONE);
                        setResultText(result = "票券狀態    驗票成功" + "\n\n票券號碼    " +TICKET_NO+ "\n\n票券種類    " +TK_NAME+ "\n\n票券入場紀錄\n\n"+df.format(c.getTime()));
                        //ResultTxt.setTextColor(Color.BLUE);
                        ResultArray[0]="A";
                        ResultArray[1]=TICKET_NO;
                        ResultArray[2]=SPS_ID;
                        ResultArray[3]="I";
                        ResultArray[4]=SPS_ID+DEVICE_ID.replace('G','H');
                        ResultArray[5]=TK_CODE;
                        ResultArray[6]=qr;
                        ResultArray[7]=df2.format(c.getTime());
                        ResultArray[8]="";
                        ResultArray[9]=getDateTime();
                        mydbHelper.InsertToSQLiteUltraLight03(ResultArray, "OK");
                    }else if(RETURN_MSG.indexOf("逾時") > -1){
                        FailedLayout.setVisibility(View.VISIBLE);
                        //setResultText(result = "票券狀態：驗票失敗，" +RETURN_MSG+ "\n\n票券號碼：" +TICKET_NO+ "\n\n票券種類：" +TK_NAME);
                        setResultText(result = "票券狀態    ");
                        setResultText2(result = RETURN_MSG);
                        //ResultTxt.setTextColor(Color.RED);
                    }else if(RETURN_MSG.indexOf("已入場") > -1){
                        FailedLayout.setVisibility(View.VISIBLE);
                        //setResultText(result = "票券狀態：驗票失敗，" +RETURN_MSG+ "\n\n票券號碼："+TICKET_NO + "\n\n票券種類："+TK_NAME + "\n\n票券入場紀錄："+RETURN_MSG_DATETIME);
                        setResultText(result = "票券狀態    ");
                        setResultText2(result = RETURN_MSG);
                        //ResultTxt.setTextColor(Color.RED);
                    }else if(RETURN_MSG.indexOf("作廢") > -1){
                        FailedLayout.setVisibility(View.VISIBLE);
                        //setResultText(result = "票券狀態：驗票失敗，" +RETURN_MSG);
                        setResultText(result = "票券狀態    ");
                        setResultText2(result = RETURN_MSG);
                        //ResultTxt.setTextColor(Color.RED);
                    }else if(RETURN_MSG.indexOf("無此售票") > -1){
                        FailedLayout.setVisibility(View.VISIBLE);
                        //setResultText(result = "票券狀態：驗票失敗，" +RETURN_MSG);
                        setResultText(result = "票券狀態    ");
                        setResultText2(result = RETURN_MSG);
                        //ResultTxt.setTextColor(Color.RED);
                    }else if(RETURN_MSG.indexOf("非法票券") > -1){
                        FailedLayout.setVisibility(View.VISIBLE);
                        //setResultText(result = "票券狀態：驗票失敗，非花博票券條碼");
                        setResultText(result = "票券狀態    ");
                        setResultText2(result = RETURN_MSG);
                        //ResultTxt.setTextColor(Color.RED);
                    }
                }catch(Exception ex){
                    FailedLayout.setVisibility(View.VISIBLE);
                    //setResultText(result = "票券狀態：驗票失敗，非花博票券條碼");
                    setResultText(result = "票券狀態    ");
                    setResultText2(result = "非花博票券條碼！");
                    //ResultTxt.setTextColor(Color.RED);
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

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            //nfc not support your device.
            return;
        }
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }//End ON CREATE

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        /*nfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        Intent intent = getIntent();
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Toast.makeText(this,
                    this.ByteArrayToHexString(getIntent().getByteArrayExtra(NfcAdapter.EXTRA_ID)),
                    Toast.LENGTH_SHORT).show();

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if(tag == null){
                setResultText(result = "tag == null");
            }else{
                String tagInfo = tag.toString() + "\n";

                tagInfo += "\nTag Id: \n";
                byte[] tagId = tag.getId();
                tagInfo += "length = " + tagId.length +"\n";
                for(int i=0; i<tagId.length; i++){
                    tagInfo += Integer.toHexString(tagId[i] & 0xFF) + " ";
                }
                tagInfo += "\n";

                String[] techList = tag.getTechList();
                tagInfo += "\nTech List\n";
                tagInfo += "length = " + techList.length +"\n";
                for(int i=0; i<techList.length; i++){
                    tagInfo += techList[i] + "\n";
                }

                setResultText(result = tagInfo);
            }
        }else{
            Toast.makeText(this,
                    "onResume() : " + action,
                    Toast.LENGTH_SHORT).show();
        }*/
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
            tagNo += Integer.toHexString(tagId[i] & 0xFF) + " ";
        }
        setResultText(result = tagNo);
        //Start for Camera
    }

    @Override
    protected void onPause() {
        super.onPause();
        cbMgr.removePrimaryClipChangedListener(mPrimaryClipChangedListener);
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
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

    private String ByteArrayToHexString(byte [] inarray) {
        int i, j, in;
        String [] hex = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
        String out= "";
        for(j = 0 ; j < inarray.length ; ++j)
        {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
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

    //RFID
    private final BroadcastReceiver RfidReceiver = new BroadcastReceiver() {
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