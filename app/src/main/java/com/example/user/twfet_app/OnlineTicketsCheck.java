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
import android.widget.ImageView;
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
 * Created by Jeff.
 */
public class OnlineTicketsCheck extends Activity {
    private static String key = "SET31275691$00000000000000000000";
    private TextView ResultTxt, ResultTxt3, PeopleNumTxt;
    private String result = "", SPS_ID, text = "";
    private Button ReturnBtn, HomeBtn;
    private ImageView FtPhotoImage;
    private LinearLayout wifiLayout, rfidLayout;

    //SQLITE
    private MyDBHelper mydbHelper;

    //SQL SERVER
    private ConnectionClass connectionClass;
    private Connection con;

    //剪貼簿
    private ClipboardManager cbMgr;
    private ClipboardManager.OnPrimaryClipChangedListener mPrimaryClipChangedListener;

    //RFID
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private Bitmap bitmap;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.online_tickets_check);

        //取得上個頁面傳來的值
        Intent intent = getIntent();
        SPS_ID = intent.getStringExtra("SPS_ID");

        ResultTxt = (TextView) findViewById(R.id.ResultTxt);
        ResultTxt3 = (TextView) findViewById(R.id.ResultTxt3);
        PeopleNumTxt = (TextView) findViewById(R.id.PeopleNumTxt);
        ReturnBtn = (Button) findViewById(R.id.ReturnBtn);
        HomeBtn = (Button) findViewById(R.id.HomeBtn);
        wifiLayout = (LinearLayout) findViewById(R.id.wifiLayout);
        rfidLayout = (LinearLayout) findViewById(R.id.rfidLayout);
        FtPhotoImage = (ImageView) findViewById(R.id.FtPhotoImage);

        //SQLITE
        mydbHelper = new MyDBHelper(this);

        //SQL SERVER
        connectionClass = new ConnectionClass();
        con = connectionClass.CONN();

        if (con == null) {
            OnlineTicketsCheck.this.finish();
            Toast.makeText(OnlineTicketsCheck.this, "連線錯誤，請檢查網路狀態", Toast.LENGTH_SHORT).show();
        }

        //查詢館內人數
        PeopleNumTxt.setText("目前園內人數 " + mydbHelper.executePeopleNumStoredProcedure(con, SPS_ID) + " 人");
        PeopleNumTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PeopleNumTxt.setText("目前園內人數 " + mydbHelper.executePeopleNumStoredProcedure(con, SPS_ID) + " 人");
            }
        });

        wifiLayout.setVisibility(View.GONE);
        rfidLayout.setVisibility(View.GONE);

        //掃描事件
        cbMgr = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        mPrimaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
            public void onPrimaryClipChanged() {
                wifiLayout.setVisibility(View.VISIBLE);
                rfidLayout.setVisibility(View.GONE);
                try {
                    setVibrate(100);
                    String qr = cbMgr.getPrimaryClip().getItemAt(0).getText().toString();
                    String a = qr.substring(0, qr.length() - 16);
                    String iv = qr.substring(qr.length() - 16);
                    byte[] descryptBytes = decryptAES(iv.getBytes("UTF-8"), key.getBytes("UTF-8"), Base64.decode(a, Base64.DEFAULT));
                    String getdata = new String(descryptBytes);
                    String ary[] = getdata.split("@");
                    String inTime = "";
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df4 = new SimpleDateFormat("HHmm");
                    if (ary.length >= 7) {
                        inTime = ary[6];
                        if (inTime != null && !inTime.equals("") && Integer.parseInt(inTime.replace(":", "")) > Integer.parseInt(df4.format(c.getTime()))) {
                            text = "票券狀態    未到入場時間！";
                            Spannable spannable = new SpannableString(text);
                            spannable.setSpan(new ForegroundColorSpan(Color.RED), 8, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            ResultTxt.setText(spannable, TextView.BufferType.SPANNABLE);
                            return;
                        }
                    }
                    String TICKET_NO = ary[4];
                    if(TICKET_NO.substring(0,2).equals("OD")  && Integer.parseInt(df4.format(c.getTime())) < 1600){
                        text = "票券狀態    未到入場時間！";
                        Spannable spannable = new SpannableString(text);
                        spannable.setSpan(new ForegroundColorSpan(Color.RED), 8, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ResultTxt.setText(spannable, TextView.BufferType.SPANNABLE);
                        return;
                    }
                    connectionClass = new ConnectionClass();
                    con = connectionClass.CONN();
                    CallableStatement cstmt = con.prepareCall("{ call dbo.SP_TVM_TicketStateQuery(?,?,?,?,?,?,?,?)}");
                    cstmt.setString(1, TICKET_NO);
                    cstmt.setString(2, SPS_ID);
                    cstmt.setString(3, "");
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
                        if ((TICKET_NO.substring(0, 2).equals("OG") || TICKET_NO.substring(0, 2).equals("OK") || TICKET_NO.substring(0, 2).equals("OJ")) && ary.length >= 8) {
                            setResultText(result = "票券狀態    " + RETURN_MSG + "\n\n票券號碼    " + TICKET_NO + "\n\n票券種類    " + TK_NAME + "\n\n人數            " + ary[7]);
                        } else {
                            setResultText(result = "票券狀態    " + RETURN_MSG + "\n\n票券號碼    " + TICKET_NO + "\n\n票券種類    " + TK_NAME);
                        }
                    } else {
                        ResultTxt.setTextColor(Color.BLACK);
                        if (RETURN_MSG.indexOf("已入場") > -1) {
                            text = "票券狀態    " + RETURN_MSG + "\n\n票券號碼    " + TICKET_NO + "\n\n票券種類    " + TK_NAME + "\n\n票券入場紀錄\n\n" + RETURN_MSG_DATETIME;
                        } else {
                            text = "票券狀態    " + RETURN_MSG;
                        }
                        Spannable spannable = new SpannableString(text);
                        spannable.setSpan(new ForegroundColorSpan(Color.RED), 8, 8 + RETURN_MSG.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ResultTxt.setText(spannable, TextView.BufferType.SPANNABLE);
                    }
                } catch (Exception ex) {
                    try {
                        String qr = cbMgr.getPrimaryClip().getItemAt(0).getText().toString();
                        connectionClass = new ConnectionClass();
                        con = connectionClass.CONN();
                        CallableStatement cstmt = con.prepareCall("{ call dbo.SP_TVM_TicketStateQuery(?,?,?,?,?,?,?,?)}");
                        cstmt.setString(1, qr);
                        cstmt.setString(2, SPS_ID);
                        cstmt.setString(3, "");
                        cstmt.registerOutParameter(4, java.sql.Types.VARCHAR);
                        cstmt.registerOutParameter(5, java.sql.Types.VARCHAR);
                        cstmt.registerOutParameter(6, java.sql.Types.VARCHAR);
                        cstmt.registerOutParameter(7, java.sql.Types.VARCHAR);
                        cstmt.registerOutParameter(8, java.sql.Types.VARCHAR);
                        cstmt.execute();
                        String RETURN_MSG = cstmt.getString(4);
                        String RETURN_MSG_DATETIME = cstmt.getString(5);
                        String TK_NAME = cstmt.getString(6);
                        String TICKET_NO = cstmt.getString(7);
                        cstmt.close();
                        if (RETURN_MSG.indexOf("可入") > -1) {
                            setResultText(result = "票券狀態    " + RETURN_MSG + "\n\n票券種類    " + TK_NAME);
                        } else {
                            ResultTxt.setTextColor(Color.BLACK);
                            String text = "";
                            if (RETURN_MSG.indexOf("已入場") > -1) {
                                text = "票券狀態    " + RETURN_MSG + "\n\n票券種類    " + TK_NAME + "\n\n票券入場紀錄\n\n" + RETURN_MSG_DATETIME;
                            } else {
                                text = "票券狀態    " + RETURN_MSG;
                            }
                            Spannable spannable = new SpannableString(text);
                            spannable.setSpan(new ForegroundColorSpan(Color.RED), 8, 8 + RETURN_MSG.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            ResultTxt.setText(spannable, TextView.BufferType.SPANNABLE);
                        }
                    } catch (Exception x) {
                        ResultTxt.setTextColor(Color.BLACK);
                        String text = "票券狀態    非花博票券條碼！";
                        Spannable spannable = new SpannableString(text);
                        spannable.setSpan(new ForegroundColorSpan(Color.RED), 8, 16, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ResultTxt.setText(spannable, TextView.BufferType.SPANNABLE);
                        WriteLog.appendLog("OnlineTicketsCheck.java/ticket/Exception:" + ex.toString());
                    }
                }
            }
        };
        cbMgr.addPrimaryClipChangedListener(mPrimaryClipChangedListener);

        ReturnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnlineTicketsCheck.this.finish();
            }
        });

        HomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnlineTicketsCheck.this.finish();
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
    }//End ON CREATE

    @Override
    protected void onResume() {
        super.onResume();
        //註冊網路狀態監聽
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NetworkChangeReceiver.NETWORK_CHANGE_ACTION);
        registerReceiver(connectionReceiver, intentFilter);
        cbMgr.removePrimaryClipChangedListener(mPrimaryClipChangedListener);
        cbMgr.addPrimaryClipChangedListener(mPrimaryClipChangedListener);
        mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(connectionReceiver);
        cbMgr.removePrimaryClipChangedListener(mPrimaryClipChangedListener);
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        getTagInfo(intent);
    }

    private void getTagInfo(Intent intent) {
        wifiLayout.setVisibility(View.GONE);
        rfidLayout.setVisibility(View.VISIBLE);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String tagNo = "";
        byte[] tagId = tag.getId();
        for (int i = 0; i < tagId.length; i++) {
            if (Integer.toHexString(tagId[i] & 0xFF).length() < 2) {
                tagNo += "0";
            }
            tagNo += Integer.toHexString(tagId[i] & 0xFF);
        }

        try {
            connectionClass = new ConnectionClass();
            con = connectionClass.CONN();
            CallableStatement cstmt = con.prepareCall("{ call dbo.SP_TVM_TicketStateQuery(?,?,?,?,?,?,?,?)}");
            cstmt.setString(1, "");
            cstmt.setString(2, SPS_ID);
            cstmt.setString(3, tagNo.toUpperCase());
            cstmt.registerOutParameter(4, java.sql.Types.VARCHAR);
            cstmt.registerOutParameter(5, java.sql.Types.VARCHAR);
            cstmt.registerOutParameter(6, java.sql.Types.VARCHAR);
            cstmt.registerOutParameter(7, java.sql.Types.VARCHAR);
            cstmt.registerOutParameter(8, java.sql.Types.VARCHAR);
            cstmt.execute();
            String RETURN_MSG = cstmt.getString(4);
            String TK_NAME = cstmt.getString(6);
            String TICKET_NO = cstmt.getString(7);
            String FT_NAME = cstmt.getString(8);
            cstmt.close();
            if (RETURN_MSG.indexOf("可入") > -1) {
                byte[] fileBytes = mydbHelper.GetByte(tagNo.toUpperCase().replace(" ", ""));
                bitmap = BitmapFactory.decodeByteArray(fileBytes, 0, fileBytes.length);
                FtPhotoImage.setImageBitmap(bitmap);
                ResultTxt3.setText("姓　　名    " + FT_NAME + "\n\n票券狀態    " + RETURN_MSG + "\n\n票券號碼    " + TICKET_NO + "\n\n票券種類    " + TK_NAME);
                //setResultText(result = "姓　　名    "+FT_NAME+"\n\n票券狀態    " + RETURN_MSG + "\n\n票券號碼    " +TICKET_NO + "\n\n票券種類    " + TK_NAME);
            } else {
                ResultTxt.setTextColor(Color.BLACK);
                String text = "票券狀態    " + RETURN_MSG;
                Spannable spannable = new SpannableString(text);
                spannable.setSpan(new ForegroundColorSpan(Color.RED), 8, 8 + RETURN_MSG.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ResultTxt.setText(spannable, TextView.BufferType.SPANNABLE);
            }
        } catch (Exception ex) {
            ResultTxt.setTextColor(Color.BLACK);
            String text = "票券狀態    非花博票券條碼！";
            Spannable spannable = new SpannableString(text);
            spannable.setSpan(new ForegroundColorSpan(Color.RED), 8, 16, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ResultTxt.setText(spannable, TextView.BufferType.SPANNABLE);
            WriteLog.appendLog("OnlineTicketsCheck.java/ticket/Exception:" + ex.toString());
        }
    }

    public static byte[] decryptAES(byte[] ivBytes, byte[] keyBytes, byte[] textBytes) {
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
    public void setVibrate(int time) {
        Vibrator myVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        myVibrator.vibrate(time);
    }

    private final BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("status").contains("Not")) {
                Toast.makeText(OnlineTicketsCheck.this, "連線中斷", Toast.LENGTH_SHORT).show();
                OnlineTicketsCheck.this.finish();
            }
        }
    };
}