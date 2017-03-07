package com.example.remi.bluetoothspike;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ScanActivity extends AppCompatActivity {

    private static final String TAG = "ScanActivity";

    private BluetoothManager mBluetoothManager;

    private RecyclerView mRecyclerView;
    private DeviceAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        Log.v(TAG, "start");

        mRecyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new DeviceAdapter();
        mRecyclerView.setAdapter(mAdapter);

        List<String> list = new ArrayList<>();
        list.add("tot");
        list.add("string");
        mAdapter.setmDevices(list);

        mBluetoothManager = new BluetoothManager(getApplicationContext());
        mBluetoothManager.startScanning(new BluetoothManagerCallback() {
            @Override
            public void scanResult(Set<BluetoothDevice> devices) {
                super.scanResult(devices);
//                mAdapter.setmDevices(new ArrayList<>(devices));
                Log.v(TAG, devices.size() + " - update devices : " + devices);
            }
        });
    }
}
