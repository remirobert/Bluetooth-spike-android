package com.example.remi.bluetoothspike;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

public class ScanActivity extends AppCompatActivity {

    private static final String TAG = "ScanActivity";

    private TextView mtextTextViewDevice;
    private TextView mtextTextViewStatus;

    private BluetoothManager mBluetoothManager;
    private RecyclerView mRecyclerView;
    private DeviceAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private DeviceAdapter.DeviceViewHolderClick mDeviceViewHolderClick = new DeviceAdapter.DeviceViewHolderClick() {
        @Override
        public void onDevice(final BluetoothDevice device) {
            AlertDialog alertDialog = new AlertDialog.Builder(ScanActivity.this).create();
            alertDialog.setTitle("Connect to : " + device.getName() + " ?");
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Connect",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mBluetoothManager.connectDevice(device);
                        }
                    });
            alertDialog.show();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        Log.v(TAG, "start");

        mtextTextViewDevice = (TextView) findViewById(R.id.text_view_connected_device);
        mtextTextViewStatus = (TextView) findViewById(R.id.text_view_connection_status);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycle_view);

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new DeviceAdapter(mDeviceViewHolderClick);
        mRecyclerView.setAdapter(mAdapter);

        mBluetoothManager = new BluetoothManager(getApplicationContext(), new BluetoothManagerCallback() {
            @Override
            public void scanResult(Set<BluetoothDevice> devices) {
                mAdapter.setmDevices(new ArrayList<>(devices));
            }

            @Override
            public void didUpdateConnection(final BluetoothDevice device, final String state) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mtextTextViewDevice.setText(device.getName());
                        mtextTextViewStatus.setText(state);
                    }
                });
            }
        });
        mBluetoothManager.startScanning();
    }
}
