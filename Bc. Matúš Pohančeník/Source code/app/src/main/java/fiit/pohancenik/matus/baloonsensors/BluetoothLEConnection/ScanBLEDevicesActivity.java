package fiit.pohancenik.matus.baloonsensors.BluetoothLEConnection;


import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import fiit.pohancenik.matus.baloonsensors.DataManagement.DatabaseHandler;
import fiit.pohancenik.matus.baloonsensors.R;

import java.util.ArrayList;

/**
 * Created by matus on 30. 12. 2015.
 */
public class ScanBLEDevicesActivity extends ListActivity {
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private boolean mScanning;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 5000;
    private  BluetoothDevice device=null;
    private DatabaseHandler db;
    private Runnable mRunnable;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_scan_ble_devices);
        this.setFinishOnTouchOutside(false);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getListView().setSelector(R.color.colorSelector);
        db = new DatabaseHandler(this);

        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            finish();
            return;
        }

        final Button button = (Button) findViewById(R.id.buttonCancel_scan_BLE_devices);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                finish();


            }
        });
        final Button button1 = (Button) findViewById(R.id.buttonOK1_scan_BLE_devices);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (device != null) {//If device was selected, change the GUI view

                    findViewById(R.id.choosingDevice_scan_BLE_devices).setVisibility(View.GONE);
                    findViewById(R.id.buttonOK1_scan_BLE_devices).setVisibility(View.GONE);
                    findViewById(R.id.setNameofSession_scan_BLE_devices).setVisibility(View.VISIBLE);
                    findViewById(R.id.buttonOK2_scan_BLE_devices).setVisibility(View.VISIBLE);
                    TextView t = (TextView) findViewById(R.id.textView2);
                    t.setText("Session name");

                }else{
                    Log.e(ScanBLEDevicesActivity.class
                            .getSimpleName(), "No device");
                    Toast.makeText(ScanBLEDevicesActivity.this, "Choose device", Toast.LENGTH_SHORT).show();

                }


            }
        });


        final Button button2 = (Button) findViewById(R.id.buttonOK2_scan_BLE_devices);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent data = new Intent();
                TextView tv = (TextView) findViewById(R.id.SessionNameText_scan_BLE_devices);


                if(! (tv.getText().toString().equals(""))){//if session name was filled, than check name in database
                    if(!(db.checkSessionName(tv.getText().toString()))) {//if the name isn't duplicate, than send data back to the MainActivity
                        CheckBox checkB = (CheckBox) findViewById(R.id.checkBox_scan_BLE_devices);
                        if (checkB.isChecked()){
                            data.putExtra("app_mode", true);
                        }else {
                            data.putExtra("app_mode", false);
                        }
                        data.putExtra("Device_name", device.getName());
                        data.putExtra("Device_address", device.getAddress());


                        data.putExtra("Session_name", String.format(tv.getText().toString()));

                        // Activity finished ok, return the data
                        setResult(RESULT_OK, data);

                        if (mScanning) {
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                            mScanning = false;
                        }
                        finish();
                    }else{
                        Toast.makeText(ScanBLEDevicesActivity.this, "Session name already exist", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(ScanBLEDevicesActivity.this, "Session name isn't filled", Toast.LENGTH_SHORT).show();
                }






            }
        });

    }



    @Override
    protected void onStart() {
        super.onStart();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }


    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter.isEnabled()) {


            // Initializes list view adapter.
            mLeDeviceListAdapter = new LeDeviceListAdapter();
            setListAdapter(mLeDeviceListAdapter);
            scanLeDevice(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
        Log.d("Callbacks", "Removed");
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
           mRunnable = new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    if(getListView().getCount() == 0){
                        Toast.makeText(ScanBLEDevicesActivity.this, "NO DEVICES", Toast.LENGTH_SHORT).show();
                        finish();
                    }else {
                        findViewById(R.id.scanningDevices_scan_BLE_devices).setVisibility(View.GONE);
                        findViewById(R.id.choosingDevice_scan_BLE_devices).setVisibility(View.VISIBLE);
                        findViewById(R.id.buttonOK1_scan_BLE_devices).setVisibility(View.VISIBLE);
                        TextView t = (TextView) findViewById(R.id.textView2);
                        t.setText("Found devices");

                    }
                }
            };
            mHandler.postDelayed(mRunnable, SCAN_PERIOD);



            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        device = (BluetoothDevice) getListView().getItemAtPosition(position);
        if (device == null) return;
    }



    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };


    private class LeDeviceListAdapter extends BaseAdapter {// Adapter for list of found devices
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = ScanBLEDevicesActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address_text_listitem_device);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name_text_listitem_device);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }


    }
    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

}