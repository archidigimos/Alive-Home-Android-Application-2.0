package com.example.archismansarkar.login_signup;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewSwitcher;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;

import com.chilkatsoft.*;


public class MainActivity extends AppCompatActivity implements
        RecognitionListener , TextToSpeech.OnInitListener{
    private final static String TAG = MainActivity.class
            .getSimpleName();
    public int track = 0;
    public int status_request = 0;
    int count = 0;
    int layer = 0;
    int layer_back_trig = 0;

    private static final int REQUEST_CODE = 0;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;

    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnSkip, btnNext;
    private ToggleButton toggleButton;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;

    Button login1, signup1, login, signup;
    Button alive_next, alive_skip, first_next, first_skip, second_next, second_skip, final_done;
    SeekBar fanSpeed;
    EditText uname_login, password_login, uname_signup, password_signup, repassword_signup, hardwareID_signup;
    //EditText username_log, password_log, username_sign, password_sign, repassword_sign, hardware_sign;
    ImageView imageView, imageView1, alive;
    ImageButton tubelight;
    GPSActivity gps;
    ImageSwitcher fanAlive;

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    // blechat - characteristics for HM-10 serial
    private BluetoothGattCharacteristic characteristicTX;
    private BluetoothGattCharacteristic characteristicRX;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    String[] data_parsed;
    String layout_position;
    private String username_init;
    private String password_init;
    private String user_login_init;
    String TL_state = "TL_OFF";
    String FAN_state = "FAN_OFF";
    boolean tubelight_state = false;
    public boolean touch = true;

    private TextToSpeech tts;

    boolean connecting = false;

    private boolean logsignanimate = false;

    public String transfer_session = "";
    public String publicKey = "<RSAPublicKey><Modulus>pManIJm8ZFVpV4w/hGkr+11gHCfou+AvpbBGMFvcYEyLC78Y2geM88v/J1uxXov6vSpZ0DFKgZzlMYgJf8f8/4HuQukZQtnC6mycqdThPxGQu8+USWcNUCkd0ilx7wlO58L/Hy2QqGxaso4HGvarIwGshfIuJDGUQ4OONavFLSk=</Modulus><Exponent>AQAB</Exponent></RSAPublicKey>";
    private String shared_aes_encryption_key;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
                    .getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up
            // initialization.
           // Toast.makeText(getApplicationContext(), "service connected", Toast.LENGTH_SHORT).show();
            mBluetoothLeService.connect(mDeviceAddress);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //Toast.makeText(getApplicationContext(), "service disconnected", Toast.LENGTH_SHORT).show();
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device. This can be a
    // result of read
    // or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                connecting = false;
                Toast.makeText(getApplicationContext(), "Welcome Home!", Toast.LENGTH_SHORT).show();
                user_layer_interface();
                if (layer == 4)layer = 5;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
                    .equals(action)) {
                if (track==0){start();}
                mConnected = false;
                connecting = false;
                Toast.makeText(getApplicationContext(), "See you soon!", Toast.LENGTH_SHORT).show();
                if (new String("").equals(username_init) && layer ==5){//startup_page();
                    signuplogincombo_page();}
                else {if (layer == 5)layer = 4;}
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                // set serial chaacteristics
                setupSerial();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                final byte[] rxBytes = characteristicRX.getValue();
                String btData = new String(rxBytes);
                peer_data_process(btData);
            }
        }
    };

    private final WebSocketConnection mConnection = new WebSocketConnection();


    public void start() {

        //final String wsuri = "ws://192.168.1.10:80";
        final String wsuri = "ws://10.124.195.9:80";
        //final String wsuri = "ws://192.168.8.100:80";

        try {
            mConnection.connect(wsuri, new WebSocketConnectionHandler() {

                @Override
                public void onOpen() {
                    track = 1;

                    CkRsa rsaEncryptor = new CkRsa();
                    rsaEncryptor.put_EncodingMode("hex");
                    boolean success = rsaEncryptor.ImportPublicKey(publicKey);
                    boolean usePrivateKey = false;

                    shared_aes_encryption_key = shared_key_generator();
                    mConnection.sendTextMessage(rsaEncryptor.encryptStringENC("LOGI-"+username_init+"-"+password_init+"-"+shared_aes_encryption_key,usePrivateKey));
                    mConnection.sendTextMessage(rsaEncryptor.encryptStringENC("ENQ-" + username_init+"-"+shared_aes_encryption_key,usePrivateKey));
                    //mConnection.sendTextMessage("LOGI-"+username_init+"-"+password_init);
                    //mConnection.sendTextMessage("ENQ-" + username_init);

                }

                @Override
                public void onTextMessage(String payload) {
                    String decrypted_data = decryption(payload, shared_aes_encryption_key);
                    data_parsed = decrypted_data.split("-");
                    //data_parsed = payload.split("-");
                    int size = data_parsed.length;
                    //if (mConnected == false) {
                        if (new String("VERIFY").equals(data_parsed[0])) {
                            if (new String("True").equals(data_parsed[1])) {
                                if ((size > 2) && new String("STATUS").equals(data_parsed[2])) {

                                    TL_state = data_parsed[3];
                                    FAN_state = data_parsed[4];

                                    if (new String("TL_ON").equals(data_parsed[3])) {
                                        tubelight_state = true;
                                        tubelight.setImageResource(R.drawable.bulbon);
                                        //speakOut("Light on.");
                                    }//tubelight.performClick();}
                                    else if (new String("TL_OFF").equals(data_parsed[3])) {
                                        tubelight_state = false;
                                        tubelight.setImageResource(R.drawable.bulboff);
                                        //speakOut("Light off.");
                                    }//tubelight.performClick();}

                                    if (new String("FAN_OFF").equals(data_parsed[4])) {
                                        fanAlive.setImageResource(R.drawable.fan);
                                        touch = false;
                                        fanSpeed.setProgress(0);
                                        touch = true;
                                        //speakOut("Fan off.");
                                    } else if (new String("FAN_ON_1").equals(data_parsed[4])) {
                                        fanAlive.setImageResource(R.drawable.fan_one);
                                        touch = false;
                                        fanSpeed.setProgress(1);
                                        touch = true;
                                        //speakOut("Fan set to speed one.");
                                    } else if (new String("FAN_ON_2").equals(data_parsed[4])) {
                                        fanAlive.setImageResource(R.drawable.fan_two);
                                        touch = false;
                                        fanSpeed.setProgress(2);
                                        touch = true;
                                        //speakOut("Fan set to speed two.");
                                    } else if (new String("FAN_ON_3").equals(data_parsed[4])) {
                                        fanAlive.setImageResource(R.drawable.fan_three);
                                        touch = false;
                                        fanSpeed.setProgress(3);
                                        touch = true;
                                        //speakOut("Fan set to speed three.");
                                    } else if (new String("FAN_ON_4").equals(data_parsed[4])) {
                                        fanAlive.setImageResource(R.drawable.fan_four);
                                        touch = false;
                                        fanSpeed.setProgress(4);
                                        touch = true;
                                        //speakOut("Fan set to speed four.");
                                    } else if (new String("FAN_ON_5").equals(data_parsed[4])) {
                                        fanAlive.setImageResource(R.drawable.fan_five);
                                        touch = false;
                                        fanSpeed.setProgress(5);
                                        touch = true;
                                        //speakOut("Fan set to speed five.");
                                    }
                                }

                                if ((size > 2) && new String("BLEMAC").equals(data_parsed[2])) {
                                    SharedPreferences ble_mac_add = getSharedPreferences("BLEMACAdd", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = ble_mac_add.edit();
                                    editor.putString("blemacadd", data_parsed[3]);
                                    editor.commit();
                                    mBluetoothLeService.disconnect();
                                    mBluetoothLeService.connect(data_parsed[3]);
                                }


                                if (count == 0) {
                                    user_layer_interface();
                                    Toast.makeText(getApplicationContext(), "You are connected to your room via the Web!!!", Toast.LENGTH_SHORT).show();
                                    count = 1;
                                    status_request = 0;
                                    if (track==1)
                                    mConnection.sendTextMessage(encryption("sessionRequest-" + username_init,shared_aes_encryption_key));
                                    // mConnection.sendTextMessage("sessionRequest-" + username_init);
                                }
                            } else if (new String("False").equals(data_parsed[1])) {
                                Toast.makeText(getApplicationContext(), "Authentication Error!!!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (new String("NOTIFY").equals(data_parsed[0])) {
                            Toast.makeText(getApplicationContext(), data_parsed[1], Toast.LENGTH_SHORT).show();
                        }
                        if (new String("session").equals(data_parsed[0])) {
                            transfer_session = data_parsed[1];
                            //Toast.makeText(getApplicationContext(), transfer_session, Toast.LENGTH_SHORT).show();
                            if (status_request == 0) {
                                if (track==1){mConnection.sendTextMessage(encryption("STATUS-" + username_init+"-"+transfer_session,shared_aes_encryption_key));}//mConnection.sendTextMessage(encryption("sessionRequest" + username_init));
                                //if (track==1){mConnection.sendTextMessage("STATUS-" + username_init+"-"+transfer_session);}
                                /*if (track == 1) {
                                    mConnection.sendTextMessage("STATUS-" + username_init);
                                }*/
                                status_request = 1;
                            }
                        }

                   // }
                }

                @Override
                public void onClose(int code, String reason) {
                    track = 0;
                }
            });
        } catch (WebSocketException e) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*
        try
        {
            // Initiate DevicePolicyManager.
            mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
            // Set DeviceAdminDemo Receiver for active the component with different option
            mAdminName = new ComponentName(this, DeviceAdminPermission.class);

            if (!mDPM.isAdminActive(mAdminName)) {
                // try to become active
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Click on Activate button to allow your application full automation functionality.");
                startActivityForResult(intent, REQUEST_CODE);
            }
            else
            {
                CkRsa rsa = new CkRsa();

                boolean success = rsa.UnlockComponent("Anything for 30-day trial");
                if (success != true) {
                    Log.i("Chilkat", "RSA component unlock failed");
                    return;
                }

//                speech = SpeechRecognizer.createSpeechRecognizer(this);
//                speech.setRecognitionListener(this);
//                recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//                recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
//                        "en");
//                recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
//                        this.getPackageName());
//                recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                        RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
//                recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
//
//                SharedPreferences logged_data = getSharedPreferences("LayoutData", Context.MODE_PRIVATE);
//                layout_position = logged_data.getString("layout","");
//
//                tts = new TextToSpeech(this, this);
//
//                SharedPreferences prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);
//                username_init = prefs.getString("username","");
//                password_init = prefs.getString("password","");
//
//                if (new String("startup_page").equals(layout_position)){initializeBLE();
//                    signuplogincombo_page();}
//                else welcome_page();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
*/
        CkRsa rsa = new CkRsa();

        boolean success = rsa.UnlockComponent("Anything for 30-day trial");
        if (success != true) {
            Log.i("Chilkat", "RSA component unlock failed");
            return;
        }

        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        SharedPreferences logged_data = getSharedPreferences("LayoutData", Context.MODE_PRIVATE);
        layout_position = logged_data.getString("layout","");

        tts = new TextToSpeech(this, this);

        SharedPreferences prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        username_init = prefs.getString("username","");
        password_init = prefs.getString("password","");

        if (new String("startup_page").equals(layout_position)){initializeBLE();
            signuplogincombo_page();}
        else welcome_page();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            if (!new String("").equals(mDeviceAddress)){
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);}
        }
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        tts = new TextToSpeech(this, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    public void onBackPressed(){
        if((layer == 2) || (layer == 3)){
            signuplogincombo_page();}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ((layer == 1)||(layer == 2)||(layer == 3)){
            getMenuInflater().inflate(R.menu.normal_menu, menu);//Menu Resource, Menu
            return true;}
        else if (layer == 4){
            getMenuInflater().inflate(R.menu.final_menu, menu);
            return true;}
        else if (layer == 5){
            getMenuInflater().inflate(R.menu.bt_menu, menu);
            bleEncryptSend("Status_Query");
            return true;}
        else return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();    //remove all items
        if ((layer == 1)||(layer == 2)||(layer == 3)){
            getMenuInflater().inflate(R.menu.normal_menu, menu);//Menu Resource, Menu
            return true;}
        else if (layer == 4){
            getMenuInflater().inflate(R.menu.final_menu, menu);
            return true;}
        else if (layer == 5){
            getMenuInflater().inflate(R.menu.bt_menu, menu);
            bleEncryptSend("Status_Query");
            return true;}
        else return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.item1:
                    String user_logout = "LOGO-"+username_init+"-"+transfer_session;
                    //String user_logout = "LOGO-"+username_init;
                    if(track==1)mConnection.sendTextMessage(encryption(user_logout,shared_aes_encryption_key));
                    //if(track==1)mConnection.sendTextMessage(user_logout);
                    SharedPreferences prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("username", "");
                    editor.putString("password", "");
                    editor.commit();
                    count = 0;
                    Toast.makeText(getApplicationContext(),"Logged out!!!", Toast.LENGTH_SHORT).show();
                    //startup_page();
                    signuplogincombo_page();
                    return true;
                case R.id.item4:
                    Toast.makeText(getApplicationContext(), "Connecting to Server!!!", Toast.LENGTH_LONG).show();
                    start();
                    return true;
                case R.id.item5:
                    Toast.makeText(getApplicationContext(), "Hi, this is Archisman Sarkar \n I am the developer of this application \n You can reach me out at: archidehex@gmail.com", Toast.LENGTH_LONG).show();
                    return true;
                case R.id.item6:
                    Toast.makeText(getApplicationContext(), "It is an Automation Company \n Originated at IIT Kharagpur!!!", Toast.LENGTH_LONG).show();
                    return true;
                case R.id.item7:
                    Toast.makeText(getApplicationContext(), "Hi, this is Archisman Sarkar \n" +
                            " I am the developer of this application \n" +
                            " You can reach me out at: archidehex@gmail.com", Toast.LENGTH_LONG).show();
                    return true;
                case R.id.item8:
                    Toast.makeText(getApplicationContext(), "It is an Automation Company \n" +
                            " Originated at IIT Kharagpur!!!", Toast.LENGTH_LONG).show();
                    return true;
                case R.id.itembt1:
                    Toast.makeText(getApplicationContext(), "Hi, this is Archisman Sarkar \n" +
                            " I am the developer of this application \n" +
                            " You can reach me out at: archidehex@gmail.com", Toast.LENGTH_LONG).show();
                    return true;
                case R.id.itembt2:
                    Toast.makeText(getApplicationContext(), "It is an Automation Company \n" +
                            " Originated at IIT Kharagpur!!!", Toast.LENGTH_LONG).show();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter
                .addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void setupSerial() {

        // blechat - set serial characteristics
        String uuid;
        String unknownServiceString = "Unknown Service";

        for (BluetoothGattService gattService : mBluetoothLeService
                .getSupportedGattServices()) {
            // HashMap<String, String> currentServiceData = new HashMap<String,
            // String>();
            uuid = gattService.getUuid().toString();

            // If the service exists for HM 10 Serial, say so.
            if (SampleGattAttributes.lookup(uuid, unknownServiceString) == "HM 10 Serial") {

                // get characteristic when UUID matches RX/TX UUID
                characteristicTX = gattService
                        .getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
                characteristicRX = gattService
                        .getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);

                mBluetoothLeService.setCharacteristicNotification(
                        characteristicRX, true);

                break;

            } // if

        } // for


    }

    public void welcome_page(){
        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.activity_welcome);

        SharedPreferences ble_mac_add = getSharedPreferences("BLEMACAdd", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ble_mac_add.edit();
        editor.putString("blemacadd","00:00:00:00:00:00");
        editor.commit();

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        btnSkip = (Button) findViewById(R.id.btn_skip);
        btnNext = (Button) findViewById(R.id.btn_next);


        // layouts of all welcome sliders
        // add few more layouts if you want
        layouts = new int[]{
                R.layout.alivehome,
                R.layout.intro,
                R.layout.intro_second,
                R.layout.intro_last
        };


        // adding bottom dots
        addBottomDots(0);

        // making notification bar transparent
        changeStatusBarColor();

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchHomeScreen();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking for last page
                // if last page home screen will be launched
                int current = getItem(+1);
                if (current < layouts.length) {
                    // move to next screen
                    viewPager.setCurrentItem(current);
                } else {
                    launchHomeScreen();
                }
            }
        });
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {
        shared_layout_cache();initializeBLE();
        signuplogincombo_page();
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
                btnNext.setText(getString(R.string.start));
                btnSkip.setVisibility(View.GONE);
            } else {
                // still pages are left
                btnNext.setText(getString(R.string.next));
                btnSkip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    public void signuplogincombo_page(){
        setContentView(R.layout.signuplogincombo_layout);
        speech.stopListening();
        logsignanimate = false;
        layer_back_trig =0;
        layer = 1;
        start();

        SharedPreferences prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        username_init = prefs.getString("username","");
        password_init = prefs.getString("password","");
        user_login_init = "LOGI-" + username_init + "-" + password_init;

        CkRsa rsaEncryptor = new CkRsa();
        rsaEncryptor.put_EncodingMode("hex");
        boolean success = rsaEncryptor.ImportPublicKey(publicKey);
        boolean usePrivateKey = false;

        shared_aes_encryption_key = shared_key_generator();
        if (track == 1){
            mConnection.sendTextMessage(rsaEncryptor.encryptStringENC("LOGI-"+username_init+"-"+password_init+"-"+shared_aes_encryption_key,usePrivateKey));
            mConnection.sendTextMessage(rsaEncryptor.encryptStringENC("ENQ-" + username_init+"-"+shared_aes_encryption_key,usePrivateKey));
            //mConnection.sendTextMessage("LOGI-"+username_init+"-"+password_init);
            //mConnection.sendTextMessage("ENQ-" + username_init);
        }
        if (mConnected)user_layer_interface();

        final LinearLayout SignupBox = (LinearLayout) findViewById(R.id.SignupBox);
        final LinearLayout LoginBox = (LinearLayout) findViewById(R.id.LoginBox);

        final Button signup_init = (Button) findViewById(R.id.signup_init);
        final Button login_init = (Button) findViewById(R.id.login_init);

        LoginBox.setVisibility(View.GONE);
        SignupBox.setVisibility(View.GONE);
        signup_init.setVisibility(View.VISIBLE);
        login_init.setVisibility(View.VISIBLE);









        signup_init.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (logsignanimate == false) {
                    if (track == 0) {
                        Toast.makeText(getApplicationContext(), "Not connected to the Server!!!", Toast.LENGTH_SHORT).show();
                        start();
                        Toast.makeText(getApplicationContext(), "Trying to connect to the server!!!", Toast.LENGTH_SHORT).show();
                    } else if (track == 1) {

                        layer = 3;
                        Toast.makeText(getApplicationContext(), "Welcome to Alive Home automation!!!", Toast.LENGTH_SHORT).show();

                        TextInputLayout username_signupLayout = (TextInputLayout) findViewById(R.id.username_signupLayout);
                        username_signupLayout.setHint("Username");
                        username_signupLayout.setErrorEnabled(true);
                        username_signupLayout.setError("Min 6 characters required");



                        TextInputLayout password_signupLayout = (TextInputLayout) findViewById(R.id.password_signupLayout);
                        password_signupLayout.setHint("Password");
                        password_signupLayout.setErrorEnabled(true);
                        password_signupLayout.setError("Min 6 characters required");



                        TextInputLayout reenterpassword = (TextInputLayout) findViewById(R.id.reenterpassword);
                        reenterpassword.setHint("Re Enter Password");
                        reenterpassword.setErrorEnabled(true);



                        TextInputLayout hardware_id = (TextInputLayout) findViewById(R.id.hardware_id);
                        hardware_id.setHint("Hardware ID");
                        hardware_id.setErrorEnabled(true);
                        hardware_id.setError("Enter valid hardware ID");


                        Animation animTranslate = AnimationUtils.loadAnimation(MainActivity.this, R.anim.translate);
                        animTranslate.setAnimationListener(new Animation.AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation arg0) {
                            }

                            @Override
                            public void onAnimationRepeat(Animation arg0) {
                            }

                            @Override
                            public void onAnimationEnd(Animation arg0) {

                                SignupBox.setVisibility(View.VISIBLE);
                                Animation animFade = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade);
                                SignupBox.startAnimation(animFade);

                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) login_init.getLayoutParams();
                                params.addRule(RelativeLayout.BELOW, R.id.SignupBox);
                                params.topMargin = 10;
                                login_init.setLayoutParams(params);
                                login_init.setVisibility(View.VISIBLE);
                                Animation login_fade = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade);
                                login_init.startAnimation(login_fade);

                                logsignanimate = true;

                                Button signup_new = (Button) findViewById(R.id.signup_new);
                                signup_new.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        EditText username_sign = (EditText) findViewById(R.id.username_signup_);
                                        username_sign.setError("Required");

                                        EditText password_sign = (EditText) findViewById(R.id.password_signup_);
                                        password_sign.setError("Required");

                                        EditText repassword_sign = (EditText) findViewById(R.id.repassword);
                                        repassword_sign.setError("Required");

                                        EditText hardware_sign = (EditText) findViewById(R.id.hardware);
                                        hardware_sign.setError("Required");

                                        if ((!new String("").equals(username_sign.getText().toString()))&&(!new String("").equals(hardware_sign.getText().toString()))) {
                                            username_init = username_sign.getText().toString();
                                            SharedPreferences prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString("username", username_sign.getText().toString());
                                            editor.putString("password", password_sign.getText().toString());
                                            editor.commit();

                                            CkRsa rsaEncryptor = new CkRsa();
                                            rsaEncryptor.put_EncodingMode("hex");
                                            boolean success = rsaEncryptor.ImportPublicKey(publicKey);
                                            boolean usePrivateKey = false;
                                            shared_aes_encryption_key = shared_key_generator();

                                            String user_signup = "NUS-" + username_sign.getText().toString() + "-" + password_sign.getText().toString() + "-" + repassword_sign.getText().toString() + "-" + hardware_sign.getText().toString()+"-"+shared_aes_encryption_key;
                                            if (track == 1) mConnection.sendTextMessage(rsaEncryptor.encryptStringENC(user_signup,usePrivateKey));
                                            //callGPS();
                                            if (track == 1) mConnection.sendTextMessage(rsaEncryptor.encryptStringENC("ENQ-" + username_init+"-"+shared_aes_encryption_key,usePrivateKey));
                                            //if (track == 1) mConnection.sendTextMessage(user_signup);
                                            //callGPS();
                                            //if (track == 1) mConnection.sendTextMessage("ENQ-" + username_init);
                                        }
                                        else if ((new String("").equals(username_sign.getText().toString()))||(new String("").equals(hardware_sign.getText().toString()))){
                                            Toast.makeText(getApplicationContext(), "Username and HardwareID cannot be left unattended!!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });

                        signup_init.setVisibility(View.GONE);
                        Animation signup_fadeout = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fadeout);
                        signup_init.startAnimation(signup_fadeout);

                        login_init.setVisibility(View.GONE);
                        Animation login_fadeout = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fadeout);
                        login_init.startAnimation(login_fadeout);

                        LoginBox.setVisibility(View.GONE);
                        Animation animFadeout = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fadeout);
                        LoginBox.startAnimation(animFadeout);

                        ImageView imgLogo = (ImageView) findViewById(R.id.alivelogo);
                        imgLogo.startAnimation(animTranslate);
                    }
                }
                else if (logsignanimate == true) {

                    signup_init.setVisibility(View.GONE);
                    Animation signup_fadeout = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fadeout);
                    signup_init.startAnimation(signup_fadeout);

                    login_init.setVisibility(View.GONE);
                    Animation login_fadeout = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fadeout);
                    login_init.startAnimation(login_fadeout);

                    LoginBox.setVisibility(View.GONE);
                    LoginBox.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.flip_in_left));


                    Animation animFade = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade);
                    SignupBox.startAnimation(animFade);
                    SignupBox.setVisibility(View.VISIBLE);

                    Animation login_fade = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade);
                    login_init.startAnimation(login_fade);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) login_init.getLayoutParams();
                    params.addRule(RelativeLayout.BELOW, R.id.SignupBox);
                    params.topMargin=10;
                    login_init.setLayoutParams(params);
                    login_init.setVisibility(View.VISIBLE);

                    TextInputLayout username_signupLayout = (TextInputLayout) findViewById(R.id.username_signupLayout);
                    username_signupLayout.setHint("Username");
                    username_signupLayout.setErrorEnabled(true);
                    username_signupLayout.setError("Min 6 characters required");


                    TextInputLayout password_signupLayout = (TextInputLayout) findViewById(R.id.password_signupLayout);
                    password_signupLayout.setHint("Password");
                    password_signupLayout.setErrorEnabled(true);
                    password_signupLayout.setError("Min 6 characters required");


                    TextInputLayout reenterpassword = (TextInputLayout) findViewById(R.id.reenterpassword);
                    reenterpassword.setHint("Re Enter Password");
                    reenterpassword.setErrorEnabled(true);


                    TextInputLayout hardware_id = (TextInputLayout) findViewById(R.id.hardware_id);
                    hardware_id.setHint("Hardware ID");
                    hardware_id.setErrorEnabled(true);
                    hardware_id.setError("Enter valid hardware ID");

                    Button signup_new = (Button) findViewById(R.id.signup_new);
                    signup_new.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            EditText username_sign = (EditText) findViewById(R.id.username_signup_);
                            username_sign.setError("Required");

                            EditText password_sign = (EditText) findViewById(R.id.password_signup_);
                            password_sign.setError("Required");

                            EditText repassword_sign = (EditText) findViewById(R.id.repassword);
                            repassword_sign.setError("Required");

                            EditText hardware_sign = (EditText) findViewById(R.id.hardware);
                            hardware_sign.setError("Required");

                            if ((!new String("").equals(username_sign.getText().toString()))&&(!new String("").equals(hardware_sign.getText().toString()))) {
                                username_init = username_sign.getText().toString();
                                SharedPreferences prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("username", username_sign.getText().toString());
                                editor.putString("password", password_sign.getText().toString());
                                editor.commit();

                                CkRsa rsaEncryptor = new CkRsa();
                                rsaEncryptor.put_EncodingMode("hex");
                                boolean success = rsaEncryptor.ImportPublicKey(publicKey);
                                boolean usePrivateKey = false;

                                shared_aes_encryption_key = shared_key_generator();
                                String user_signup = "NUS-" + username_sign.getText().toString() + "-" + password_sign.getText().toString() + "-" + repassword_sign.getText().toString() + "-" + hardware_sign.getText().toString()+"-"+shared_aes_encryption_key;
                                if (track == 1) mConnection.sendTextMessage(rsaEncryptor.encryptStringENC(user_signup,usePrivateKey));
                                //callGPS();
                                if (track == 1) mConnection.sendTextMessage(rsaEncryptor.encryptStringENC("ENQ-" + username_init+"-"+shared_aes_encryption_key,usePrivateKey));
                                //if (track == 1) mConnection.sendTextMessage(user_signup);
                                //callGPS();
                                //if (track == 1) mConnection.sendTextMessage("ENQ-" + username_init);
                            }
                            else if ((new String("").equals(username_sign.getText().toString()))||(new String("").equals(hardware_sign.getText().toString()))){
                                Toast.makeText(getApplicationContext(), "Username and HardwareID cannot be left unattended!!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        login_init.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (logsignanimate == false) {

                    if(track == 0){
                        Toast.makeText(getApplicationContext(), "Not connected to the Server!!!", Toast.LENGTH_SHORT).show();
                        start();
                        Toast.makeText(getApplicationContext(), "Trying to connect to the server!!!", Toast.LENGTH_SHORT).show();
                    }
                    else if(track ==1) {
                        layer = 2;
                        Toast.makeText(getApplicationContext(), "Good to see you back!!!", Toast.LENGTH_SHORT).show();

                        TextInputLayout username_loginLayout = (TextInputLayout) findViewById(R.id.username_loginLayout);
                        username_loginLayout.setHint("Username");
                        username_loginLayout.setErrorEnabled(true);
                        username_loginLayout.setError("Enter valid username");



                        TextInputLayout password_loginLayout = (TextInputLayout) findViewById(R.id.password_loginLayout);
                        password_loginLayout.setHint("Password");
                        password_loginLayout.setErrorEnabled(true);


                        Animation animTranslate = AnimationUtils.loadAnimation(MainActivity.this, R.anim.translate);
                        animTranslate.setAnimationListener(new Animation.AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation arg0) {
                            }

                            @Override
                            public void onAnimationRepeat(Animation arg0) {
                            }

                            @Override
                            public void onAnimationEnd(Animation arg0) {

                                LoginBox.setVisibility(View.VISIBLE);
                                Animation animFade = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade);
                                LoginBox.startAnimation(animFade);

                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) signup_init.getLayoutParams();
                                params.addRule(RelativeLayout.BELOW, R.id.LoginBox);
                                params.topMargin = 10;
                                signup_init.setLayoutParams(params);
                                signup_init.setVisibility(View.VISIBLE);
                                Animation signup_fade = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade);
                                signup_init.startAnimation(signup_fade);

                                logsignanimate = true;

                                Button login_new = (Button) findViewById(R.id.login_new);
                                login_new.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        EditText username_log = (EditText) findViewById(R.id.username_login_);
                                        username_log.setError("Required");

                                        EditText password_log = (EditText) findViewById(R.id.password_login_);
                                        password_log.setError("Required");

                                        if (!new String("").equals(username_log.getText().toString())) {
                                            username_init = username_log.getText().toString();
                                            SharedPreferences prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString("username", username_log.getText().toString());
                                            editor.putString("password", password_log.getText().toString());
                                            editor.commit();

                                            CkRsa rsaEncryptor = new CkRsa();
                                            rsaEncryptor.put_EncodingMode("hex");
                                            boolean success = rsaEncryptor.ImportPublicKey(publicKey);
                                            boolean usePrivateKey = false;

                                            shared_aes_encryption_key = shared_key_generator();
                                            String user_login = "LOGI-" + username_log.getText().toString() + "-" + password_log.getText().toString()+"-"+shared_aes_encryption_key;
                                            if (track == 1) mConnection.sendTextMessage(rsaEncryptor.encryptStringENC(user_login,usePrivateKey));
                                            //callGPS();
                                            if (track == 1) mConnection.sendTextMessage(rsaEncryptor.encryptStringENC("ENQ-" + username_init+"-"+shared_aes_encryption_key,usePrivateKey));
                                            //if (track == 1) mConnection.sendTextMessage(user_login);
                                            //callGPS();
                                            //if (track == 1) mConnection.sendTextMessage("ENQ-" + username_init);
                                        }
                                        else if (new String("").equals(username_log.getText().toString())){
                                            Toast.makeText(getApplicationContext(), "Please enter an username!!!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });

                        signup_init.setVisibility(View.GONE);
                        Animation signup_fadeout = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fadeout);
                        signup_init.startAnimation(signup_fadeout);

                        login_init.setVisibility(View.GONE);
                        Animation login_fadeout = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fadeout);
                        login_init.startAnimation(login_fadeout);

                        SignupBox.setVisibility(View.GONE);
                        Animation animFadeout = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fadeout);
                        SignupBox.startAnimation(animFadeout);

                        ImageView imgLogo = (ImageView) findViewById(R.id.alivelogo);
                        imgLogo.startAnimation(animTranslate);
                    }
                }
                else if (logsignanimate == true) {

                    login_init.setVisibility(View.GONE);
                    Animation login_fadeout = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fadeout);
                    login_init.startAnimation(login_fadeout);

                    signup_init.setVisibility(View.GONE);
                    Animation signup_fadeout = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fadeout);
                    signup_init.startAnimation(signup_fadeout);

                    SignupBox.setVisibility(View.GONE);
                    SignupBox.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.flip_in_left));

                    Animation animFade = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade);
                    LoginBox.startAnimation(animFade);
                    LoginBox.setVisibility(View.VISIBLE);

                    Animation signup_fade = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade);
                    signup_init.startAnimation(signup_fade);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) signup_init.getLayoutParams();
                    params.addRule(RelativeLayout.BELOW, R.id.LoginBox);
                    params.topMargin=10;
                    signup_init.setLayoutParams(params);
                    signup_init.setVisibility(View.VISIBLE);

                    TextInputLayout username_loginLayout = (TextInputLayout) findViewById(R.id.username_loginLayout);
                    username_loginLayout.setHint("Username");
                    username_loginLayout.setErrorEnabled(true);
                    username_loginLayout.setError("Enter valid username");



                    TextInputLayout password_loginLayout = (TextInputLayout) findViewById(R.id.password_loginLayout);
                    password_loginLayout.setHint("Password");
                    password_loginLayout.setErrorEnabled(true);

                    Button login_new = (Button) findViewById(R.id.login_new);
                    login_new.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            EditText username_log = (EditText) findViewById(R.id.username_login_);
                            username_log.setError("Required");

                            EditText password_log = (EditText) findViewById(R.id.password_login_);
                            password_log.setError("Required");

                            if (!new String("").equals(username_log.getText().toString())) {
                                username_init = username_log.getText().toString();
                                SharedPreferences prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("username", username_log.getText().toString());
                                editor.putString("password", password_log.getText().toString());
                                editor.commit();

                                CkRsa rsaEncryptor = new CkRsa();
                                rsaEncryptor.put_EncodingMode("hex");
                                boolean success = rsaEncryptor.ImportPublicKey(publicKey);
                                boolean usePrivateKey = false;

                                shared_aes_encryption_key = shared_key_generator();
                                String user_login = "LOGI-" + username_log.getText().toString() + "-" + password_log.getText().toString()+"-"+shared_aes_encryption_key;
                                if (track == 1) mConnection.sendTextMessage(rsaEncryptor.encryptStringENC(user_login,usePrivateKey));
                                //callGPS();
                                if (track == 1) mConnection.sendTextMessage(rsaEncryptor.encryptStringENC("ENQ-" + username_init+"-"+shared_aes_encryption_key,usePrivateKey));

                                //if (track == 1) mConnection.sendTextMessage(user_login);
                                //callGPS();
                                //if (track == 1) mConnection.sendTextMessage("ENQ-" + username_init);
                            }
                            else if (new String("").equals(username_log.getText().toString())){
                                Toast.makeText(getApplicationContext(), "Please enter an username!!!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });
    }

    public void shared_layout_cache(){
        SharedPreferences logged_data = getSharedPreferences("LayoutData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = logged_data.edit();
        editor.putString("layout", "startup_page");
        editor.commit();
    }


    public void user_layer_interface(){

        setContentView(R.layout.user_layout);

        SharedPreferences settings = getSharedPreferences("mysettings",
                Context.MODE_PRIVATE);
        String ble_interrupt = settings.getString("btconnectionstate", "");
        if(new String("connected").equals(ble_interrupt))mConnected = true;
        else if (new String("disconnected").equals(ble_interrupt))mConnected = false;


        //Toast.makeText(getApplicationContext(), ble_interrupt+"\n"+mConnected, Toast.LENGTH_SHORT).show();

        layer_back_trig = 0;
        if (mConnected == false)layer = 4;
        else if (mConnected == true)layer = 5;
        if (mConnected==false)callGPS();

        alive = (ImageView) findViewById(R.id.alive);
        alive.setImageResource(R.mipmap.ic_launcher);

        fanAlive = (ImageSwitcher) findViewById(R.id.fanAlive);
        fanAlive.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setLayoutParams(new ImageSwitcher.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
                imageView.setImageResource(R.drawable.fan);
                return imageView;
            }
        });

        tubelight = (ImageButton) findViewById(R.id.tubelight);
        tubelight.setImageResource(R.drawable.bulboff);

        tubelight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (mConnected==false)callGPS();
                if(tubelight_state == true) {
                    TL_state = "TL_OFF";
                    tubelight.setImageResource(R.drawable.bulboff);
                    if(track==1 && mConnected == false){mConnection.sendTextMessage(encryption("CTRL-"+username_init+"-"+TL_state+"-"+FAN_state+"-"+transfer_session,shared_aes_encryption_key));}
                    //if(track==1 && mConnected == false){mConnection.sendTextMessage("CTRL-"+username_init+"-"+TL_state+"-"+FAN_state+"-"+transfer_session);}
                    //if(track==1 && mConnected == false){mConnection.sendTextMessage("CTRL-"+username_init+"-"+TL_state+"-"+FAN_state);}
                    bleEncryptSend(TL_state+"-"+FAN_state);
                    tubelight_state = false;
                }
                else if (tubelight_state == false){
                    TL_state = "TL_ON";
                    tubelight.setImageResource(R.drawable.bulbon);
                    if(track==1 && mConnected == false){mConnection.sendTextMessage(encryption("CTRL-"+username_init+"-"+TL_state+"-"+FAN_state+"-"+transfer_session,shared_aes_encryption_key));}
                    //if(track==1 && mConnected == false){mConnection.sendTextMessage("CTRL-"+username_init+"-"+TL_state+"-"+FAN_state+"-"+transfer_session);}
                    //if(track==1 && mConnected == false){mConnection.sendTextMessage("CTRL-"+username_init+"-"+TL_state+"-"+FAN_state);}
                    bleEncryptSend(TL_state+"-"+FAN_state);
                    tubelight_state = true;
                }
                if (track==1) {//mConnection.sendTextMessage(encryption("STATUS-" + username_init+"-"+transfer_session));//mConnection.sendTextMessage(encryption("sessionRequest" + username_init));}
                mConnection.sendTextMessage(encryption("sessionRequest-" + username_init,shared_aes_encryption_key));}
                //if (track==1) {mConnection.sendTextMessage("STATUS-" + username_init+"-"+transfer_session);mConnection.sendTextMessage(encryption("sessionRequest" + username_init));}
                    //mConnection.sendTextMessage("sessionRequest-" + username_init);}
            }
        });

        fanSpeed = (SeekBar) findViewById(R.id.fanSpeed);

        fanSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                //if (mConnected==false)callGPS();
                progressChanged = progress;
                if (touch == true) {
                    if (progress == 0) {
                        fanAlive.setImageResource(R.drawable.fan);
                        FAN_state = "FAN_OFF";
                        if (track == 1 && mConnected == false) {
                            mConnection.sendTextMessage(encryption("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state + "-" + transfer_session,shared_aes_encryption_key));
                            //mConnection.sendTextMessage("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state + "-" + transfer_session);
                            //mConnection.sendTextMessage("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state);
                        }
                        bleEncryptSend(TL_state + "-" + FAN_state);
                    } else if (progressChanged == 1) {
                        fanAlive.setImageResource(R.drawable.fan_one);
                        FAN_state = "FAN_ON_1";
                        if (track == 1 && mConnected == false) {
                            mConnection.sendTextMessage(encryption("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state + "-" + transfer_session,shared_aes_encryption_key));
                            //mConnection.sendTextMessage("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state + "-" + transfer_session);
                            //mConnection.sendTextMessage("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state);
                        }
                        bleEncryptSend(TL_state + "-" + FAN_state);
                    } else if (progressChanged == 2) {
                        fanAlive.setImageResource(R.drawable.fan_two);
                        FAN_state = "FAN_ON_2";
                        if (track == 1 && mConnected == false) {
                            mConnection.sendTextMessage(encryption("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state + "-" + transfer_session,shared_aes_encryption_key));
                            //mConnection.sendTextMessage("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state + "-" + transfer_session);
                            //mConnection.sendTextMessage("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state);
                        }
                        bleEncryptSend(TL_state + "-" + FAN_state);
                    } else if (progressChanged == 3) {
                        fanAlive.setImageResource(R.drawable.fan_three);
                        FAN_state = "FAN_ON_3";
                        if (track == 1 && mConnected == false) {
                            mConnection.sendTextMessage(encryption("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state + "-" + transfer_session,shared_aes_encryption_key));
                            //mConnection.sendTextMessage("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state + "-" + transfer_session);
                            //mConnection.sendTextMessage("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state);
                        }
                        bleEncryptSend(TL_state + "-" + FAN_state);
                    } else if (progressChanged == 4) {
                        fanAlive.setImageResource(R.drawable.fan_four);
                        FAN_state = "FAN_ON_4";
                        if (track == 1 && mConnected == false) {
                            mConnection.sendTextMessage(encryption("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state + "-" + transfer_session,shared_aes_encryption_key));
                            //mConnection.sendTextMessage("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state + "-" + transfer_session);
                            //mConnection.sendTextMessage("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state);
                        }
                        bleEncryptSend(TL_state + "-" + FAN_state);
                    } else if (progressChanged == 5) {
                        fanAlive.setImageResource(R.drawable.fan_five);
                        FAN_state = "FAN_ON_5";
                        if (track == 1 && mConnected == false) {
                            mConnection.sendTextMessage(encryption("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state + "-" + transfer_session,shared_aes_encryption_key));
                            //mConnection.sendTextMessage("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state + "-" + transfer_session);
                            //mConnection.sendTextMessage("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state);
                        }
                        bleEncryptSend(TL_state + "-" + FAN_state);
                    }

                    if (track == 1) {
                        //mConnection.sendTextMessage(encryption("STATUS-" + username_init + "-" + transfer_session));//mConnection.sendTextMessage(encryption("sessionRequest" + username_init));}
                        mConnection.sendTextMessage(encryption("sessionRequest-" + username_init,shared_aes_encryption_key));
                        //mConnection.sendTextMessage("STATUS-" + username_init + "-" + transfer_session);//mConnection.sendTextMessage(encryption("sessionRequest" + username_init));}
                        //mConnection.sendTextMessage("sessionRequest-" + username_init);
                    }
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        toggleButton = (ToggleButton) findViewById(R.id.toggle);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    speech.startListening(recognizerIntent);
                } else {
                    speech.stopListening();
                }
            }
        });

    }
/*
    public void peer_data_process(String payload) {
        data_parsed = payload.split("-");
        int size = data_parsed.length;

        if (size >= 2) {
            TL_state = data_parsed[0];
            FAN_state = data_parsed[1];

            if (new String("TL_ON").equals(data_parsed[0])) {
                tubelight_state = false;
                tubelight.performClick();
            } else if (new String("TL_OFF").equals(data_parsed[0])) {
                tubelight_state = true;
                tubelight.performClick();
            }

            if (new String("FAN_OFF").equals(data_parsed[1])) {
                fanAlive.setImageResource(R.drawable.fan);
                fanSpeed.setProgress(0);
            } else if (new String("FAN_ON_1").equals(data_parsed[1])) {
                fanAlive.setImageResource(R.drawable.fan_one);
                fanSpeed.setProgress(1);
            } else if (new String("FAN_ON_2").equals(data_parsed[1])) {
                fanAlive.setImageResource(R.drawable.fan_two);
                fanSpeed.setProgress(2);
            } else if (new String("FAN_ON_3").equals(data_parsed[1])) {
                fanAlive.setImageResource(R.drawable.fan_three);
                fanSpeed.setProgress(3);
            } else if (new String("FAN_ON_4").equals(data_parsed[1])) {
                fanAlive.setImageResource(R.drawable.fan_four);
                fanSpeed.setProgress(4);
            } else if (new String("FAN_ON_5").equals(data_parsed[1])) {
                fanAlive.setImageResource(R.drawable.fan_five);
                fanSpeed.setProgress(5);
            }
        }
    }
*/

    public void peer_data_process(String payload) {
        /*
        data_parsed = payload.split("-");
        int size = data_parsed.length;

        if (size >= 2) {
            TL_state = data_parsed[0];
            FAN_state = data_parsed[1];

            if (new String("TL_ON").equals(data_parsed[0])) {
                tubelight_state = false;
                tubelight.performClick();
            } else if (new String("TL_OFF").equals(data_parsed[0])) {
                tubelight_state = true;
                tubelight.performClick();
            }

            if (new String("FAN_OFF").equals(data_parsed[1])) {
                fanAlive.setImageResource(R.drawable.fan);
                fanSpeed.setProgress(0);
            } else if (new String("FAN_ON_1").equals(data_parsed[1])) {
                fanAlive.setImageResource(R.drawable.fan_one);
                fanSpeed.setProgress(1);
            } else if (new String("FAN_ON_2").equals(data_parsed[1])) {
                fanAlive.setImageResource(R.drawable.fan_two);
                fanSpeed.setProgress(2);
            } else if (new String("FAN_ON_3").equals(data_parsed[1])) {
                fanAlive.setImageResource(R.drawable.fan_three);
                fanSpeed.setProgress(3);
            } else if (new String("FAN_ON_4").equals(data_parsed[1])) {
                fanAlive.setImageResource(R.drawable.fan_four);
                fanSpeed.setProgress(4);
            } else if (new String("FAN_ON_5").equals(data_parsed[1])) {
                fanAlive.setImageResource(R.drawable.fan_five);
                fanSpeed.setProgress(5);
            }
        }
*/

        if(new String("N").equals(payload)){tubelight_state = false;tubelight.setImageResource(R.drawable.bulboff);fanAlive.setImageResource(R.drawable.fan);touch=false;fanSpeed.setProgress(0);touch = true;/*speakOut("Light off. Fan off")*/;}
        else if(new String("O").equals(payload)){tubelight_state = false;tubelight.setImageResource(R.drawable.bulboff);fanAlive.setImageResource(R.drawable.fan_one);touch=false;fanSpeed.setProgress(1);touch = true;/*speakOut("Light off. Fan set to speed one")*/;}
        else if(new String("P").equals(payload)){tubelight_state = false;tubelight.setImageResource(R.drawable.bulboff);fanAlive.setImageResource(R.drawable.fan_two);touch=false;fanSpeed.setProgress(2);touch = true;/*speakOut("Light off. Fan set to speed two")*/;}
        else if(new String("Q").equals(payload)){tubelight_state = false;tubelight.setImageResource(R.drawable.bulboff);fanAlive.setImageResource(R.drawable.fan_three);touch=false;fanSpeed.setProgress(3);touch = true;/*speakOut("Light off. Fan set to speed three")*/;}
        else if(new String("R").equals(payload)){tubelight_state = false;tubelight.setImageResource(R.drawable.bulboff);fanAlive.setImageResource(R.drawable.fan_four);touch=false;fanSpeed.setProgress(4);touch = true;/*speakOut("Light off. Fan set to speed four")*/;}
        else if(new String("S").equals(payload)){tubelight_state = false;tubelight.setImageResource(R.drawable.bulboff);fanAlive.setImageResource(R.drawable.fan_five);touch=false;fanSpeed.setProgress(5);touch = true;/*speakOut("Light off. Fan set to speed five")*/;}

        else if(new String("T").equals(payload)){tubelight_state = true;tubelight.setImageResource(R.drawable.bulbon);fanAlive.setImageResource(R.drawable.fan);touch=false;fanSpeed.setProgress(0);touch = true;/*speakOut("Light on. Fan off");*/}
        else if(new String("U").equals(payload)){tubelight_state = true;tubelight.setImageResource(R.drawable.bulbon);fanAlive.setImageResource(R.drawable.fan_one);touch=false;fanSpeed.setProgress(1);touch = true;/*speakOut("Light on. Fan set to speed one");*/}
        else if(new String("V").equals(payload)){tubelight_state = true;tubelight.setImageResource(R.drawable.bulbon);fanAlive.setImageResource(R.drawable.fan_two);touch=false;fanSpeed.setProgress(2);touch = true;/*speakOut("Light on. Fan set to speed two");*/}
        else if(new String("W").equals(payload)){tubelight_state = true;tubelight.setImageResource(R.drawable.bulbon);fanAlive.setImageResource(R.drawable.fan_three);touch=false;fanSpeed.setProgress(3);touch = true;/*speakOut("Light on. Fan set to speed three");*/}
        else if(new String("X").equals(payload)){tubelight_state = true;tubelight.setImageResource(R.drawable.bulbon);fanAlive.setImageResource(R.drawable.fan_four);touch=false;fanSpeed.setProgress(4);touch = true;/*speakOut("Light on. Fan set to speed four");*/}
        else if(new String("Y").equals(payload)){tubelight_state = true;tubelight.setImageResource(R.drawable.bulbon);fanAlive.setImageResource(R.drawable.fan_five);touch=false;fanSpeed.setProgress(5);touch = true;/*speakOut("Light on. Fan set to speed five");*/}
    }

    private void callGPS(){
        // create class object
        gps = new GPSActivity(MainActivity.this);

        // check if GPS enabled
        if(gps.canGetLocation()){

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            String latitudeS = String.valueOf(latitude);
            String longitudeS = String.valueOf(longitude);
            if(track==1){mConnection.sendTextMessage(encryption("LOCATION-"+username_init+"-"+latitudeS+"-"+longitudeS+"-"+transfer_session,shared_aes_encryption_key));/*mConnection.sendTextMessage(encryption("sessionRequest-" + username_init));*/}
            //if(track==1){mConnection.sendTextMessage("LOCATION-"+username_init+"-"+latitudeS+"-"+longitudeS+"-"+transfer_session);/*mConnection.sendTextMessage(encryption("sessionRequest-" + username_init));*/}
            //if(track==1){mConnection.sendTextMessage("LOCATION-"+username_init+"-"+latitudeS+"-"+longitudeS);/*mConnection.sendTextMessage(encryption("sessionRequest-" + username_init));*/}
            //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

    private void sendSerialBLE(String message) {
        Log.d(TAG, "Sending: " + message);
        final byte[] tx = message.getBytes();
        characteristicTX.setValue(tx);
        mBluetoothLeService.writeCharacteristic(characteristicTX);
        // if
    }

    public  void initializeBLE(){
        final Intent intent = getIntent();

        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);

        SharedPreferences ble_mac_add = getSharedPreferences("BLEMACAdd", Context.MODE_PRIVATE);
        String ble_mac = ble_mac_add.getString("blemacadd","");

        mDeviceAddress = ble_mac;

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        Intent broadcastIntent = new Intent("Startbleservice");
        sendBroadcast(broadcastIntent);

    }

    public String encryption(String data, String passkey){
        AESHelper.key = passkey;
        String encryptedData="";
        try {
            encryptedData = AESHelper.encrypt_string(data);
        } catch (Exception e) {
        e.printStackTrace();
    }
        return encryptedData;
    }

    public String decryption(String data, String passkey){
        AESHelper.key = passkey;
        String decryptedData="";
        try {
            decryptedData = AESHelper.decrypt_string(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedData;
    }

    private void bleEncryptSend(String controlData)
    {
        if(mConnected == true) {
            if (new String("TL_OFF-FAN_OFF").equals(controlData)) sendSerialBLE("a");
            else if (new String("TL_OFF-FAN_ON_1").equals(controlData)) sendSerialBLE("b");
            else if (new String("TL_OFF-FAN_ON_2").equals(controlData)) sendSerialBLE("c");
            else if (new String("TL_OFF-FAN_ON_3").equals(controlData)) sendSerialBLE("d");
            else if (new String("TL_OFF-FAN_ON_4").equals(controlData)) sendSerialBLE("e");
            else if (new String("TL_OFF-FAN_ON_5").equals(controlData)) sendSerialBLE("f");

            else if (new String("TL_ON-FAN_OFF").equals(controlData)) sendSerialBLE("g");
            else if (new String("TL_ON-FAN_ON_1").equals(controlData)) sendSerialBLE("h");
            else if (new String("TL_ON-FAN_ON_2").equals(controlData)) sendSerialBLE("i");
            else if (new String("TL_ON-FAN_ON_3").equals(controlData)) sendSerialBLE("j");
            else if (new String("TL_ON-FAN_ON_4").equals(controlData)) sendSerialBLE("k");
            else if (new String("TL_ON-FAN_ON_5").equals(controlData)) sendSerialBLE("l");
            else if (new String("Status_Query").equals(controlData)) sendSerialBLE("q");
        }
    }


    @Override
    public void onBeginningOfSpeech() {
        //Toast.makeText(getApplicationContext(),"onBeginningOfSpeech", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        //Toast.makeText(getApplicationContext(), "onBufferReceived: " + buffer, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEndOfSpeech() {
        //Toast.makeText(getApplicationContext(), "onEndOfSpeech", Toast.LENGTH_SHORT).show();
        toggleButton.setChecked(false);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Toast.makeText(getApplicationContext(), "FAILED " + errorMessage, Toast.LENGTH_SHORT).show();
        toggleButton.setChecked(false);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        //Toast.makeText(getApplicationContext(), "onEvent", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        //Toast.makeText(getApplicationContext(), "onPartialResults", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Toast.makeText(getApplicationContext(), "Speak out your command!!!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResults(Bundle results) {
        //Toast.makeText(getApplicationContext(), "Recognising", Toast.LENGTH_SHORT).show();
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        for (String result : matches) {

            //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();

            if(new String("turn off the fan").equals(result) ) {
                //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                FAN_state = "FAN_OFF";
                fanAlive.setImageResource(R.drawable.fan);
                touch = false;
                fanSpeed.setProgress(0);
                touch = true;
                if (track == 1 && mConnected == false) {
                    mConnection.sendTextMessage(encryption("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state + "-" + transfer_session, shared_aes_encryption_key));
                }
                bleEncryptSend(TL_state + "-" + FAN_state);
                speakOut("Fan turn off request sent");
            }
            else if(new String("fan speed 1").equals(result) ){
                //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                FAN_state = "FAN_ON_1";
                fanAlive.setImageResource(R.drawable.fan_one);
                touch = false;
                fanSpeed.setProgress(1);
                touch = true;
                if (track == 1 && mConnected == false) {
                    mConnection.sendTextMessage(encryption("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state + "-" + transfer_session,shared_aes_encryption_key));
                }
                bleEncryptSend(TL_state + "-" + FAN_state);
                speakOut("Request, to set fan speed at one, sent");
            }
            else if(new String("fan Speed 2").equals(result) ){
                //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                FAN_state = "FAN_ON_2";
                fanAlive.setImageResource(R.drawable.fan_two);
                touch = false;
                fanSpeed.setProgress(2);
                touch = true;
                if (track == 1 && mConnected == false) {
                    mConnection.sendTextMessage(encryption("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state + "-" + transfer_session,shared_aes_encryption_key));
                }
                bleEncryptSend(TL_state + "-" + FAN_state);
                speakOut("Request, to set fan speed at two, sent");
            }
            else if(new String("fan Speed 3").equals(result) ){
                //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                FAN_state = "FAN_ON_3";
                fanAlive.setImageResource(R.drawable.fan_three);
                touch = false;
                fanSpeed.setProgress(3);
                touch = true;
                if (track == 1 && mConnected == false) {
                    mConnection.sendTextMessage(encryption("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state + "-" + transfer_session,shared_aes_encryption_key));
                }
                bleEncryptSend(TL_state + "-" + FAN_state);
                speakOut("Request, to set fan speed at three, sent");
            }
            else if(new String("fan speed 4").equals(result) ){
                //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                FAN_state = "FAN_ON_4";
                fanAlive.setImageResource(R.drawable.fan_four);
                touch = false;
                fanSpeed.setProgress(4);
                touch = true;
                if (track == 1 && mConnected == false) {
                    mConnection.sendTextMessage(encryption("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state + "-" + transfer_session,shared_aes_encryption_key));
                }
                bleEncryptSend(TL_state + "-" + FAN_state);
                speakOut("Request, to set fan speed at four, sent");
            }
            else if(new String("fan speed 5").equals(result) ){
                //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                FAN_state = "FAN_ON_5";
                fanAlive.setImageResource(R.drawable.fan_five);
                touch = false;
                fanSpeed.setProgress(5);
                touch = true;
                if (track == 1 && mConnected == false) {
                    mConnection.sendTextMessage(encryption("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state + "-" + transfer_session,shared_aes_encryption_key));
                }
                bleEncryptSend(TL_state + "-" + FAN_state);
                speakOut("Request, to set fan speed at five, sent");
            }
            else if(new String("turn on the light").equals(result) ){
                //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                FAN_state = "TL_ON";
                tubelight_state = true;
                tubelight.setImageResource(R.drawable.bulbon);
                if (track == 1 && mConnected == false) {
                    mConnection.sendTextMessage(encryption("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state + "-" + transfer_session,shared_aes_encryption_key));
                }
                bleEncryptSend(TL_state + "-" + FAN_state);
                speakOut("Light, turn on request sent");
            }
            else if(new String("turn off the light").equals(result) ){
                //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                FAN_state = "TL_OFF";
                tubelight_state = false;
                tubelight.setImageResource(R.drawable.bulboff);
                if (track == 1 && mConnected == false) {
                    mConnection.sendTextMessage(encryption("CTRL-" + username_init + "-" + TL_state + "-" + FAN_state + "-" + transfer_session,shared_aes_encryption_key));
                }
                bleEncryptSend(TL_state + "-" + FAN_state);
                speakOut("Light, turn off request sent");
            }

            if (track==1) {//mConnection.sendTextMessage(encryption("STATUS-" + username_init+"-"+transfer_session));//mConnection.sendTextMessage(encryption("sessionRequest" + username_init));}
                mConnection.sendTextMessage(encryption("sessionRequest-" + username_init,shared_aes_encryption_key));}
        }

 }

    @Override
    public void onRmsChanged(float rmsdB) {
        //Toast.makeText(getApplicationContext(), "onRmsChangd: " + rmsdB, Toast.LENGTH_SHORT).show();
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                if (new String("startup_page").equals(layout_position)){}
                else speakOut("Thank you for choosing our product and making your room alive. Regards, team Alive Home.");
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }
/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(REQUEST_CODE == requestCode)
        {
            if(requestCode == Activity.RESULT_OK)
            {
                CkRsa rsa = new CkRsa();

                boolean success = rsa.UnlockComponent("Anything for 30-day trial");
                if (success != true) {
                    Log.i("Chilkat", "RSA component unlock failed");
                    return;
                }

                speech = SpeechRecognizer.createSpeechRecognizer(this);
                speech.setRecognitionListener(this);
                recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                        "en");
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                        this.getPackageName());
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

                SharedPreferences logged_data = getSharedPreferences("LayoutData", Context.MODE_PRIVATE);
                layout_position = logged_data.getString("layout","");

                tts = new TextToSpeech(this, this);

                SharedPreferences prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);
                username_init = prefs.getString("username","");
                password_init = prefs.getString("password","");

                if (new String("startup_page").equals(layout_position)){initializeBLE();
                    signuplogincombo_page();}
                else welcome_page();
            }
            else
            {
                // cancle it.
            }
        }
    }
*/
    private void speakOut(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }


    @NonNull
    public static String shared_key_generator() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = 12;
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    static {
        System.loadLibrary("chilkat");
    }
}
