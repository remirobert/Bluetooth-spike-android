package com.example.remi.bluetoothspike;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
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

import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED;

/**
 * Created by remi on 07/03/2017.
 */

public final class BluetoothManager {

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Context mContext;
    private static final String TAG = "BluetoothManager";
    private static final int REQUEST_ENABLE_BT = 0;
    private BluetoothManagerCallback mManagerCallback;

    private Set<BluetoothDevice> mDevices;

    private void didScanNewDevice(BluetoothDevice device) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (device.getType() != BluetoothDevice.DEVICE_TYPE_UNKNOWN) {
                mDevices.add(device);
            }
        }
        else {
            mDevices.add(device);
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            switch (action) {
                case BluetoothDevice.ACTION_FOUND: {
                    didScanNewDevice(device);
                    break;
                }
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED: {
                    mBluetoothAdapter.startDiscovery();
                    break;
                }
                case BluetoothDevice.ACTION_ACL_CONNECTED: {
                    pairedDevices();
                    mManagerCallback.didUpdateConnection(device, "connected");
                    Log.v(TAG, "Connected to device");
                    break;
                }
                case BluetoothDevice.ACTION_ACL_DISCONNECTED: {
                    mManagerCallback.didUpdateConnection(device, "disconnected");
                    Log.v(TAG, "disconnected to device");
                    break;
                }
                default: break;
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

    public Boolean pairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        Log.v(TAG, "Paired devices connected : " + pairedDevices.size());
        if (pairedDevices.size() == 0) {
            return false;
        }
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                Log.v(TAG, "Found device " + deviceName + " MAC : " + deviceHardwareAddress);
                connectDevice(device);
            }
        }
        return true;
    }

    public void startScanning() {
        if (!checkBluetoothAvailability()) {
            return;
        }

        pairedDevices();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

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
                        mManagerCallback.scanResult(mDevices);
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
                    mManagerCallback.scanResult(mDevices);
                }
            });
        }
    }

    public void stopScanning() {
        mContext.unregisterReceiver(mReceiver);
        mBluetoothAdapter.cancelDiscovery();
    }

    public void connectDevice(final BluetoothDevice device) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            BluetoothGattCallback callbackGatt = new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);
                    String state = "None";
                    switch (newState) {
                        case BluetoothGatt.STATE_CONNECTING:
                            state = "connecting";
                            break;
                        case BluetoothGatt.STATE_CONNECTED:
                            state = "connected";
                            break;
                        case BluetoothGatt.STATE_DISCONNECTING:
                            state = "disconnecting";
                            break;
                        case BluetoothGatt.STATE_DISCONNECTED:
                            state = "disconnected";
                            break;
                        default: break;
                    }
                    mManagerCallback.didUpdateConnection(device, state);
                }
            };
            mManagerCallback.didUpdateConnection(device, "connecting");
            device.connectGatt(mContext, true, callbackGatt);
            if (!device.createBond()) {
                Log.e(TAG, "impossible to bound the device");
            }
        }
    }

    public BluetoothManager(Context mContext, BluetoothManagerCallback callback) {
        this.mContext = mContext;
        mDevices = new HashSet<>();
        mManagerCallback = callback;
    }
}
