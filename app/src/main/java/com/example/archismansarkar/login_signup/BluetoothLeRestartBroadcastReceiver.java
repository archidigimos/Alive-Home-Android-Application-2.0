package com.example.archismansarkar.login_signup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


public class BluetoothLeRestartBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context,"Service Stops!",Toast.LENGTH_SHORT).show();
        context.startService(new Intent(context, BluetoothLeService.class));
    }


}