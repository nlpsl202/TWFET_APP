package com.example.user.afc_nmp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

/**
 * Created by USER on 2015/12/15.
 */
public class ConnectSetting extends Activity {

    Button wifiBtn,bluetoothBtn,offlineBtn,ReturnBtn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.connect_setting);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

        wifiBtn=(Button)findViewById(R.id.WifiBtn);
        bluetoothBtn=(Button)findViewById(R.id.BluetoothBtn);
        //offlineBtn=(Button)findViewById(R.id.OfflineBtn);
        ReturnBtn=(Button)findViewById(R.id.ReturnBtn);

        wifiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callSub = new Intent();
                callSub.setClass(ConnectSetting.this, WiFiConnectSetting.class);
                startActivityForResult(callSub, 0);
            }
        });

        bluetoothBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callSub = new Intent();
                callSub.setClass(ConnectSetting.this, BluetoothConnectSetting.class);
                startActivity(callSub);
            }
        });

        ReturnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
