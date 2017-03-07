package com.example.remi.bluetoothspike;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by remi on 07/03/2017.
 */

public final class BluetoothManager {

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Context mContext;
    private static final String TAG = "BluetoothManager";
    private static final int REQUEST_ENABLE_BT = 0;

    public Set<BluetoothDevice> mDevices;

    private void didScanNewDevice(BluetoothDevice device) {
        Log.v(TAG, "Found device " + device.getName() + " MAC : " + device.getAddress());
        mDevices.add(device);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v(TAG, "current action : " + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                didScanNewDevice(device);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mBluetoothAdapter.startDiscovery();
            }
        }
    };

    private Boolean checkBluetoothAvailability() {
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth not avalaible on device");
            Toast.makeText(mContext, "Bluetooth not available", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(mContext, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
            return false;
        }
        Toast.makeText(mContext, "Bluetooth ready", Toast.LENGTH_SHORT).show();
        return true;
    }

    public void pairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        Log.v(TAG, "Paired devices connected : " + pairedDevices.size());
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                Log.v(TAG, "Found device " + deviceName + " MAC : " + deviceHardwareAddress);
            }
        }
    }

    public void startScanning(final BluetoothManagerCallback callback) {
        if (!checkBluetoothAvailability()) {
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        mContext.registerReceiver(mReceiver, filter);
        mBluetoothAdapter.startDiscovery();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.v(TAG, "Start scanning BLE device 1");
            BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
            scanner.startScan(new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        BluetoothDevice device = result.getDevice();
                        didScanNewDevice(device);
                        callback.scanResult(mDevices);
                    }
                }
            });
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Log.v(TAG, "Start scanning BLE device 2");
            mBluetoothAdapter.startLeScan(new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    didScanNewDevice(device);
                    callback.scanResult(mDevices);
                }
            });
        }
    }

    public void stopScanning() {
        mContext.unregisterReceiver(mReceiver);
        mBluetoothAdapter.cancelDiscovery();
    }

    public BluetoothManager(Context mContext) {
        this.mContext = mContext;
        mDevices = new HashSet<>();
    }
}
