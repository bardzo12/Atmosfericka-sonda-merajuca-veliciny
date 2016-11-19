package fiit.pohancenik.matus.baloonsensors;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import fiit.pohancenik.matus.baloonsensors.BluetoothLEConnection.ScanBLEDevicesActivity;
import fiit.pohancenik.matus.baloonsensors.BluetoothLEConnection.BluetoothLEService;
import fiit.pohancenik.matus.baloonsensors.DataManagement.DatabaseHandler;
import fiit.pohancenik.matus.baloonsensors.DataManagement.FileManager;
import fiit.pohancenik.matus.baloonsensors.DataManagement.SessionInfo;
import fiit.pohancenik.matus.baloonsensors.DataManagement.ListAllSessionsActivity;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static String DEVICE_NAME = "DEVICE_NAME";
    private static String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private static String SESSION_NAME = "SESSION_NAME";
    private static boolean APP_MODE;
    private static boolean FOUND_DEVICE = false;
    private boolean mConnected = false;
    private File file;
    private CSVWriter myOutWriter;
    private FileManager FM ;
    private BluetoothLEService mBluetoothLeService;
    private final static String TAG = MainActivity.class
            .getSimpleName();
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private TextView t1;
    private TextView t2;
    private TextView t3;
    private TextView t4;
    private TextView t5;
    private TextView t6;

    private int TemperatureID = 1;
    private int HumidityID = 2;
    private int PressureID = 3;
    private int AccelerationIDX = 4;
    private int AccelerationIDY = 5;
    private int AccelerationIDZ = 6;


    private DatabaseHandler db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // FileManager initialization
        FM = new FileManager(getApplicationContext());
        //DatabaseHandler initialization
        db = new DatabaseHandler(this);


        t1 = (TextView) findViewById(R.id.Temperature_number_show_data_layout_content_main);
        t2 = (TextView) findViewById(R.id.Humidity_number_show_data_layout_content_main);
        t3 = (TextView) findViewById(R.id.Pressure_number_show_data_layout_content_main);
        t4 = (TextView) findViewById(R.id.Acceleration_number_show_data_layout_content_mainX);
        t5 = (TextView) findViewById(R.id.Acceleration_number_show_data_layout_content_mainY);
        t6 = (TextView) findViewById(R.id.Acceleration_number_show_data_layout_content_mainZ);

        final Button buttonSTOP = (Button) findViewById(R.id.STOP_button_content_main);

        buttonSTOP.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                showDialog();


            }
        });







    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Sprava","obnovenie");


    }

    @Override
    protected void onDestroy(){//when app is destroyed, than unregister receiver
                                // and change visibility in GUI
        super.onDestroy();
        FOUND_DEVICE = false;
        invalidateOptionsMenu();
        if(APP_MODE){
            RelativeLayout l = (RelativeLayout) findViewById(R.id.launch_baloon_layout_content_main);
            l.setVisibility(View.GONE);
        }else{
            LinearLayout l = (LinearLayout) findViewById(R.id.connected_to_device_layout_content_main);
            l.setVisibility(View.GONE);
        }

        RelativeLayout l1 = (RelativeLayout) findViewById(R.id.no_connection_layout_content_main);
        l1.setVisibility(View.VISIBLE);
        if(FOUND_DEVICE) {
            unregisterReceiver(mGattUpdateReceiver);
            unbindService(mServiceConnection);
        }

        Log.i("Sprava", "vyplo ju");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {//if ScanBLEDevicesActivity returns result
            if(resultCode == RESULT_OK){// activity result is valid
                DEVICE_NAME = data.getExtras().getString("Device_name");
                DEVICE_ADDRESS = data.getExtras().getString("Device_address");
                SESSION_NAME = data.getExtras().getString("Session_name");
                APP_MODE = data.getExtras().getBoolean("app_mode");
                FOUND_DEVICE = true;

                invalidateOptionsMenu();
                Intent gattServiceIntent = new Intent(this, BluetoothLEService.class);
                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                if(APP_MODE){
                    RelativeLayout l = (RelativeLayout) findViewById(R.id.launch_baloon_layout_content_main);
                    l.setVisibility(View.VISIBLE);
                }else{
                    LinearLayout l = (LinearLayout) findViewById(R.id.connected_to_device_layout_content_main);
                    l.setVisibility(View.VISIBLE);
                    TextView t = (TextView) findViewById(R.id.session_Name_show_data_layout_content_main);
                    t.setText(SESSION_NAME);
                }

                RelativeLayout l1 = (RelativeLayout) findViewById(R.id.no_connection_layout_content_main);
                l1.setVisibility(View.GONE);


                // broadcast receiver registration
                registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
                if (mBluetoothLeService != null) {
                    //connect to device
                    final boolean result = mBluetoothLeService.connect(DEVICE_ADDRESS);
                    Log.d(TAG, "Connect request result=" + result);
                }
                    //gets folder for data storage
                    File packagefolder = FM.getPackageFolder();
                    //gets current date
                    DateFormat df = new SimpleDateFormat("dd-MM-yyyy-HH-mm");
                    String date = df.format(Calendar.getInstance().getTime());

                    //file for data saving
                    file = FM.getFile(packagefolder.getAbsolutePath() , SESSION_NAME + "-" + date + ".csv");// getting file-name contains string set by user and current date

                    //file info are saved into database
                    SessionInfo sessionInfo = new SessionInfo(file.getAbsolutePath(),SESSION_NAME,date);
                    db.addSession(sessionInfo);

                    try {
                        myOutWriter = FM.openStreamForFileCSV(file.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    //    Toast.makeText(this, "Available", Toast.LENGTH_SHORT).show();

            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(FOUND_DEVICE) {
            menu.findItem(R.id.action_connect_settings).setVisible(false);
            super.onPrepareOptionsMenu(menu);
            menu.findItem(R.id.action_show_sessions_list).setVisible(false);
            super.onPrepareOptionsMenu(menu);
        }else{
            menu.findItem(R.id.action_disconnect_settings).setVisible(false);
            super.onPrepareOptionsMenu(menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_connect_settings) {
            //Create Intent for Product Activity
            Intent intent = new Intent(this,ScanBLEDevicesActivity.class);
            //Start ScanBLEDevicesActivity
            startActivityForResult(intent, 1);
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_disconnect_settings) {
            showDialog();
            return true;
        }


        if (id == R.id.action_show_sessions_list) {
            //Create Intent for Product Activity
            Intent intent = new Intent(this,ListAllSessionsActivity.class);
            //Start Product Activity
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void showDialog() {//dialog for asking user, if he is sure to disconnect from device
        new AlertDialog.Builder(this,  R.style.AlertDialogCustom)
                .setTitle("Disconnect")
                .setMessage("Are you sure you want to disconnect from device?")
                .setPositiveButton("DISCONNECT", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mBluetoothLeService.disconnect();
                        FOUND_DEVICE = false;
                        invalidateOptionsMenu();
                        if(APP_MODE){
                            RelativeLayout l = (RelativeLayout) findViewById(R.id.launch_baloon_layout_content_main);
                            l.setVisibility(View.GONE);
                        }else{
                            t1.setText("");
                            t2.setText("");
                            t3.setText("");
                            t4.setText("");
                            t5.setText("");
                            t6.setText("");
                            LinearLayout l = (LinearLayout) findViewById(R.id.connected_to_device_layout_content_main);
                            l.setVisibility(View.GONE);
                        }

                        RelativeLayout l1 = (RelativeLayout) findViewById(R.id.no_connection_layout_content_main);
                        l1.setVisibility(View.VISIBLE);

                        try {
                            FM.closeStreamForFileCSV();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }



    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((BluetoothLEService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up
            // initialization.
            mBluetoothLeService.connect(DEVICE_ADDRESS);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    ;
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLEService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
            } else if (BluetoothLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;

            } else if (BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // start receiving data from connected device
                startGattService(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                processData(intent.getStringExtra(BluetoothLEService.EXTRA_DATA));
            }
        }
    };


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void processData(String data) {// method to process received data

        if (data != null && (data.length() == 20)) {
            // is received message is correct
            final String bracket1 = data.substring(0, 1);
            final String bracket2 = data.substring(2, 3);


            if (bracket1.equals("[") && bracket2.equals("]") ) {//check if message format is valid
                final String Type = data.substring(1, 2);
                if (!APP_MODE) {// if statement about current app mode

                    final String DataLenghtString = data.substring(3, 4);
                    int DataLenght = Integer.parseInt(DataLenghtString);

                    switch (Type) {// switch decides how to process data, Type means Type of sensor
                        case "1":

                            t1.setText(data.substring(20 - DataLenght));
                            FM.writeToCSVFile(myOutWriter, new String[]{"" + TemperatureID, data.substring(20 - DataLenght)});
                            break;
                        case "2":

                            t2.setText(data.substring(20 - DataLenght));
                            FM.writeToCSVFile(myOutWriter, new String[]{"" + HumidityID, data.substring(20 - DataLenght)});
                            break;
                        case "3":

                            t3.setText(data.substring(20 - DataLenght));
                            FM.writeToCSVFile(myOutWriter, new String[]{"" + PressureID, data.substring(20 - DataLenght)});
                            break;
                        case "4":

                            t4.setText(data.substring(20 - DataLenght));
                            FM.writeToCSVFile(myOutWriter, new String[]{"" + AccelerationIDX, data.substring(20 - DataLenght)});
                            break;
                        case "5":

                            t5.setText(data.substring(20 - DataLenght));
                            FM.writeToCSVFile(myOutWriter, new String[]{"" + AccelerationIDY, data.substring(20 - DataLenght)});
                            break;

                        case "6":

                            t6.setText(data.substring(20 - DataLenght));
                            FM.writeToCSVFile(myOutWriter, new String[]{"" + AccelerationIDZ, data.substring(20 - DataLenght)});
                            break;


                    }

                } else {// ...


                    final String DataLenghtString = data.substring(3, 4);
                    int DataLenght = Integer.parseInt(DataLenghtString);
                    switch (Type) {// switch decides how to process data, Type means Type of senso

                        case "1":

                            FM.writeToCSVFile(myOutWriter, new String[]{"" + TemperatureID, data.substring(20 - DataLenght)});
                            break;
                        case "2":

                            FM.writeToCSVFile(myOutWriter, new String[]{"" + HumidityID, data.substring(20 - DataLenght)});
                            break;
                        case "3":

                            FM.writeToCSVFile(myOutWriter, new String[]{"" + PressureID, data.substring(20 - DataLenght)});
                            break;
                        case "4":

                            FM.writeToCSVFile(myOutWriter, new String[]{"" + AccelerationIDX, data.substring(20 - DataLenght)});
                            break;
                        case "5":

                            FM.writeToCSVFile(myOutWriter, new String[]{"" + AccelerationIDY, data.substring(20 - DataLenght)});
                            break;
                        case "6":

                            FM.writeToCSVFile(myOutWriter, new String[]{"" + AccelerationIDZ, data.substring(20 - DataLenght)});
                            break;

                    }

                }
            }
        }

    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.



    private void startGattService(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            if(uuid.equals("0000ffe0-0000-1000-8000-00805f9b34fb")) {

                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();

                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    uuid = gattCharacteristic.getUuid().toString();
                    if(uuid.equals("0000ffe1-0000-1000-8000-00805f9b34fb")) {

                        mBluetoothLeService.setCharacteristicNotification(
                                gattCharacteristic, true);
                    }
                }

            }
        }



    }

}