package com.example.user.twfet_app;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import com.example.user.afc_nmp.R;

import java.security.spec.AlgorithmParameterSpec;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by USER on 2015/11/19.
 */
public class OfflineTickets extends Activity {
    private static final String key="SET31275691$00000000000000000000";
    TextView ResultTxt,ResultTxt2;
    String result="",DEVICE_ID,SPS_ID,TICKET_NO,TK_CODE;
    Button ReturnBtn,HomeBtn;
    LinearLayout FailedLayout;

    //剪貼簿
    private ClipboardManager cbMgr;
    private ClipboardManager.OnPrimaryClipChangedListener mPrimaryClipChangedListener;

    //SQLITE
    private MyDBHelper mydbHelper;

    //RFID
    NfcAdapter mNfcAdapter;
    PendingIntent mPendingIntent;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.offline_tickets);

        //接收上個頁面傳來的值
        Intent intent = getIntent();
        DEVICE_ID=intent.getStringExtra("DEVICE_ID");
        SPS_ID = intent.getStringExtra("SPS_ID");

        ReturnBtn=(Button)findViewById(R.id.ReturnBtn);
        HomeBtn=(Button)findViewById(R.id.HomeBtn);
        ResultTxt=(TextView) findViewById(R.id.ResultTxt);
        ResultTxt2=(TextView) findViewById(R.id.ResultTxt2);
        FailedLayout=(LinearLayout) findViewById(R.id.FailedLayout);

        //SQLITE
        mydbHelper = new MyDBHelper(this);

        //掃描事件
        this.cbMgr=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        mPrimaryClipChangedListener=new ClipboardManager.OnPrimaryClipChangedListener() {
            public void onPrimaryClipChanged() {
                try {
                    setVibrate(100);
                    String qr = cbMgr.getPrimaryClip().getItemAt(0).getText().toString();
                    String a = qr.substring(0, qr.length() - 16);
                    String iv = qr.substring(qr.length() - 16);
                    byte[] descryptBytes = decryptAES(iv.getBytes("UTF-8"), key.getBytes("UTF-8"), Base64.decode(a, Base64.DEFAULT));
                    String getdata = new String(descryptBytes);
                    String inTime="";
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df4 = new SimpleDateFormat("HHmm");
                    if((getdata.split("@").length>=7)) {
                        inTime=getdata.split("@")[6];
                        if (inTime != null && !inTime.equals("") && Integer.parseInt(inTime.replace(":", "")) > Integer.parseInt(df4.format(c.getTime()))) {
                            FailedLayout.setVisibility(View.VISIBLE);
                            setResultText(result = "票券狀態    ");
                            setResultText2(result = "未到入場時間！");
                            return;
                        }
                    }
                    TICKET_NO = getdata.split("@")[4];
                    TK_CODE = getdata.split("@")[5];
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMddHHmmss");
                    SimpleDateFormat df3 = new SimpleDateFormat("yyyy-MM-dd");
                    String[] ResultArray = new String[11];
                    if (mydbHelper.IsTICKETNOexist(TICKET_NO, SPS_ID, df3.format(c.getTime()))) {
                        FailedLayout.setVisibility(View.VISIBLE);
                        setResultText(result = "票券狀態    ");
                        setResultText2(result = "此票券已入場！");
                    } else {
                        FailedLayout.setVisibility(View.GONE);
                        setResultText(result = "票券狀態    驗票成功" + "\n\n票券號碼    " + TICKET_NO + "\n\n票券種類    " + mydbHelper.GetTKName(TK_CODE) + "\n\n票券入場紀錄\n\n" + df.format(c.getTime()));
                        ResultArray[0] = "A";
                        ResultArray[1] = TICKET_NO;
                        ResultArray[2] = SPS_ID;
                        ResultArray[3] = "I";
                        ResultArray[4] = DEVICE_ID.replace('G','H');
                        ResultArray[5] = TK_CODE;
                        ResultArray[6] = qr;
                        ResultArray[7] = df2.format(c.getTime());
                        ResultArray[8] = "";
                        ResultArray[9] = getDateTime();
                        ResultArray[10] = "";
                        mydbHelper.InsertToSQLiteUltraLight03(ResultArray, "");
                    }
                } catch (Exception ex) {
                    try {
                        String qr = cbMgr.getPrimaryClip().getItemAt(0).getText().toString();
                        SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMddHHmmss");
                        SimpleDateFormat df3 = new SimpleDateFormat("yyyy-MM-dd");
                        Calendar c = Calendar.getInstance();
                        String[] ResultArray = new String[11];
                        if (qr.length()==15 || qr.length()==11 || qr.length()==24 || qr.length()==14 || qr.length()==44) {
                            if (mydbHelper.IsTKQRCODEexist(qr, SPS_ID, df3.format(c.getTime()))) {
                                FailedLayout.setVisibility(View.VISIBLE);
                                setResultText(result = "票券狀態    ");
                                setResultText2(result = "此票券已入場！");
                            }else{
                                FailedLayout.setVisibility(View.GONE);
                                setResultText(result = "票券狀態    驗票成功");
                                ResultArray[0] = "C";
                                ResultArray[1] = "";
                                ResultArray[2] = SPS_ID;
                                ResultArray[3] = "I";
                                ResultArray[4] = DEVICE_ID.replace('G','H');
                                ResultArray[5] = "";
                                ResultArray[6] = qr;
                                ResultArray[7] = df2.format(c.getTime());
                                ResultArray[8] = "";
                                ResultArray[9] = getDateTime();
                                ResultArray[10] = "";
                                mydbHelper.InsertToSQLiteUltraLight03(ResultArray, "");
                            }
                        } else {
                            FailedLayout.setVisibility(View.VISIBLE);
                            setResultText(result = "票券狀態    ");
                            setResultText2(result = "非花博票券條碼！");
                        }
                    }
                    catch (Exception e) {
                        FailedLayout.setVisibility(View.VISIBLE);
                        setResultText(result = "票券狀態    ");
                        setResultText2(result = "非花博票券條碼！");
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

        //RFID
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            //nfc not support your device.
            return;
        }
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }//END ONCREATE

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
            setVibrate(100);
            SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMddHHmmss");
            Calendar c = Calendar.getInstance();
            String[] ResultArray = new String[11];
            if(tagNo.length()==14){
                FailedLayout.setVisibility(View.GONE);
                setResultText(result = "票券狀態    驗票成功\n\n票券種類    全期間票");
                ResultArray[0] = "D";
                ResultArray[1] = "";
                ResultArray[2] = SPS_ID;
                ResultArray[3] = "I";
                ResultArray[4] = DEVICE_ID.replace('G','H');
                ResultArray[5] = "";
                ResultArray[6] = "";
                ResultArray[7] = df2.format(c.getTime());
                ResultArray[8] = "";
                ResultArray[9] = getDateTime();
                ResultArray[10] = tagNo.toUpperCase();
                mydbHelper.InsertToSQLiteUltraLight03(ResultArray, "");
            }else{
                FailedLayout.setVisibility(View.VISIBLE);
                setResultText(result = "票券狀態    ");
                setResultText2(result = "非花博票券條碼！");
                ResultTxt2.setTextColor(Color.RED);
            }
        }catch(Exception ex){
            FailedLayout.setVisibility(View.VISIBLE);
            setResultText(result = "票券狀態    ");
            setResultText2(result = "非花博票券條碼！");
            ResultTxt2.setTextColor(Color.RED);
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
        cbMgr.removePrimaryClipChangedListener(mPrimaryClipChangedListener);
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

    //票券狀態文字
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
}
