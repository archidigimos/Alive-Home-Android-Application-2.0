package com.example.archismansarkar.login_signup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RestartBLEServiceReceiver extends BroadcastReceiver
{

    private static final String TAG = "RestartServiceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, BluetoothLeService.class));

    }

}
